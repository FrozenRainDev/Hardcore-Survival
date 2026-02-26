package biz.coolpage.hcs.mixin.entity.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static biz.coolpage.hcs.status.HcsEffects.IS_EFFECT_NAME_VARIABLE;
import static biz.coolpage.hcs.status.HcsEffects.getEffectVarName;

@Mixin(MobEffectUtil.class)
public class StatusEffectUtilMixin {
    @Inject(method = "formatDuration", at = @At("HEAD"), cancellable = true)
    private static void formatDuration(MobEffectInstance instance, float multiplier, CallbackInfoReturnable<Component> cir) {
        if (instance == null) return;
        String key = instance.getDescriptionId();
        if (key.contains("effect.hcs.") && instance.getDuration() <= 210 && instance.getDuration() > 201) {
            String descriptionKey;
            if (IS_EFFECT_NAME_VARIABLE.test(instance.getEffect()))
                descriptionKey = getEffectVarName(key, instance.getAmplifier());
            else descriptionKey = key;
            descriptionKey += ".description";
            MutableComponent description = Component.translatable(descriptionKey);
            if (!description.getString().equals(descriptionKey)) cir.setReturnValue(description);
        }
    }
}
