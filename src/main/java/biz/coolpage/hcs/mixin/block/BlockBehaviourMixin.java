package biz.coolpage.hcs.mixin.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    // Also see EntityMixin
    @ModifyReturnValue(method = "getCollisionShape", at = @At("RETURN"))
    private VoxelShape hcs$removeLeavesCollision(VoxelShape original, @NotNull BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        // Remove collision volume for leaves
        // Return empty shape if it's leaves, otherwise fallback to the original shape
        if (state.is(BlockTags.LEAVES)) {
            return Shapes.empty();
        }
        return original;
    }

    @Inject(method = "entityInside", at = @At("HEAD"))
    private void hcs$leavesFallDamage(BlockState state, @NotNull Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        // Apply fall damage once when falling into leaves
        if (!level.isClientSide && state.is(BlockTags.LEAVES)) {
            if (entity.fallDistance > 0.0F) {
                // Cause fall damage using entity's current fall distance
                entity.causeFallDamage(entity.fallDistance, 1.0F, level.damageSources().fall());
                // Reset fall distance to prevent continuous damage while falling through leaves
                entity.fallDistance = 0.0F;
            }
        }
    }
}