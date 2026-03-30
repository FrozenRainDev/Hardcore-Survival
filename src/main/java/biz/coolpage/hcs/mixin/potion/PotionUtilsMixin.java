package biz.coolpage.hcs.mixin.potion;

import biz.coolpage.hcs.status.HcsEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.PotionUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(PotionUtils.class)
public class PotionUtilsMixin {
    @Inject(at = @At("HEAD"), method = "getColor(Ljava/util/Collection;)I", cancellable = true)
    private static void getColor(@NotNull Collection<MobEffectInstance> effects, CallbackInfoReturnable<Integer> cir) {
        if (effects.size() == 1) {
            MobEffect effect = effects.iterator().next().getEffect();
            if (effect == HcsEffects.RETURN.get())
                cir.setReturnValue(0x22d3f6);
            else if (effect == MobEffects.DIG_SPEED)
                cir.setReturnValue(0x968f00);
        }
    }
}