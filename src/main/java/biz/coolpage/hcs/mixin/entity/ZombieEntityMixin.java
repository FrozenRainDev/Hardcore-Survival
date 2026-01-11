package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.entity.goal.AdvancedAvoidSunlightGoal;
import biz.coolpage.hcs.entity.goal.BreakBlockGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity {
    // Also see MobVisibilityCacheMixin, BreakDoorGoalMixin and TrackTargetGoalMixin
    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract boolean burnsInDaylight();

    // Using "protected" will crash even the original method is "protected"
    @Inject(method = "initCustomGoals", at = @At("TAIL"))
    public void initCustomGoals(CallbackInfo ci) {
        // Zombies will break blocks when its path is obstructed
        this.targetSelector.add(1, new BreakBlockGoal(this));
        if (this.burnsInDaylight()) this.targetSelector.add(1, new AdvancedAvoidSunlightGoal(this));
        // Add animal target for adult zombies
        // Prioritize player(s) within 8 blocks in **TrackTargetGoalMixin/shouldContinue()**
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, AnimalEntity.class, false) {
            @Override
            public boolean canStart() {
                if (this.mob != null && this.mob.getWorld() instanceof ServerWorld serverWorld)
                    return super.canStart() && Configs.isEnabled(serverWorld, Configs.HOSTILE_ZOMBIE);
                return super.canStart();
            }

            @Override
            public boolean shouldContinue() {
                if (this.mob != null && this.mob.getWorld() instanceof ServerWorld serverWorld)
                    return super.shouldContinue() && Configs.isEnabled(serverWorld, Configs.HOSTILE_ZOMBIE);
                return super.shouldContinue();
            }
        });
    }

    @Inject(method = "burnsInDaylight", at = @At("HEAD"), cancellable = true)
    protected void burnsInDaylight(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ZombieVillagerEntity) cir.setReturnValue(false);
    }
}