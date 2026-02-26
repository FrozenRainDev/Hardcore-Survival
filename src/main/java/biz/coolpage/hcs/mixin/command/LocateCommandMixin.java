package biz.coolpage.hcs.mixin.command;

import biz.coolpage.hcs.util.CommUtil;
import biz.coolpage.hcs.util.WorldHelper;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.server.commands.LocateCommand.ERROR_STRUCTURE_INVALID;
import static net.minecraft.server.commands.LocateCommand.ERROR_STRUCTURE_NOT_FOUND;

@Mixin(LocateCommand.class)
public class LocateCommandMixin {
    //prevent /locate excessive memory cost when finding non-existent village(mod deleted)
    @Inject(method = "locateStructure", at = @At("HEAD"))
    private static void locateStructure(@NotNull CommandSourceStack source, ResourceOrTagKeyArgument.Result<Structure> predicate, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        Registry<Structure> registry = source.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
        HolderSet<?> holderSet = LocateCommand.getHolderSet(predicate, registry).orElseThrow(() -> ERROR_STRUCTURE_INVALID.create(predicate.asPrintable()));
        holderSet.forEach(registryEntry -> {
            if (CommUtil.regEntryContains(registryEntry, "village") && !WorldHelper.shouldGenerateVillages()) {
                try {
                    throw ERROR_STRUCTURE_NOT_FOUND.create(predicate.asPrintable());
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
