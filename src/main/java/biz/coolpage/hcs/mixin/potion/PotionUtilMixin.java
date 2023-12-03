package biz.coolpage.hcs.mixin.potion;

import biz.coolpage.hcs.status.HcsEffects;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.PotionUtil;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;


@Mixin(PotionUtil.class)
public class PotionUtilMixin {
    @Inject(at = @At("HEAD"), method = "getColor(Ljava/util/Collection;)I", cancellable = true)
    private static void getColor(@NotNull Collection<StatusEffectInstance> effects, CallbackInfoReturnable<Integer> cir) {
        if (effects.size() == 1) {
            StatusEffect effect = effects.iterator().next().getEffectType();
            if (effect == HcsEffects.RETURN)
                cir.setReturnValue(0x22d3f6); //DO NOT delete, otherwise the potion color will become black
            else if (effect == StatusEffects.HASTE) cir.setReturnValue(0x968f00);
        }
    }
}