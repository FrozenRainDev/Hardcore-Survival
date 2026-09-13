package biz.coolpage.hcs.mixin.entity.goal;

import biz.coolpage.hcs.status.accessor.IKickCoolDown;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PanicGoal.class)
public abstract class PanicGoalMixin {

    @Shadow
    @Final
    protected PathfinderMob mob;

    @Shadow
    protected double posX, posY, posZ;

    @ModifyArg(method = "start", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;moveTo(DDDD)Z"), index = 3)
    public double hcs$modifyEscapeSpeed(double speed) {
        biz.coolpage.hcs.Hcs.info("hcs$modifyEscapeSpeed was called!");
        //Improve escaping speed
        if (mob instanceof Cow) speed *= 1.1;
        else if (mob instanceof Chicken) speed *= 1.2;
        else speed *= 1.4;
        return speed; // * 5; // todo test
    }

//    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
//    public void hcs$modifyMoveTo(@NotNull CallbackInfo ci) {
//        var attacker = this.mob.getLastHurtByMob();
//        biz.coolpage.hcs.Hcs.info("attacker " + attacker);
//        if (attacker != null) {
//            Vec3 fromPlayer = this.mob.position().subtract(attacker.position()).normalize(); // vector from player to this.mob
//            Vec3 fleeDir = fromPlayer.scale(26.5); // flee distance
//            Vec3 targetPos = this.mob.position().add(fleeDir); // target position
//            biz.coolpage.hcs.Hcs.info("targetPos " + targetPos);
//            // initiate movement towards the target position at sprint speed modifier
//            this.mob.getNavigation().moveTo(
//                    targetPos.x,
//                    targetPos.y,
//                    targetPos.z,
//                    2.0D
//            );
//            ci.cancel();
//        }
//    }

    @Inject(method = "findRandomPosition", at = @At("HEAD"), cancellable = true)
    protected void hcs$findRandomPosition(CallbackInfoReturnable<Boolean> cir) {
        var attacker = this.mob.getLastHurtByMob();
        // If there is no attacker (e.g. panic caused by fire or freezing), let vanilla handle it
        if (attacker == null) return;

        // Use vanilla's terrain-aware logic to find a safe position AWAY from the attacker.
        // 16 is the horizontal range, 7 is the vertical range (same as AvoidEntityGoal).
        Vec3 safePosAway = DefaultRandomPos.getPosAway(this.mob, 48, 7, attacker.position());

        if (safePosAway != null) {
            this.posX = safePosAway.x;
            this.posY = safePosAway.y;
            this.posZ = safePosAway.z;
            cir.setReturnValue(true);
        }
        // If safePosAway is null (e.g. cornered), we do not cancel, letting it fall back to vanilla random panic
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"))
    public void hcs$canContinueToUse(@NotNull CallbackInfoReturnable<Boolean> cir) {
        /* `CowKickRevengeGoal` Core Code */
        if (this.mob instanceof IKickCoolDown kicker && !this.mob.isBaby()) {
            kicker.updateCooldown();
            if (kicker.canKick()) {
                LivingEntity attacker = this.mob.getLastHurtByMob();
                if (attacker != null && !EntityHelper.isInLeather(attacker) && this.mob.distanceTo(attacker) < 3.0) {
                    EntityHelper.kickAndFly(this.mob, attacker, 6.0F);
                    kicker.notifyKick();
                }
            }
        }
    }
}