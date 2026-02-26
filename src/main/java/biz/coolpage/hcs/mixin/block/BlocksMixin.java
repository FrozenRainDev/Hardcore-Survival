package biz.coolpage.hcs.mixin.block;

import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Blocks.class)
public class BlocksMixin {
    @ModifyVariable(method = "litBlockEmission", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static int litBlockEmission(int value) {
        if (value <= 7 && value > 4) return 4; // Redstone torch
        return value;
    }
}
