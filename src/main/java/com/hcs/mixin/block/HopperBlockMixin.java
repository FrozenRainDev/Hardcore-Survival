package com.hcs.mixin.block;

import com.hcs.item.HotWaterBottleItem;
import com.hcs.main.Reg;
import com.hcs.main.helper.RotHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlock.class)
public class HopperBlockMixin {
    @Inject(at = @At("HEAD"), method = "getRaycastShape")
    public void getRaycastShape(BlockState state, @NotNull BlockView blockView, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
        BlockEntity entity = blockView.getBlockEntity(pos);
        if (entity == null) {
            Reg.LOGGER.error("HopperBlockMixin/getRaycastShape;entity==null");
        } else {
            RotHelper.update(entity.getWorld(), (Inventory) entity);
            HotWaterBottleItem.update(entity.getWorld(), (Inventory) entity);
        }
    }
}
