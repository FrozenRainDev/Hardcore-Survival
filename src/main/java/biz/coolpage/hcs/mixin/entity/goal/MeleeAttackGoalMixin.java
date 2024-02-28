package biz.coolpage.hcs.mixin.entity.goal;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public class MeleeAttackGoalMixin {
//    @Inject(method = "getSquaredMaxAttackDistance", at = @At("RETURN"), cancellable = true)
//    protected void getSquaredMaxAttackDistance(@NotNull LivingEntity entity, CallbackInfoReturnable<Double> cir) {
//        float reachAddition = EntityHelper.getReachRangeAddition(entity);
//        System.out.println(entity + " " + reachAddition); // FIXME THE REACH DIST == PLAYER REACH DIST ------ THE PARAM entity IS THE VICTIM
//        if (reachAddition > 0.0F)
//            cir.setReturnValue(cir.getReturnValue() + reachAddition * reachAddition);
//    }
}
