package biz.coolpage.hcs.mixin.entity.goal;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Zombie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetGoal.class)
public abstract class TrackTargetGoalMixin {
    @Shadow
    @Mutable
    @Final
    protected final Mob mob;

    @Shadow
    @Nullable
    protected LivingEntity targetMob;

    @Shadow
    public abstract void stop();

    public TrackTargetGoalMixin(Mob mob) {
        this.mob = mob;
    }

    @Inject(at = @At("RETURN"), method = "getFollowDistance", cancellable = true)
    protected void getFollowDistance(@NotNull CallbackInfoReturnable<Double> cir) {
        if (mob instanceof Zombie)
            cir.setReturnValue(Math.max(cir.getReturnValue(), EntityHelper.ZOMBIE_SENSING_RANGE));
    }

    @Inject(at = @At("HEAD"), method = "canContinueToUse", cancellable = true)
    public void canContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        if (this.mob instanceof Zombie && this.mob.getTarget() /*this.targetMob is ALWAYS null!!*/ instanceof Animal && this.mob.level().getNearestPlayer(this.mob.getX(), this.mob.getY(), this.mob.getZ(), 16.0, true) != null) {
            this.stop();
            cir.setReturnValue(false);
        }
    }

}
