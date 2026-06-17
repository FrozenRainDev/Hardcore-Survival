package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void hcsurvival$checkWebCollision(CallbackInfo ci) {
        // Cast 'this' to AbstractArrow to access entity methods
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        BlockState state = arrow.level().getBlockState(arrow.blockPosition());

        // If the arrow is currently inside a cobweb, drastically reduce its speed
        if (state.is(Blocks.COBWEB)) {
            Vec3 currentMovement = arrow.getDeltaMovement();
            // Apply a drag multiplier mimicking the cobweb's sticky behavior
            arrow.setDeltaMovement(currentMovement.multiply(0.25D, 0.05F, 0.25D));
        }
    }
}