package com.hcs.mixin.block;

import com.hcs.util.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CropBlock.class)
public class CropBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        //Slow down growing speed
        if (Math.random() > 0.2) ci.cancel();
    }

    @Inject(method = "appendProperties", at = @At("HEAD"))
    protected void appendProperties(StateManager.@NotNull Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(WorldHelper.FERTILIZER_FREE);
    }

    @Inject(method = "withAge", at = @At("RETURN"), cancellable = true)
    public void withAge(int age, @NotNull CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        //See particles effect at BoneMealItemMixin/useOnFertilizable()
        if (state.getEntries().containsKey(WorldHelper.FERTILIZER_FREE)) {
            boolean fertilizerFree = state.get(WorldHelper.FERTILIZER_FREE);
            if (fertilizerFree)
                cir.setReturnValue(state.with(WorldHelper.FERTILIZER_FREE, true)); //break gain against counteracting for refreshing block state
            else cir.setReturnValue(Blocks.AIR.getDefaultState());
        }
    }
}