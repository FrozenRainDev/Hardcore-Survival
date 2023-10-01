package com.hcs.mixin.entity.player;

import com.hcs.status.HcsEffects;
import com.hcs.status.accessor.DamageSourcesAccessor;
import com.hcs.status.accessor.StatAccessor;
import com.hcs.status.manager.PainManager;
import com.hcs.status.manager.StaminaManager;
import com.hcs.status.manager.TemperatureManager;
import com.hcs.status.manager.ThirstManager;
import com.hcs.status.network.ServerS2C;
import com.hcs.util.EntityHelper;
import com.hcs.util.TemperatureHelper;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.hcs.status.manager.TemperatureManager.CHANGE_SPAN;
import static com.hcs.status.network.ServerS2C.doubleToInt;

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
            double playerTemp = temperatureManager.get(), tempSatuPercent = temperatureManager.getSaturationPercentage();
            float envTempReal = temperatureManager.getEnvTempCache();
            int skyLightLevel = this.world.getLightLevel(LightType.SKY, this.getBlockPos().up())/*, darkness = this.world.getAmbientDarkness()*/, sunshineIntensity = TemperatureHelper.getSunshineIntensityLevel(this.world.getLunarTime(), this.world.isRaining(), biomeName), windchillLevel = TemperatureHelper.getWindchillLevel(this.world, this.getBlockPos(), envTempReal, biomeEntry);
            float envTemp = TemperatureHelper.getFeelingTemp(this, envTempReal, biomeName, skyLightLevel);
            double span1 = CHANGE_SPAN * ((playerTemp > 0.75) ? 0.3 : 1.0), span2 = -CHANGE_SPAN * ((playerTemp < 0.25) ? 0.3 : 1.0);

            //Players' temp affected by environment and change
            if (this.inPowderSnow) {
                if (this.isFrozen()) temperatureManager.add(-1.0);
                else temperatureManager.set(Math.max(0.01, temperatureManager.get() - 0.005));
            } else if (playerTemp > 0.9F && this.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))
                temperatureManager.set(0.9);
            else if (this.getFireTicks() > 1) temperatureManager.add(0.002);
            else if (temperatureManager.getAmbientCache() > 1.5F) temperatureManager.add(0.0005);
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

            //Debuff of poor condition & heat for doing sport
            double currentStaminaVal = staminaManager.get();
            if (doubleToInt(thirstManager.get()) <= 10 && this.world.getTime() % 400 == 0) {// ticks will cause different players Cannot be hurt at the same time
                DamageSource damageSource = ((DamageSourcesAccessor) this.world.getDamageSources()).dehydrate();
                if (damageSource != null) this.damage(damageSource, 1.0F);
            }
            thirstManager.updateThirstRateAffectedByTemp(envTemp, (float) playerTemp);
            if (thirstManager.get() <= 0.3) EntityHelper.addHcsDebuff(this, HcsEffects.DEHYDRATED);
            if (this.getHungerManager().getFoodLevel() <= 6) EntityHelper.addHcsDebuff(this, HcsEffects.STARVING);
            if (currentStaminaVal <= 0.3)
                EntityHelper.addHcsDebuff(this, HcsEffects.EXHAUSTED, currentStaminaVal <= 0.15 ? 1 : 0);
            if (playerTemp >= 1.0) EntityHelper.addHcsDebuff(this, HcsEffects.HEATSTROKE, (int) tempSatuPercent);
            else if (playerTemp <= 0.0) EntityHelper.addHcsDebuff(this, HcsEffects.HYPOTHERMIA, (int) tempSatuPercent);
            if (this.isUsingItem() && (this.getMainHandStack().getItem() instanceof ShieldItem || this.getOffHandStack().getItem() instanceof ShieldItem))
                staminaManager.pauseRestoring();

            //Debuff of strong sun & chilly wind
            if ((biome.weather.downfall() <= 0.0F || TemperatureHelper.isSpecialSunshineArea(biomeName)) && sunshineIntensity > 0 && skyLightLevel >= 15)
                EntityHelper.addHcsDebuff(this, HcsEffects.STRONG_SUN, Math.max(0, sunshineIntensity - 1));
            if (biome.isCold(this.getBlockPos()) && windchillLevel > 0)
                EntityHelper.addHcsDebuff(this, HcsEffects.CHILLY_WIND, windchillLevel - 1);

            //Debuff of insanity
            double san = ((StatAccessor) this).getSanityManager().get();
            if (san < 0.3)
                EntityHelper.addHcsDebuff(this, HcsEffects.INSANITY, san < 0.15 ? (san < 0.1 ? (san < 0.05 ? 3 : 2) : 1) : 0);
            //Debuff of malnutrition
            double vegetable = ((StatAccessor) this).getNutritionManager().getVegetable();
            if (vegetable < 0.00001) EntityHelper.addHcsDebuff(this, HcsEffects.MALNUTRITION);
            //Debuff of wet
            double wet = ((StatAccessor) this).getWetnessManager().get();
            if (wet >= 0.3) EntityHelper.addHcsDebuff(this, HcsEffects.WET, wet > 0.7 ? 1 : 0);
            //Debuff for soul impaired(death punishment)
            int soulImpairedStat = ((StatAccessor) this).getStatusManager().getSoulImpairedStat();
            if (soulImpairedStat > 0) {
                if (this.getHealth() > this.getMaxHealth()) this.setHealth(this.getMaxHealth());
                EntityHelper.addHcsDebuff(this, HcsEffects.SOUL_IMPAIRED, soulImpairedStat - 1);
            }

            //Debuff of injury
            float hpPercent = this.getHealth() / this.getMaxHealth();
            if (hpPercent < 0.1F) EntityHelper.addHcsDebuff(this, HcsEffects.INJURY, 3);
            else if (hpPercent < 0.25F) EntityHelper.addHcsDebuff(this, HcsEffects.INJURY, 2);
            else if (hpPercent < 0.5F) EntityHelper.addHcsDebuff(this, HcsEffects.INJURY, 1);
            else if (hpPercent < 0.7F) EntityHelper.addHcsDebuff(this, HcsEffects.INJURY, 0);

            //Debuff of pain
            PainManager painManager = ((StatAccessor) this).getPainManager();
            double pain = painManager.get();
            if (pain > 0.0) EntityHelper.addHcsDebuff(this, HcsEffects.PAIN, (int) pain);
            painManager.tick();
        }
    }

    @Inject(method = "damage", at = @At("HEAD"))
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        float hurtPercent = amount / this.getMaxHealth();
        ((StatAccessor) this).getPainManager().add(hurtPercent * 5.0);
    }
}