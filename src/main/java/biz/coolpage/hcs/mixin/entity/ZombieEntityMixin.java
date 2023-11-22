package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.entity.goal.AdvancedAvoidSunlightGoal;
import biz.coolpage.hcs.entity.goal.BreakBlockGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity {

    //Also see at MobVisibilityCacheMixin, BreakDoorGoalMixin and TrackTargetGoalMixin
    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    //Use "protected" will crash even the original method has "protected"
    @Inject(method = "initCustomGoals", at = @At("TAIL"))
    public void initCustomGoals(CallbackInfo ci) {
        //Zombies will break blocks when its path is obstructed
        this.targetSelector.add(1, new BreakBlockGoal(this));
        this.targetSelector.add(1, new AdvancedAvoidSunlightGoal(this));
        //Add animal target for adult zombies
        //Prioritize player(s) within 8 blocks in **TrackTargetGoalMixin/shouldContinue()**
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, AnimalEntity.class, false));
    }

    @Inject(method = "burnsInDaylight", at = @At("HEAD"), cancellable = true)
    protected void burnsInDaylight(CallbackInfoReturnable<Boolean> cir) {
        //noinspection ConstantValue
        if ((Object) this instanceof ZombieVillagerEntity) cir.setReturnValue(false);
    }
}