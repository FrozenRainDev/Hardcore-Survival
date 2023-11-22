package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.Reg;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
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

    @Inject(at = @At("HEAD"), method = "calcBlockBreakingDelta", cancellable = true)
    public void calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (player != null) {
            if (state.getHardness(world, pos) == -1.0F) player.getBlockBreakingSpeed(state);
            Block block = state.getBlock();
            //See BambooBlockMixin/calcBlockBreakingDelta()
            if (block instanceof SweetBerryBushBlock)
                cir.setReturnValue(player.getBlockBreakingSpeed(state) / 0.18F / 30);
            else if (block instanceof SugarCaneBlock) cir.setReturnValue(0.05F);
        }
    }
}
