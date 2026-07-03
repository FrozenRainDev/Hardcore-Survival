package biz.coolpage.hcs.mixin.entity.goal;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MeleeAttackGoal.class)
public class MeleeAttackGoalMixin {
    @Shadow
    @Final
    protected PathfinderMob mob;

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;checkAndPerformAttack(Lnet/minecraft/world/entity/LivingEntity;D)V"), index = 1)
    private double tickAttack(double squaredDistance) {
        LivingEntity target = this.mob.getTarget();

        // If there is a wall blocking all line-of-sight paths, fail the attack distance check
        if (target != null && !EntityHelper.canHitEntity(this.mob, target)) {
            return Double.MAX_VALUE;
        }

        double reachRangeAddition = EntityHelper.getReachRangeAddition(this.mob);
        return squaredDistance - reachRangeAddition * reachRangeAddition;
    }
}