package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {
    @Inject(method = "appendProperties", at = @At("HEAD"))
    protected void appendProperties(StateManager.@NotNull Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(WorldHelper.FERTILIZER_FREE);
    }

    @Inject(method = "canPlaceAt", at = @At("RETURN"))
    public void canPlaceAt(BlockState state, WorldView world, BlockPos pos, @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && state.contains(WorldHelper.FERTILIZER_FREE) && !state.get(WorldHelper.FERTILIZER_FREE) && world instanceof ServerWorld serverWorld)
            serverWorld.setBlockState(pos, state.with(WorldHelper.FERTILIZER_FREE, true));
    }
}
