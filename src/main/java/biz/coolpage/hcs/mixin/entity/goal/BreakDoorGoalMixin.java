package biz.coolpage.hcs.mixin.entity.goal;

import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BreakDoorGoal.class)
public class BreakDoorGoalMixin {
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    public void canUse(@NotNull CallbackInfoReturnable<Boolean> cir) {
        // This goal is disabled, because it conflicts with customized BreakBlockGoal
        cir.setReturnValue(false);
    }
}
