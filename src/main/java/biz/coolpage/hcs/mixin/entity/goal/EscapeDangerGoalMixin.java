package biz.coolpage.hcs.mixin.entity.goal;

import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EscapeDangerGoal.class)
public class EscapeDangerGoalMixin {
    @Final
    @Shadow
    @Mutable
    protected final double speed;
    @Final
    @Shadow
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

    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    public void start(@NotNull CallbackInfo cir) {
        //Improve escaping speed
        this.mob.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, this.speed * ((mob instanceof CowEntity || mob instanceof ChickenEntity) ? 1.0 : 1.4));
        this.active = true;
        cir.cancel();
    }

}
