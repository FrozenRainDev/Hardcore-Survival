package biz.coolpage.hcs.mixin.entity.goal;

import net.minecraft.entity.ai.goal.BreakDoorGoal;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BreakDoorGoal.class)
public class BreakDoorGoalMixin {
    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    public void canStart(@NotNull CallbackInfoReturnable<Boolean> cir) {
        //This goal was disabled, as it conflicts with BreakBlockGoal
        cir.setReturnValue(false);
    }
}
