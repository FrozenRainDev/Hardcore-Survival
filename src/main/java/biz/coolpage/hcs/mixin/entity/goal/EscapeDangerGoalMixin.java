package biz.coolpage.hcs.mixin.entity.goal;

import biz.coolpage.hcs.status.accessor.IKickCoolDown;
import biz.coolpage.hcs.status.accessor.ILivingEntity;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static biz.coolpage.hcs.util.CommUtil.hasNull;

@Mixin(PanicGoal.class)
public abstract class EscapeDangerGoalMixin {
    @Shadow
    @Final
    @Mutable
    protected final double speedModifier;

    @Shadow
    @Final
    @Mutable
    protected final PathfinderMob mob;
    @Shadow
    protected double posX, posY, posZ;

    public EscapeDangerGoalMixin(double speed, PathfinderMob mob) {
        this.speedModifier = speed;
        this.mob = mob;
    }

    @Unique
    private boolean isAttackerAfar() {
        if (this.mob instanceof ILivingEntity ent) {
            LivingEntity attacker0 = ent.getHcsLastAttacker();
            return attacker0 != null && this.mob.distanceTo(attacker0) > 48;
        }
        return false;
    }

    @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
    protected void canUse(CallbackInfoReturnable<Boolean> cir) {
        if (isAttackerAfar()) cir.setReturnValue(false);
    }

    @ModifyArg(method = "start", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;moveTo(DDDD)Z"), index = 3)
    public double startMixin(double speed) {
        //Improve escaping speed
        if (mob instanceof Cow) speed *= 1.1;
        else if (mob instanceof Chicken) speed *= 1.2;
        else speed *= 1.4;
        return speed;
    }

    @Inject(method = "findRandomPosition", at = @At("HEAD"), cancellable = true)
    protected void findRandomPosition(@NotNull CallbackInfoReturnable<Boolean> cir) {
        var attacker = this.mob.getLastHurtByMob();
        if (attacker == null) return;
        var attackerPos = attacker.position();
        var mobPos = this.mob.position();
        if (hasNull(attackerPos, mobPos)) return;
        double escVectorX = mobPos.x - attackerPos.x, escVectorZ = mobPos.z - attackerPos.z;
        var escPos = DefaultRandomPos.getPosTowards(this.mob, 5, 4, Vec3.atBottomCenterOf(BlockPos.containing(mobPos.x + escVectorX, mobPos.y, mobPos.z + escVectorZ)), 1.5707963705062866);
        if (escPos == null) {
            var escVector = attackerPos.subtract(mobPos);
            escPos = DefaultRandomPos.getPosTowards(this.mob, 5, 4, Vec3.atBottomCenterOf(BlockPos.containing(escVector)), 1.5707963705062866);
        }
        if (escPos == null) {
            cir.setReturnValue(false);
            return;
        }
        this.posX = escPos.x;
        this.posY = escPos.y;
        this.posZ = escPos.z;
        cir.setReturnValue(true);
    }

    @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
    public void canContinueToUse(@NotNull CallbackInfoReturnable<Boolean> cir) {
        /* `CowKickRevengeGoal` Core Code
        Goals with same priority won't run simultaneously,
        if CowKickRevengeGoal runs, EscapingDangerGoal terminates and the entity will lose memory of escaping from its attackers,
        so I mixed such goal's code into EscapingDanger reluctantly. */
        if (this.mob instanceof IKickCoolDown kicker && !this.mob.isBaby()) {
            kicker.updateCooldown();
            if (kicker.canKick()) {
                LivingEntity attacker = this.mob.getLastHurtByMob();
                if (attacker != null && !EntityHelper.isInLeather(attacker) && this.mob.distanceTo(attacker) < 3.0) {
                    EntityHelper.flyOut(this.mob, attacker, 6.0F);
                    kicker.notifyKick();
                }
            }
        }
        // The method stop() won't be called when the return value is false, so I called it manually
        if (isAttackerAfar()) {
            cir.setReturnValue(false);
        }
    }
}
