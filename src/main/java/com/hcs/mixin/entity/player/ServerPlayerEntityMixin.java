package com.hcs.mixin.entity.player;

import com.hcs.main.helper.EntityHelper;
import com.hcs.main.helper.TemperatureHelper;
import com.hcs.main.manager.StaminaManager;
import com.hcs.main.manager.TemperatureManager;
import com.hcs.main.manager.ThirstManager;
import com.hcs.misc.HcsEffects;
import com.hcs.misc.accessor.DamageSourcesAccessor;
import com.hcs.misc.accessor.StatAccessor;
import com.hcs.misc.network.ServerS2C;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ShieldItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.hcs.main.manager.TemperatureManager.CHANGE_SPAN;
import static com.hcs.misc.network.ServerS2C.floatToInt;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow
    public abstract boolean damage(DamageSource source, float amount);

    @Shadow
    public abstract ServerWorld getWorld();

    @Shadow
    public abstract boolean isCreative();

    @Shadow
    public abstract boolean isSpectator();

    /*
    @Inject(method = "getSpawnPointPosition", at = @At("HEAD"))
    See at main.event.ServerPlayerEvent
    */

    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {
        TemperatureHelper.updateAmbientBlocks(this);
        TemperatureHelper.getTemp(this); //Update temp cache
        ServerS2C.writeS2CPacket(this);
        if (!(this.isCreative() || this.isSpectator())) {
            //Init variables
            if (this.hasStatusEffect(HcsEffects.OVEREATEN) && this.hungerManager.getFoodLevel() < 20)
                this.removeStatusEffect(HcsEffects.OVEREATEN);
            RegistryEntry<Biome> biomeEntry = this.world.getBiome(this.getBlockPos());
            Biome biome = biomeEntry.value();
            String biomeName = TemperatureHelper.getBiomeName(biomeEntry);
            ThirstManager thirstManager = ((StatAccessor) this).getThirstManager();
            TemperatureManager temperatureManager = ((StatAccessor) this).getTemperatureManager();
            StaminaManager staminaManager = ((StatAccessor) this).getStaminaManager();
            float playerTemp = temperatureManager.get(), tempSatuPercent = temperatureManager.getSaturationPercentage();
            float envTempReal = temperatureManager.getEnvTempCache();
            int skyLightLevel = this.world.getLightLevel(LightType.SKY, this.getBlockPos().up())/*, darkness = this.world.getAmbientDarkness()*/, sunshineIntensity = TemperatureHelper.getSunshineIntensityLevel(this.world.getLunarTime(), this.world.isRaining(), biomeName), windchillLevel = TemperatureHelper.getWindchillLevel(this.world, this.getBlockPos(), envTempReal, biomeEntry);
            float envTemp = TemperatureHelper.getFeelingTemp(this, envTempReal, biomeName, skyLightLevel);
            float span1 = CHANGE_SPAN * ((playerTemp > 0.75F) ? 0.3F : 1.0F), span2 = -CHANGE_SPAN * ((playerTemp < 0.25F) ? 0.3F : 1.0F);

            //Players' temp affected by environment and change
            if (this.inPowderSnow) {
                if (this.isFrozen()) temperatureManager.add(-1.0F);
                else temperatureManager.set(Math.max(0.01F, temperatureManager.get() - 0.005F));
            } else if (playerTemp > 0.9F && this.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))
                temperatureManager.set(0.9F);
            else if (this.getFireTicks() > 1) temperatureManager.add(0.003F);
            else if (envTemp - playerTemp > span1) {
                temperatureManager.setTrendType(1);
                temperatureManager.add(span1);
            } else if (envTemp - playerTemp < span2) {
                temperatureManager.setTrendType(-1);
                temperatureManager.add(span2);
            } else {
                temperatureManager.setTrendType(0);
                temperatureManager.set(envTemp);
            }

            //Debuff for poor condition & heat for doing sport
            float currentStaminaVal = staminaManager.get();
            if (floatToInt(thirstManager.get()) <= 10 && this.world.getTime() % 400 == 0) {// ticks will cause different players Cannot be hurt at the same time
                DamageSource damageSource = ((DamageSourcesAccessor) this.world.getDamageSources()).dehydrate();
                if (damageSource != null) this.damage(damageSource, 1.0F);
            }
            thirstManager.updateThirstRateAffectedByTemp(envTemp, playerTemp);
            if (thirstManager.get() <= 0.3F) EntityHelper.addHcsDebuff(this, HcsEffects.DEHYDRATED, 0);
            if (this.getHungerManager().getFoodLevel() <= 6) EntityHelper.addHcsDebuff(this, HcsEffects.STARVING, 0);
            if (currentStaminaVal <= 0.3F)
                EntityHelper.addHcsDebuff(this, HcsEffects.EXHAUSTED, currentStaminaVal <= 0.15F ? 1 : 0);
            if (playerTemp >= 1.0F) EntityHelper.addHcsDebuff(this, HcsEffects.HEATSTROKE, (int) tempSatuPercent);
            else if (playerTemp <= 0.0F) EntityHelper.addHcsDebuff(this, HcsEffects.HYPOTHERMIA, (int) tempSatuPercent);
            if (this.isUsingItem() && (this.getMainHandStack().getItem() instanceof ShieldItem || this.getOffHandStack().getItem() instanceof ShieldItem))
                staminaManager.pauseRestoring();
            //Debuff of strong_sun & chilly_wind
            if ((biome.weather.downfall() <= 0.0F || TemperatureHelper.isSpecialSunshineArea(biomeName)) && sunshineIntensity > 0 && skyLightLevel >= 15)
                EntityHelper.addHcsDebuff(this, HcsEffects.STRONG_SUN, Math.max(0, sunshineIntensity - 1));
            if (biome.isCold(this.getBlockPos()) && windchillLevel > 0)
                EntityHelper.addHcsDebuff(this, HcsEffects.CHILLY_WIND, windchillLevel - 1);
        }
    }
}
