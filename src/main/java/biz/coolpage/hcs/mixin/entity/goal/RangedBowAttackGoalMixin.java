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
    @ Shadow @Final private Mob mob;
    @Shadow @Final private float attackRadiusSqr;

    // Inject right before the vanilla code checks if the mob is using the item.
    // Changed the target owner from LivingEntity to Mob due to Java type erasure in bytecode.
    @Inject(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;isUsingItem()Z"),
            cancellable = true
    )
    private void hcs$cancelBowActionWhenFar(CallbackInfo ci) {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            double distanceSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            // If the distance is greater than the attack radius (far away)
            if (distanceSqr > (double) this.attackRadiusSqr) {
                // 1. Stop using the bow if they were drawing it out of range (prevents walking while drawing)
                if (this.mob.isUsingItem()) {
                    this.mob.stopUsingItem();
                }
                // 2. Cancel the rest of the tick to skip vanilla bow drawing and shooting logic
                ci.cancel();
            }
        }
    }
}