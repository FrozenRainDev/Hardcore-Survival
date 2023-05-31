package com.hcs.mixin.entity;

import com.hcs.entity.goal.BreakBlockGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity {
    @Shadow
    public abstract boolean isBaby();

    //Also see at MobVisibilityCacheMixin, BreakDoorGoalMixin and TrackTargetGoalMixin
    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    //Use "protected" will crash even the original method is modified by "protected"
    @Inject(method = "initCustomGoals", at = @At("TAIL"))
    public void initCustomGoals(CallbackInfo ci) {
        //Zombies will break blocks when its path is obstructed
        this.targetSelector.add(1, new BreakBlockGoal(this));
        //Zombies will attack animals spontaneously
        if (!this.isBaby()) this.targetSelector.add(5, new ActiveTargetGoal<>(this, AnimalEntity.class, false));
    }

    /*
    @Inject(method = "damage", at = @At("HEAD"))
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        //On death
        if ((this.getHealth() - amount) <= 0.0F && this.getHealth() > 0.0F) {
            EntityHelper.dropItem(this, Items.BONE, 1 + (int) Math.round(Math.random()));
        }
    }
     */
}