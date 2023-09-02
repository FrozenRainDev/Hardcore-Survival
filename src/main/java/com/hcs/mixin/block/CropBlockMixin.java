package com.hcs.mixin.block;

import com.hcs.util.WorldHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public class CropBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        //Slow down growing speed
        if (Math.random() > 0.2) ci.cancel();
    }

    @Inject(method = "applyGrowth", at = @At("HEAD"), cancellable = true)
    public void applyGrowth(@NotNull World world, @NotNull BlockPos pos, BlockState state, CallbackInfo ci) {
        BlockState stateDown = world.getBlockState(pos.down());
        if (stateDown.isOf(Blocks.FARMLAND) && stateDown.getEntries().containsKey(WorldHelper.FERTILIZER_FREE) && !stateDown.get(WorldHelper.FERTILIZER_FREE))
            ci.cancel();
    }
}