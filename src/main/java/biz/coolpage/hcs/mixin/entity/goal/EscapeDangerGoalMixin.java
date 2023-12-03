package biz.coolpage.hcs.mixin.entity.goal;

import biz.coolpage.hcs.status.accessor.IAnimalEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static biz.coolpage.hcs.util.CommUtil.hasNull;

@Mixin(EscapeDangerGoal.class)
public class EscapeDangerGoalMixin {
    @Shadow
    @Final
    @Mutable
    protected final double speed;

    @Shadow
    @Final
    @Mutable
    protected final PathAwareEntity mob;
    @Shadow
    protected double targetX, targetY, targetZ;
    @Shadow
    protected boolean active;

    public EscapeDangerGoalMixin(double speed, PathAwareEntity mob) {
        this.speed = speed;
        this.mob = mob;
    }

    @Inject(method = "isInDanger", at = @At("RETURN"), cancellable = true)
    protected void isInDanger(@NotNull CallbackInfoReturnable<Boolean> cir) {
        if (this.mob instanceof IAnimalEntity ent) {
//            System.out.println(ent.hasInfectedPanic());
            cir.setReturnValue(cir.getReturnValueZ() || ent.hasInfectedPanic());
        }
    }

    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    public void start(@NotNull CallbackInfo cir) {
        //Improve escaping speed
        this.mob.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed * ((mob instanceof CowEntity || mob instanceof ChickenEntity) ? 1.0 : 1.4));
        this.active = true;
        cir.cancel();
    }

    @Inject(method = "findTarget", at = @At("HEAD"), cancellable = true)
    protected void findTarget(@NotNull CallbackInfoReturnable<Boolean> cir) {
        var attacker = this.mob.getAttacker();
        if (attacker == null || hasNull(attacker.getPos(), this.mob.getPos())) return;
        var escVector = attacker.getPos().subtract(this.mob.getPos());
        var escPos = NoPenaltyTargeting.findTo(this.mob, 5, 4, Vec3d.ofBottomCenter(BlockPos.ofFloored(this.mob.getPos().add(escVector))), 1.5707963705062866);
        if (escPos == null) {
            cir.setReturnValue(false);
            return;
        }
        this.targetX = escPos.x;
        this.targetY = escPos.y;
        this.targetZ = escPos.z;
        cir.setReturnValue(true);
    }


}
