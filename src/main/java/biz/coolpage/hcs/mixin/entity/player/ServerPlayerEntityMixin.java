package biz.coolpage.hcs.mixin.entity.player;

import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.DamageSourcesAccessor;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.*;
import biz.coolpage.hcs.status.network.ServerS2C;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.TemperatureHelper;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ShieldItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        if (!EntityHelper.IS_SURVIVAL_LIKE.test(this)) return;
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
        double span1 = TemperatureManager.CHANGE_SPAN * ((playerTemp > 0.75) ? 0.3 : 1.0), span2 = -TemperatureManager.CHANGE_SPAN * ((playerTemp < 0.25) ? 0.3 : 1.0);
        final long currTime = this.world.getTime();

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
        final double currStamina = staminaManager.get();
        if (ServerS2C.dtoi(thirstManager.get()) <= 10 && currTime % 400 == 0) {// ticks will cause different players Cannot be hurt at the same time
            DamageSource damageSource = ((DamageSourcesAccessor) this.world.getDamageSources()).dehydrate();
            if (damageSource != null) this.damage(damageSource, 1.0F);
        }
        thirstManager.updateThirstRateAffectedByTemp(envTemp, (float) playerTemp);
        if (thirstManager.get() <= 0.3) EntityHelper.addHcsDebuff(this, HcsEffects.DEHYDRATED);
        if (this.getHungerManager().getFoodLevel() <= 6) EntityHelper.addHcsDebuff(this, HcsEffects.STARVING);
        if (currStamina <= 0.3)
            EntityHelper.addHcsDebuff(this, HcsEffects.EXHAUSTED, currStamina <= 0.15 ? 1 : 0);
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
        SanityManager sanityManager = ((StatAccessor) this).getSanityManager();
        final double currSan = sanityManager.get();
        if (currSan < 0.3) {
            EntityHelper.addHcsDebuff(this, HcsEffects.INSANITY, currSan < 0.15 ? (currSan < 0.1 ? (currSan < 0.05 ? 3 : 2) : 1) : 0);
            if (currSan < 0.05)
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 10, 0, false, false, false));
        }
        //Debuff of malnutrition
        double vegetable = ((StatAccessor) this).getNutritionManager().getVegetable();
        if (vegetable < 0.00001) EntityHelper.addHcsDebuff(this, HcsEffects.MALNUTRITION);
        //Debuff of wet
        double wet = ((StatAccessor) this).getWetnessManager().get();
        if (wet > 0.72) EntityHelper.addHcsDebuff(this, HcsEffects.WET, 2);
        if (wet > 0.4) EntityHelper.addHcsDebuff(this, HcsEffects.WET, 1);
        if (wet > 0.1) EntityHelper.addHcsDebuff(this, HcsEffects.WET, 0);
        //Debuff for soul impaired(death punishment)
        StatusManager statusManager = ((StatAccessor) this).getStatusManager();
        int soulImpairedStat = statusManager.getSoulImpairedStat();
        if (soulImpairedStat > 0) {
            if (this.getHealth() > this.getMaxHealth()) this.setHealth(this.getMaxHealth());
            EntityHelper.addHcsDebuff(this, HcsEffects.SOUL_IMPAIRED, soulImpairedStat - 1);
        }

        //Debuff of injury
        float hpPercent = this.getHealth() / this.getMaxHealth();
        if (hpPercent < 0.1F) EntityHelper.addHcsDebuff(this, HcsEffects.INJURY, 3);
        else if (hpPercent < 0.25F) EntityHelper.addHcsDebuff(this, HcsEffects.INJURY, 2);
        else if (hpPercent < 0.45F) EntityHelper.addHcsDebuff(this, HcsEffects.INJURY, 1);
        else if (hpPercent < 0.7F) EntityHelper.addHcsDebuff(this, HcsEffects.INJURY, 0);

        //Debuff of pain (view add pain in PlayerEntityMixin/applyDamage)
        InjuryManager injuryManager = ((StatAccessor) this).getInjuryManager();
        final double pain = injuryManager.getRealPain();
        if (pain > 0.0) EntityHelper.addHcsDebuff(this, HcsEffects.PAIN, MathHelper.clamp((int) pain, 0, 3));
        injuryManager.tick();
        sanityManager.tickEnemies(this);

        //Debuff of bleeding
        final double bleeding = injuryManager.getBleeding() - 1.0;
        if (bleeding > 0.0)
            EntityHelper.addHcsDebuff(this, HcsEffects.BLEEDING, MathHelper.clamp((int) bleeding, 0, 3));

        //Panic; also view PlayerEntityMixin/tick()
        MoodManager moodManager = ((StatAccessor) this).getMoodManager();
        final boolean isDarkEnv = this.hasStatusEffect(HcsEffects.DARKNESS_ENVELOPED);
        final double currRawPanic = moodManager.getRawPanic(), currRealPanic = moodManager.getRealPanic(), expectedRawPanic = isDarkEnv ? 4 : MathHelper.clamp(sanityManager.countEnemies() * 0.5, 0.0, 4), panicDiff = Math.abs(currRawPanic - expectedRawPanic);
        moodManager.tick(currRawPanic, expectedRawPanic, panicDiff, currSan);
        if (currRealPanic > 0.0) {
            double finalPanic = currRealPanic;
            if (!isDarkEnv) {
                finalPanic -= 0.06 * MathHelper.clamp(statusManager.getMaxExpLevelReached(), 0, 30); //Reduce panic when player reaches higher exp level
                if (expectedRawPanic > 0.0)
                    sanityManager.add(-MathHelper.clamp(0.00003 * finalPanic, 0.000008, 0.0001));
            }
            if (finalPanic > 0.0)
                EntityHelper.addHcsDebuff(this, HcsEffects.PANIC, MathHelper.clamp((int) finalPanic, 0, 3));
        }

        //Debuff of Fracture
        if (injuryManager.getFracture() > 0.0) EntityHelper.addHcsDebuff(this, HcsEffects.FRACTURE);

        //Debuff of Parasite & cold
        DiseaseManager diseaseManager = ((StatAccessor) this).getDiseaseManager();
        final double currParasite = diseaseManager.getParasite(), currCold = diseaseManager.getCold();
        if (currParasite > 0.1)
            EntityHelper.addHcsDebuff(this, HcsEffects.PARASITE_INFECTION, MathHelper.clamp((int) currParasite, 0, 2));
        if (currCold > 1.0) EntityHelper.addHcsDebuff(this, HcsEffects.COLD);

        //Debuff of unhappiness
        if (moodManager.getHappiness() < 0.5) EntityHelper.addHcsDebuff(this, HcsEffects.UNHAPPY);
    }
}