package biz.coolpage.hcs.mixin.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedBowAttackGoal.class)
public abstract class RangedBowAttackGoalMixin {
    @Shadow @Final private Mob mob;
    @Shadow private int seeTime;
    @Shadow private int strafingTime;
    @Shadow @Final private double speedModifier;
    @Shadow @Final private float attackRadiusSqr;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void hcs$approachWhenFar(CallbackInfo ci) {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            double distanceSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());

            // If the distance is greater than the attack radius (far away)
            if (distanceSqr > (double) this.attackRadiusSqr) {

                // 1. Replicate vanilla line-of-sight logic so the seeTime state remains healthy
                boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(target);
                boolean isSeeing = this.seeTime > 0;
                if (hasLineOfSight != isSeeing) {
                    this.seeTime = 0;
                }
                if (hasLineOfSight) {
                    ++this.seeTime;
                } else {
                    --this.seeTime;
                }

                // 2. Approach logic: Only move towards the target, do not strafe, and look at the target
                this.mob.getNavigation().moveTo(target, this.speedModifier);
                this.strafingTime = -1;
                this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

                // 3. Stop using the bow if they were drawing it out of range (prevents walking while drawing)
                if (this.mob.isUsingItem()) {
                    this.mob.stopUsingItem();
                }

                // 4. Cancel the rest of the tick to skip vanilla bow drawing and shooting logic
                ci.cancel();
            }

            // If distanceSqr <= attackRadiusSqr, the ci.cancel() is skipped.
            // The vanilla tick() takes over entirely, handling strafing, aiming, and shooting perfectly.
        }
    }
}