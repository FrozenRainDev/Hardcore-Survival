package biz.coolpage.hcs.mixin.entity.goal;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrackTargetGoal.class)
public abstract class TrackTargetGoalMixin {
    @Shadow
    @Mutable
    @Final
    protected final MobEntity mob;

    @Shadow
    @Nullable
    protected LivingEntity target;

    @Shadow
    public abstract void stop();

    public TrackTargetGoalMixin(MobEntity mob) {
        this.mob = mob;
    }

    @Inject(at = @At("RETURN"), method = "getFollowRange", cancellable = true)
    protected void getFollowRange(@NotNull CallbackInfoReturnable<Double> cir) {
        if (mob instanceof ZombieEntity)
            cir.setReturnValue(Math.max(cir.getReturnValue(), EntityHelper.ZOMBIE_SENSING_RANGE));
    }

    @Inject(at = @At("HEAD"), method = "shouldContinue", cancellable = true)
    public void shouldContinue(CallbackInfoReturnable<Boolean> cir) {
        if (this.mob instanceof ZombieEntity && this.mob.getTarget() /*this.target is ALWAYS null!!*/ instanceof AnimalEntity && this.mob.world.getClosestPlayer(this.mob.getX(), this.mob.getY(), this.mob.getZ(), 16.0, true) != null) {
            this.stop();
            cir.setReturnValue(false);
        }
    }

}