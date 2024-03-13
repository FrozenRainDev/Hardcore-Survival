package biz.coolpage.hcs.mixin.entity.goal;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MeleeAttackGoal.class)
public class MeleeAttackGoalMixin {
    @Shadow
    @Final
    protected PathAwareEntity mob;

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/MeleeAttackGoal;attack(Lnet/minecraft/entity/LivingEntity;D)V"), index = 1)
    private double tickAttack(double squaredDistance) {
        double reachRangeAddition = EntityHelper.getReachRangeAddition(this.mob);
        return squaredDistance - reachRangeAddition * reachRangeAddition;
    }
}
