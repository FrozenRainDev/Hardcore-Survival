package com.hcs.mixin.entity.effect;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectUtil.class)
public class StatusEffectUtilMixin {
    @Inject(method = "durationToString", at = @At("HEAD"), cancellable = true)
    private static void durationToString(StatusEffectInstance instance, float multiplier, CallbackInfoReturnable<Text> cir) {
        if (instance == null) return;
        if (instance.getTranslationKey().contains("effect.hcs.") && instance.getDuration() <= 210 && instance.getDuration() > 201) {
            String descriptionKey = instance.getTranslationKey() + ".description";
            MutableText description = Text.translatable(descriptionKey);
            if (!description.getString().equals(descriptionKey)) cir.setReturnValue(description);
        }
    }
}
