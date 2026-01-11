package biz.coolpage.hcs.mixin.item;

import static biz.coolpage.hcs.config.Configs.*;

import biz.coolpage.hcs.status.HcsEffects;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.effect.StatusEffectInstance;


import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FoodComponent.class)
public class FoodComponentMixin {
    @Inject(method = "isAlwaysEdible", at = @At("HEAD"), cancellable = true)
    public void isAlwaysEdible(@NotNull CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Unique
    private static boolean isFoodPoisoningEffect(StatusEffectInstance effect) {
        if (effect == null) return false;
        var type = effect.getEffectType();
        return type == StatusEffects.POISON /*|| type == StatusEffects.HUNGER*/ || type == StatusEffects.NAUSEA
                || type == HcsEffects.FOOD_POISONING || type == HcsEffects.DIARRHEA;
    }

    @Inject(method = "getStatusEffects", at = @At("RETURN"), cancellable = true)
    public void getStatusEffects(CallbackInfoReturnable<List<Pair<StatusEffectInstance, Float>>> cir) {
        if (!isEnabled(FOOD_POISON)) {
            var effects = cir.getReturnValue();
            effects = effects.stream().filter(effect -> {
                if (effect == null) return true;
                return !isFoodPoisoningEffect(effect.getFirst());
            }).toList();
            cir.setReturnValue(effects);
        }
    }
}
