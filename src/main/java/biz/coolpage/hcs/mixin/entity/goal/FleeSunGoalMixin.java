package biz.coolpage.hcs.mixin.entity.goal;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FleeSunGoal.class)
public class FleeSunGoalMixin {

    @Shadow @Final private Level level;

    @Shadow @Final protected PathfinderMob mob;

    /**
     * Anticipate dawn: intercept the isDay() check.
     * Consider it "day" a bit earlier (from tick 23000) so mobs start reacting before sunrise.
     */
    @ModifyExpressionValue(
            method = "canUse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z")
    )
    private boolean hcsurvival$anticipateDaytime(boolean original) {
        long timeOfDay = this.level.getDayTime() % 24000L;
        // Return true if it's already day, or if it's the pre-dawn period (23000 - 24000)
        return original || timeOfDay >= 23000L;
    }

    /**
     * Anticipate fire: intercept the isOnFire() check.
     * Allow mobs to flee before they catch on fire if the sun is rising or it's already day.
     * The canSeeSky() check later in the vanilla logic will ensure they only flee if exposed.
     */
    @ModifyExpressionValue(
            method = "canUse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/PathfinderMob;isOnFire()Z")
    )
    private boolean hcsurvival$anticipateFire(boolean original) {
        long timeOfDay = this.level.getDayTime() % 24000L;
        // Flee even if not yet on fire to anticipate the sun (pre-dawn or day)
        return original || this.level.isDay() || timeOfDay >= 23000L;
    }

    /**
     * Accelerate the movement speed to 1.5x of the original speed when fleeing the sun.
     * Using MixinExtras @ModifyExpressionValue to intercept the field read instead of @ModifyArg.
     */
    @ModifyExpressionValue(
            method = "start",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/ai/goal/FleeSunGoal;speedModifier:D"
            )
    )
    private double hcsurvival$boostFleeSpeed(double originalSpeed) {
        return originalSpeed * 1.2D;
    }
}