package biz.coolpage.hcs.mixin.entity.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StatusEffect.class)
public class StatusEffectMixin {
    @Redirect(method = "applyUpdateEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V"))
    private void applyUpdateEffectMixin(@NotNull PlayerEntity player, float exhaustion) {
        player.getHungerManager().addExhaustion(exhaustion);
    }
}