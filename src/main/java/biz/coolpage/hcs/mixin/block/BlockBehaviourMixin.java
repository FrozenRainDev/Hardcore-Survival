package biz.coolpage.hcs.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
    // Also see EntityMixin
    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void hcs$removeLeavesCollision(@NotNull BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        // Remove collision volume for leaves
        if (state.is(BlockTags.LEAVES)) {
            cir.setReturnValue(Shapes.empty());
        }
    }

//    @Inject(method = "entityInside", at = @At("HEAD"))
//    private void hcs$applyVegetationSlowdown(@NotNull BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
//        if (state.is(BlockTags.LEAVES)) {
//            entity.makeStuckInBlock(state, new Vec3(1.2D, 1.2D, 1.2D));
//        }
//    }
}