package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.item.HotWaterBottleItem;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlock.class)
public class HopperBlockMixin {
    @Inject(at = @At("HEAD"), method = "getInteractionShape")
    public void getInteractionShape(BlockState state, @NotNull BlockGetter blockView, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
        BlockEntity entity = blockView.getBlockEntity(pos);
        if (entity == null) {
            Reg.LOGGER.error("HopperBlockMixin/getInteractionShape;entity==null");
        } else {
            RotHelper.update(entity.getLevel(), (Container) entity);
            HotWaterBottleItem.update(entity.getLevel(), (Container) entity);
        }
    }
}
