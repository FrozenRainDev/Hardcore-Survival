package biz.coolpage.hcs.mixin.entity.goal;

import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

//import static biz.coolpage.hcs.util.CommUtil.applyNullable;

@Mixin(EscapeDangerGoal.class)
public abstract class EscapeDangerGoalMixin {
    @Shadow
    @Final
    @Mutable
    protected final double speed;

    @Shadow
    @Final
    @Mutable
    protected final PathAwareEntity mob;
//    @Shadow
//    protected double targetX, targetY, targetZ;
//
//    @Shadow
//    public abstract void stop();
//
//    @Shadow
//    protected boolean active;

    public EscapeDangerGoalMixin(double speed, PathAwareEntity mob) {
        this.speed = speed;
        this.mob = mob;
    }

//    @Unique
//    private boolean isAttackerAfar() {
//        return applyNullable(this.mob.getAttacker(), attacker -> this.mob.distanceTo(attacker) > 32, true);
//    }

//    @Inject(method = "canStart", at = @At("RETURN"), cancellable = true)
//    protected void canStart(CallbackInfoReturnable<Boolean> cir) {
//        if (isAttackerAfar()) {
//            cir.setReturnValue(false);
////            this.mob.lastAttackedTicks = 0;
////            this.mob.setAttacker(null);
//        }
//    }

    @ModifyArg(method = "start", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/pathing/EntityNavigation;startMovingTo(DDDD)Z"), index = 3)
    public double startMixin(double speed) {
        //Improve escaping speed
        if (mob instanceof ChickenEntity) speed *= 1.2;
        else if (!(mob instanceof CowEntity)) speed *= 1.4;
        return speed;
    }

    /*
    @Inject(method = "findTarget", at = @At("HEAD"), cancellable = true)
    protected void findTarget(@NotNull CallbackInfoReturnable<Boolean> cir) {
        var attacker = this.mob.getAttacker();
        if (attacker == null) return;
        var attackerPos = attacker.getPos();
        var mobPos = this.mob.getPos();
        if (hasNull(attackerPos, mobPos)) return;
        double escVectorX = mobPos.x - attackerPos.x, escVectorZ = mobPos.z - attackerPos.z;
        var escPos = NoPenaltyTargeting.findTo(this.mob, 5, 4, Vec3d.ofBottomCenter(BlockPos.ofFloored(mobPos.x + escVectorX, mobPos.y, mobPos.z + escVectorZ)), 1.5707963705062866);
        if (escPos == null) {
            var escVector = attackerPos.subtract(mobPos);
            escPos = NoPenaltyTargeting.findTo(this.mob, 5, 4, Vec3d.ofBottomCenter(BlockPos.ofFloored(escVector)), 1.5707963705062866);
        }
        if (escPos == null) {
            cir.setReturnValue(false);
            return;
        }
        this.targetX = escPos.x;
        this.targetY = escPos.y;
        this.targetZ = escPos.z;
        cir.setReturnValue(true);
    }

    @Inject(method = "shouldContinue", at = @At("HEAD"), cancellable = true)
    public void shouldContinue(@NotNull CallbackInfoReturnable<Boolean> cir) {
        //The method stop() won't be called when the return value is false, so I called it manually
        if (isAttackerAfar()) {
            this.stop();
            this.active = false;
            cir.setReturnValue(true);
            this.mob.lastAttackedTicks = 0;
            this.mob.setAttacker(null);
        }
    }
    */


}
