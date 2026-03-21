package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.entity.goal.StaringRevengeGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombifiedPiglin.class)
public abstract class ZombifiedPiglinMixin extends Zombie implements NeutralMob { // ZombifiedPiglinEntityMixin
    public ZombifiedPiglinMixin(EntityType<? extends Zombie> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "addBehaviourGoals", at = @At("TAIL"))
    protected void addBehaviourGoals(CallbackInfo ci) {
        this.targetSelector.addGoal(2, new StaringRevengeGoal(this, this::isAngryAt));
    }
}