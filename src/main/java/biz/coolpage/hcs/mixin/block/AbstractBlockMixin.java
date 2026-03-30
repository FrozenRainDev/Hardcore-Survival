package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.Hcs;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class AbstractBlockMixin {
    @Inject(at = @At("RETURN"), method = "canSurvive", cancellable = true)
    public void canPlaceAt(BlockState state, @NotNull LevelReader world, @NotNull BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        //Drying rack should occupy 2 height, while it only consist of 1 block, so it is forbidden to place any upper block
        if (world.getBlockState(pos.below()).is(Hcs.DRYING_RACK.get())) cir.setReturnValue(false);
    }

    @Inject(at = @At("HEAD"), method = "getDestroyProgress", cancellable = true)
    public void calcBlockBreakingDelta(BlockState state, Player player, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (player != null) {
            if (state.getDestroySpeed(world, pos) == -1.0F) player.getDestroySpeed(state);
            Block block = state.getBlock();
            //See BambooBlockMixin/calcBlockBreakingDelta()
            if (block instanceof SweetBerryBushBlock)
                cir.setReturnValue(player.getDestroySpeed(state) / 0.18F / 30);
            else if (block instanceof SugarCaneBlock) {
                var mainHandItem = player.getMainHandItem().getItem();
                final boolean isUsingSuitableTool = mainHandItem instanceof SwordItem/*Knives included*/ || mainHandItem instanceof AxeItem;
                cir.setReturnValue(isUsingSuitableTool ? 0.15F : 0.01F);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "entityInside")
    public void onEntityCollision(BlockState state, Level world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity instanceof ItemEntity itemEntity)
            CombustionHelper.checkAddFuel(entity.level(), entity.blockPosition(), state, itemEntity.getItem());
    }
}