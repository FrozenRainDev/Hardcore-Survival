package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.CombustionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(at = @At("RETURN"), method = "canSurvive", cancellable = true)
    public void canSurvive(BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (level.getBlockState(pos.below()).is(Reg.DRYING_RACK)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "getDestroyProgress", cancellable = true)
    public void getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (player != null) {
            if (state.getDestroySpeed(level, pos) == -1.0F) player.getDestroySpeed(state);
            net.minecraft.world.level.block.Block block = state.getBlock();
            if (block instanceof SweetBerryBushBlock)
                cir.setReturnValue(player.getDestroySpeed(state) / 0.18F / 30);
            else if (block instanceof SugarCaneBlock) {
                var mainHandItem = player.getMainHandItem().getItem();
                final boolean isUsingSuitableTool = mainHandItem instanceof SwordItem || mainHandItem instanceof AxeItem;
                cir.setReturnValue(isUsingSuitableTool ? 0.15F : 0.01F);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "entityInside")
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity instanceof ItemEntity itemEntity)
            CombustionHelper.checkAddFuel(entity.level(), entity.blockPosition(), state, itemEntity.getItem());
    }
}