package biz.coolpage.hcs.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class StrawBedBlock extends BedBlock {
    public StrawBedBlock(DyeColor pColor, BlockBehaviour.Properties pProperties) {
        super(pColor, pProperties);
    }

    // Inherit all vanilla BedBlock logic to respect the original code structure.
    // Spawning point modification and single-use logic are handled via Forge Events
    // to avoid heavily overriding complex Player sleep routines in the use() method.
    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public void updateEntityAfterFallOn(@NotNull BlockGetter pLevel, @NotNull Entity pEntity) {
        // Skip bed bounce logics by reproducing vanilla Block.class logic directly
        // This sets the Y (vertical) delta movement to 0 to cancel the fall velocity
        pEntity.setDeltaMovement(pEntity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
    }

    @Override
    public void fallOn(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockPos pPos, @NotNull Entity pEntity, float pFallDistance) {
        // Skip BedBlock's 0.5F multiplier, apply our own 0.7F
        // Reproduce vanilla Block.class fall damage logic directly
        pEntity.causeFallDamage(pFallDistance * 0.7F, 1.0F, pLevel.damageSources().fall());
    }
}