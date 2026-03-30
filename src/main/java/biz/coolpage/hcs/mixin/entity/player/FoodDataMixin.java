package biz.coolpage.hcs.mixin.entity.player;

import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StatusManager;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static biz.coolpage.hcs.config.Configs.SLOW_HEAL;

@Mixin(FoodData.class)
// HungerManagerMixin
public abstract class FoodDataMixin {
    @Shadow
    private int foodLevel = 20;
    @Shadow
    private float saturationLevel = 5.0F;
    @Shadow
    private float exhaustionLevel;
    @Shadow
    private int tickTimer;
    @Shadow
    private int lastFoodLevel = 20;

    @Shadow
    public abstract void addExhaustion(float exhaustion);

    @Unique
    private static float adjustHealingAmount(@NotNull Player player, float f) {
        if (player.hasEffect(HcsEffects.COLD.get())) f *= 0.65F;
        if (player.hasEffect(HcsEffects.UNHAPPY.get())) f *= 0.75F;
        // bad code; malnutrition see method "update"
        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
        if (statusManager.getBandageWorkTicks() > 0) f *= 1.5F;
        return f;
    }

    @Inject(at = @At("HEAD"), method = "addExhaustion", cancellable = true)
    private void addExhaustion(float exhaustion, CallbackInfo ci) {
        if (this.saturationLevel < 0.01F) {
            //Slower hunger rate when foodLevel is low
            float rate = 1.0F;
            if (foodLevel == 2) rate = 0.3F;
            else if (foodLevel <= 4) rate = 0.5F;
            else if (foodLevel <= 6) rate = 0.6F;
            else if (foodLevel <= 8) rate = 0.8F;
            this.exhaustionLevel += exhaustion * rate;
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void update(@NotNull Player player, CallbackInfo ci) {
        if (((StatAccessor) player).getConfigManager().get(SLOW_HEAL)) {
            Difficulty difficulty = player.level().getDifficulty();
            this.lastFoodLevel = this.foodLevel;
            double thirst = ((StatAccessor) player).getThirstManager().get();
            boolean malnutrition = player.hasEffect(HcsEffects.MALNUTRITION.get());
            if (difficulty == Difficulty.PEACEFUL) {
                ((StatAccessor) player).getThirstManager().addDirectly(0.01);
                ((StatAccessor) player).getSanityManager().add(0.01);
            }
            if (this.saturationLevel > 3.0F) this.saturationLevel = 3.0F;
            if (this.exhaustionLevel > 4.0F) {
                this.exhaustionLevel = 0.0F;
                if (this.saturationLevel > 0.0F) {
                    this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);
                } else if (difficulty != Difficulty.PEACEFUL) {
                    this.foodLevel = Math.max(this.foodLevel - 1, 0);
                }
            }
            boolean bl = player.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
            // 修正点：player.canFoodHeal() -> player.isHurt()
            if (bl && this.saturationLevel >= 0.0F && player.isHurt() && this.foodLevel >= 19 && thirst >= 0.8 && !player.hasEffect(HcsEffects.BLEEDING.get())) {
                if (!malnutrition || Math.random() < 0.5) ++this.tickTimer;
                if (this.tickTimer >= 10) {
                    float f = Math.min(1.0F + this.saturationLevel / 6.0F, 2.0F) / 100.0F;
                    f = adjustHealingAmount(player, f);
                    player.heal(f);
                    this.addExhaustion(f * 6.0F);
                    this.tickTimer = 0;
                }
            } else if (bl && this.foodLevel >= 10 && player.isHurt() && thirst >= 0.5) {
                ++this.tickTimer;
                if (this.tickTimer >= (100 * (this.foodLevel >= 14 ? 1 : 2) * (thirst >= 0.7 ? 1 : 1.5))) {
                    float f = 0.1F;
                    f = adjustHealingAmount(player, f);
                    player.heal(f);
                    this.addExhaustion(0.6F);
                    this.tickTimer = 0;
                }
            } else if (this.foodLevel <= 0) {
                ++this.tickTimer;
                if (this.tickTimer >= 400) {
                    player.hurt(player.level().damageSources().starve(), 1.0F);
                    this.tickTimer = 0;
                }
            } else {
                this.tickTimer = 0;
            }
            ci.cancel();
        }
    }
}