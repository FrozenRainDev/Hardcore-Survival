package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FarmBlock.class)
public class FarmlandBlockMixin {
    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"))
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(WorldHelper.FERTILIZER_FREE);
    }

    @Inject(method = "canSurvive", at = @At("RETURN"))
    public void canSurvive(BlockState state, LevelReader world, BlockPos pos, @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && state.hasProperty(WorldHelper.FERTILIZER_FREE) && !state.getValue(WorldHelper.FERTILIZER_FREE) && world instanceof ServerLevel serverWorld)
            serverWorld.setBlock(pos, state.setValue(WorldHelper.FERTILIZER_FREE, true), 3);
    }
}
