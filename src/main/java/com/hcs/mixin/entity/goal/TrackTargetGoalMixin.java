package com.hcs.mixin.entity.goal;

import com.hcs.util.EntityHelper;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrackTargetGoal.class)
public class TrackTargetGoalMixin {
    @Shadow @Mutable @Final
    protected final MobEntity mob;

    public TrackTargetGoalMixin(MobEntity mob) {
        this.mob = mob;
    }

    @Inject(at = @At("RETURN"), method = "getFollowRange", cancellable = true)
    protected void getFollowRange(@NotNull CallbackInfoReturnable<Double> cir) {
        if (mob instanceof ZombieEntity) cir.setReturnValue(Math.max(cir.getReturnValue(), EntityHelper.ZOMBIE_SENSING_RANGE));
    }

}