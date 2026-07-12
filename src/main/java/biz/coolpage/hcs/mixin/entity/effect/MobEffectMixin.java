package biz.coolpage.hcs.mixin.entity.effect;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEffect.class)
public class MobEffectMixin {

    @WrapOperation(
            method = "applyEffectTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"
            )
    )
    private void hcsurvival$modifyFoodExhaustion(@NotNull Player instance, float exhaustion, Operation<Void> original) {
        // Bypass the default causeFoodExhaustion logic and apply exhaustion directly
        instance.getFoodData().addExhaustion(exhaustion);

        // Note: We intentionally do not call original.call(instance, exhaustion)
        // to completely overwrite the vanilla behavior, acting as a safer @Redirect.
    }
}