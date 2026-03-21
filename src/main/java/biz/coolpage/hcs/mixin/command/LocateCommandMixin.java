package biz.coolpage.hcs.mixin.command;

import biz.coolpage.hcs.util.CommUtil;
import biz.coolpage.hcs.util.WorldHelper;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LocateCommand.class)
public abstract class LocateCommandMixin {

    // 使用 @Shadow 引用私有异常字段
    @Shadow @Final private static DynamicCommandExceptionType ERROR_STRUCTURE_NOT_FOUND;
    @Shadow @Final private static DynamicCommandExceptionType ERROR_STRUCTURE_INVALID;

    // 使用 @Shadow 引用私有辅助方法
    @Shadow
    private static Optional<? extends HolderSet.ListBacked<Structure>> getHolders(ResourceOrTagKeyArgument.Result<Structure> pStructure, Registry<Structure> pStructureRegistry) {
        return Optional.empty();
    }

    // prevent /locate excessive memory cost when finding non-existent village(mod deleted)
    // 目标方法名从 executeLocateStructure (Yarn) 改为 locateStructure (Mojang)
    @Inject(method = "locateStructure", at = @At("HEAD"))
    private static void executeLocateStructure(@NotNull CommandSourceStack source, ResourceOrTagKeyArgument.Result<Structure> predicate, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        Registry<Structure> registry = source.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);

        // 调用 Shadow 的 getHolders，并使用 asPrintable() 代替 asString()
        HolderSet<?> holderSet = getHolders(predicate, registry).orElseThrow(() ->
                ERROR_STRUCTURE_INVALID.create(predicate.asPrintable())
        );

        holderSet.forEach(holder -> {
            // 在 Mojang 映射中，RegistryEntry 对应 Holder
            if (CommUtil.regEntryContains(holder, "village") && !WorldHelper.shouldGenerateVillages()) {
                try {
                    // 使用 asPrintable() 代替 asString()
                    throw ERROR_STRUCTURE_NOT_FOUND.create(predicate.asPrintable());
                } catch (CommandSyntaxException e) {
                    // Mixin 中 HEAD 注入如果要抛出受检异常，通常需要包装成 RuntimeException
                    // 或者通过 cir.setReturnValue 配合异常逻辑处理
                    throw new RuntimeException(e);
                }
            }
        });
    }
}