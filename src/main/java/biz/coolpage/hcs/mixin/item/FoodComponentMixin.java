package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.status.HcsEffects;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static biz.coolpage.hcs.config.Configs.*;

// FoodComponent -> FoodProperties (Mojang)
@Mixin(FoodProperties.class)
public class FoodComponentMixin {

    // isAlwaysEdible -> canAlwaysEat (Mojang)
    @Inject(method = "canAlwaysEat", at = @At("HEAD"), cancellable = true)
    public void canAlwaysEat(@NotNull CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Unique
    private static boolean isFoodPoisoningEffect(MobEffectInstance effect) {
        if (effect == null) return false;
        // getEffectType -> getEffect (Mojang)
        var type = effect.getEffect();
        return type == MobEffects.POISON || type == MobEffects.CONFUSION
                || type == HcsEffects.FOOD_POISONING || type == HcsEffects.DIARRHEA;
    }

    // getStatusEffects -> getEffects (Mojang)
    @Inject(method = "getEffects", at = @At("RETURN"), cancellable = true)
    public void getEffects(CallbackInfoReturnable<List<Pair<MobEffectInstance, Float>>> cir) {
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