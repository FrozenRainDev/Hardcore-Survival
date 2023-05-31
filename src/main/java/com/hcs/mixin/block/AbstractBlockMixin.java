package com.hcs.mixin.block;

import com.hcs.main.Reg;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(at = @At("RETURN"), method = "canPlaceAt", cancellable = true)
    public void canPlaceAt(BlockState state, @NotNull WorldView world, @NotNull BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        //Drying rack should occupy 2 height, while it only consist of 1 block, so it is forbidden to place any upper block
        if (world.getBlockState(pos.down()).isOf(Reg.DRYING_RACK)) cir.setReturnValue(false);
    }
}
