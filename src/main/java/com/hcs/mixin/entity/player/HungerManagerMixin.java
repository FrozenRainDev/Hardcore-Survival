package com.hcs.mixin.entity.player;

import com.hcs.status.HcsEffects;
import com.hcs.status.accessor.StatAccessor;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @Shadow
    private int foodLevel = 20;
    @Shadow
    private float saturationLevel = 5.0F;
    @Shadow
    private float exhaustion;
    @Shadow
    private int foodTickTimer;
    @Shadow
    private int prevFoodLevel = 20;

    @SuppressWarnings("EmptyMethod")
    @Shadow
    public void addExhaustion(float exhaustion) {
    }

    @Inject(at = @At("HEAD"), method = "addExhaustion", cancellable = true)
    private void addExhaustion(float exhaustion, CallbackInfo ci) {
        if (this.saturationLevel < 0.01F) {
            //Slower hunger rate when foodLevel is low
            float rate = 1.6F;
            if (foodLevel == 2) rate = 0.3F;
            else if (foodLevel <= 4) rate = 0.5F;
            else if (foodLevel <= 6) rate = 0.8F;
            else if (foodLevel <= 8) rate = 1.0F;
            this.exhaustion += exhaustion * rate;
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "update", cancellable = true)
    public void update(@NotNull PlayerEntity player, CallbackInfo cir) {
        Difficulty difficulty = player.world.getDifficulty();
        this.prevFoodLevel = this.foodLevel;
        double thirst = ((StatAccessor) player).getThirstManager().get();
        boolean malnutrition = player.hasStatusEffect(HcsEffects.MALNUTRITION);
        if (difficulty == Difficulty.PEACEFUL) {
            ((StatAccessor) player).getThirstManager().addDirectly(0.01);
            ((StatAccessor) player).getSanityManager().add(0.01);
        }
        if (this.exhaustion > 4.0F) {
            this.exhaustion = 0.0F;
            if (this.saturationLevel > 0.0F) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);
            } else if (difficulty != Difficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }
        boolean bl = player.world.getGameRules().getBoolean(GameRules.NATURAL_REGENERATION);
        if (bl && this.saturationLevel >= 0.0F && player.canFoodHeal() && this.foodLevel >= 19 && thirst >= 0.8) {
            if (!malnutrition || Math.random() < 0.5) ++this.foodTickTimer;
            if (this.foodTickTimer >= 10) {
                float f = Math.min(1.0F + this.saturationLevel / 6.0F, 2.0F) / 100.0F;
                player.heal(f);
                this.addExhaustion(f * 6.0F);
                this.foodTickTimer = 0;
            }
        } else if (bl && this.foodLevel >= 10 && player.canFoodHeal() && thirst >= 0.5) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= (100 * (this.foodLevel >= 14 ? 1 : 2) * (thirst >= 0.7 ? 1 : 1.5))) {
                player.heal(0.1F);
                this.addExhaustion(0.6F);
                this.foodTickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 400) {
                player.damage(player.world.getDamageSources().starve(), 1.0F);
                this.foodTickTimer = 0;
            }
        } else {
            this.foodTickTimer = 0;
        }
        cir.cancel();
    }

}
