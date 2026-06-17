package biz.coolpage.hcs.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block; // Newly added import
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class CobwebBlockMixin {
    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void hcs$projectileWebCollision(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        // Check if the current block instance is a Cobweb (WebBlock)
        if ((Object) this instanceof WebBlock) {
            // Verify if the collision context contains an entity, and if that entity is a Projectile
            if (context instanceof EntityCollisionContext entityContext && entityContext.getEntity() instanceof Projectile) {
                // Return a slightly smaller block collision shape so the projectile hits it
                // and its center falls strictly inside the web block coordinates, preventing bouncing.
                cir.setReturnValue(Block.box(0.1, 0.1, 0.1, 15.9, 15.9, 15.9));
            }
        }
    }
}