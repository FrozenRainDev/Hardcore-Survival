package biz.coolpage.hcs.mixin.command;

import biz.coolpage.hcs.util.CommUtil;
import biz.coolpage.hcs.util.WorldHelper;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.RegistryPredicateArgumentType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.command.LocateCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.gen.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.server.command.LocateCommand.STRUCTURE_INVALID_EXCEPTION;
import static net.minecraft.server.command.LocateCommand.STRUCTURE_NOT_FOUND_EXCEPTION;

@Mixin(LocateCommand.class)
public class LocateCommandMixin {
    //prevent /locate excessive memory cost when finding non-existent village(mod deleted)
    @Inject(method = "executeLocateStructure", at = @At("HEAD"))
    private static void executeLocateStructure(@NotNull ServerCommandSource source, RegistryPredicateArgumentType.RegistryPredicate<Structure> predicate, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        Registry<Structure> registry = source.getWorld().getRegistryManager().get(RegistryKeys.STRUCTURE);
        RegistryEntryList<?> registryEntryList = LocateCommand.getStructureListForPredicate(predicate, registry).orElseThrow(() -> STRUCTURE_INVALID_EXCEPTION.create(predicate.asString()));
        registryEntryList.forEach(registryEntry -> {
            if (CommUtil.regEntryContains(registryEntry, "village") && !WorldHelper.shouldGenerateVillages()) {
                try {
                    throw STRUCTURE_NOT_FOUND_EXCEPTION.create(predicate.asString());
                } catch (CommandSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
