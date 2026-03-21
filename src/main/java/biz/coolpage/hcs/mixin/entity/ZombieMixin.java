package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.entity.goal.AdvancedAvoidSunlightGoal;
import biz.coolpage.hcs.entity.goal.BreakBlockGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieMixin extends Monster { // ZombieEntityMixin
    // Also see MobVisibilityCacheMixin, BreakDoorGoalMixin and TrackTargetGoalMixin
    protected ZombieMixin(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    @Shadow
    public abstract boolean isSunSensitive();

    // Using "protected" will crash even the original method is "protected"
    @Inject(method = "addBehaviourGoals", at = @At("TAIL"))
    public void addBehaviourGoals(CallbackInfo ci) {
        // Zombies will break blocks when its path is obstructed
        this.targetSelector.addGoal(1, new BreakBlockGoal(this));
        if (this.isSunSensitive()) this.targetSelector.addGoal(1, new AdvancedAvoidSunlightGoal(this));
        // Add animal target for adult zombies
        // Prioritize player(s) within 8 blocks in **TrackTargetGoalMixin/shouldContinue()**
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Animal.class, false) {
            @Override
            public boolean canUse() {
                if (this.mob == null) return false;
                if (this.mob.isBaby() || this.mob.getVehicle() instanceof Animal) return false;
                if (this.mob.level() instanceof ServerLevel serverWorld)
                    return super.canUse() && Configs.isEnabled(serverWorld, Configs.HOSTILE_ZOMBIE);
                return super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                if (this.mob != null && this.mob.level() instanceof ServerLevel serverWorld)
                    return super.canContinueToUse() && Configs.isEnabled(serverWorld, Configs.HOSTILE_ZOMBIE);
                return super.canContinueToUse();
            }
        });
    }

    @Inject(method = "isSunSensitive", at = @At("HEAD"), cancellable = true)
    protected void isSunSensitive(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ZombieVillager) cir.setReturnValue(false);
    }
}
