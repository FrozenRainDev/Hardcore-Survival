package biz.coolpage.hcs.mixin.entity.goal;

import biz.coolpage.hcs.util.EntityHelper;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Zombie;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetGoal.class)
// TrackTargetGoalMixin
public abstract class TargetGoalMixin {
    @Shadow
    @Mutable
    @Final
    protected final Mob mob;

    @Shadow
    public abstract void stop();

    public TargetGoalMixin(Mob mob) {
        this.mob = mob;
    }

    // Use MixinExtras @ModifyReturnValue instead of @Inject at RETURN
    @ModifyReturnValue(at = @At("RETURN"), method = "getFollowDistance")
    protected double getFollowDistance(double original) {
        if (mob instanceof Zombie) return Math.max(original, EntityHelper.MOB_BASE_SENSING_RANGE);
        return original;
    }

    // @Inject at HEAD is still the best practice for early cancellation with side effects
    @Inject(at = @At("HEAD"), method = "canContinueToUse", cancellable = true)
    public void canContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        if (this.mob instanceof Zombie
                && this.mob.getTarget() /*this.targetMob is ALWAYS null!!*/ instanceof Animal
                && this.mob.level().getNearestPlayer(this.mob.getX(), this.mob.getY(), this.mob.getZ(), 16.0, true) != null) {
            this.stop();
            cir.setReturnValue(false);
        }
    }

}