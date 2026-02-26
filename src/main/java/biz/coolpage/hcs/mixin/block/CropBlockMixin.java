package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin {
    @Shadow
    public abstract void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random);

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        //Slow down growing speed
        if (Math.random() > 0.2) ci.cancel();
    }

    @Inject(method = "performBonemeal", at = @At("HEAD"), cancellable = true)
    public void performBonemeal(@NotNull Level world, @NotNull RandomSource random, @NotNull BlockPos pos, BlockState state, CallbackInfo ci) {
        BlockState stateDown = world.getBlockState(pos.below());
        if (stateDown.is(Blocks.FARMLAND) && stateDown.getValues().containsKey(WorldHelper.FERTILIZER_FREE) && !stateDown.getValue(WorldHelper.FERTILIZER_FREE))
            ci.cancel();
    }

    @SuppressWarnings("all")
    public void precipitationTick(BlockState state, Level world, BlockPos pos, Biome.Precipitation precipitation) {
        if (world instanceof ServerLevel serverWorld && world.getGameTime() % 1000 == 0L)
            this.randomTick(state, serverWorld, pos, RandomSource.create());
    }
}
