package biz.coolpage.hcs.mixin.entity.player;

import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.ServerS2C;
import biz.coolpage.hcs.status.accessor.IDamageSources;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.*;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.TemperatureHelper;
import biz.coolpage.hcs.util.WorldHelper;
import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ServerPlayer.class)
// ServerPlayerEntityMixin
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin(Level level, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(level, pos, yaw, gameProfile);
    }

    @Unique
    private static final String SPONSOR_URL = "https://hcs.coolpage.biz/sponsorship.html";

    @Shadow
    public abstract boolean hurt(DamageSource source, float amount);

    @Shadow
    public abstract boolean isCreative();

    @Shadow
    public abstract boolean isSpectator();

    @Shadow
    @Final
    private ServerStatsCounter stats;

    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {
        TemperatureHelper.updateAmbientBlocks(this);
        TemperatureHelper.getTemp(this);
        ServerS2C.writeS2CPacket((ServerPlayer) (Object) this);
        StatusManager statusManager = ((StatAccessor) this).getStatusManager();
        ConfigManager configs = ((StatAccessor) this).getConfigManager();

        if (this.level() instanceof ServerLevel serverLevel) {
            WorldHelper.trySetServerWorld(serverLevel);
            statusManager.setHcsDifficulty(HcsDifficulty.getDifficulty(serverLevel));
            configs.update(serverLevel);
        }

        if (!statusManager.hasShownInitTips()) {
            int enterWorldTimes = statusManager.getEnterCurrWldTimes();
            if (enterWorldTimes > 0 && enterWorldTimes % 5 == 0)
                this.sendSystemMessage(Component.literal("\n")
                        .append(Component.translatable("hcs.tip.sponsor", enterWorldTimes))
                        .append(Component.translatable("hcs.tip.sponsor_link")
                                .withStyle(style -> style.withUnderlined(true).withColor(ChatFormatting.AQUA)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, SPONSOR_URL)))));
            statusManager.setHasCheckInitTips(true);
        }

        if (!EntityHelper.IS_SURVIVAL_LIKE.test(this)) return;

        if (this.hasEffect(HcsEffects.OVEREATEN.get()) && this.getFoodData().getFoodLevel() < 20)
            this.removeEffect(HcsEffects.OVEREATEN.get());

        Holder<Biome> biomeEntry = this.level().getBiome(this.blockPosition());
        Biome biome = biomeEntry.value();
        String biomeName = TemperatureHelper.getBiomeName(biomeEntry);
        ThirstManager thirstManager = ((StatAccessor) this).getThirstManager();
        TemperatureManager temperatureManager = ((StatAccessor) this).getTemperatureManager();
        StaminaManager staminaManager = ((StatAccessor) this).getStaminaManager();

        double playerTemp = temperatureManager.get(), tempSatuPercent = temperatureManager.getSaturationPercentage();
        float envTempReal = temperatureManager.getEnvTempCache();
        int skyLightLevel = this.level().getBrightness(LightLayer.SKY, this.blockPosition().above());
        int sunshineIntensity = TemperatureHelper.getSunshineIntensityLevel(this.level().getDayTime(), this.level().isRaining(), biomeName);
        int windchillLevel = TemperatureHelper.getWindchillLevel(this.level(), this.blockPosition(), biomeEntry);
        float envTemp = TemperatureHelper.getFeelingTemp(this, envTempReal, biomeName, skyLightLevel);

        double span1 = TemperatureManager.CHANGE_SPAN * ((playerTemp > 0.75) ? 0.3 : 1.0);
        double span2 = -TemperatureManager.CHANGE_SPAN * ((playerTemp < 0.25) ? 0.3 : 1.0);
        final long currTime = this.level().getGameTime();
        final boolean isRelaxingMode = HcsDifficulty.isOf(this.level(), HcsDifficulty.HcsDifficultyEnum.relaxing);

        if (this.isInPowderSnow) {
            if (this.isFullyFrozen()) temperatureManager.add(-1.0);
            else temperatureManager.set(Math.max(0.01, temperatureManager.get() - 0.005));
        } else if (playerTemp > 0.9F && this.hasEffect(MobEffects.FIRE_RESISTANCE))
            temperatureManager.set(0.9);
        else if (this.getRemainingFireTicks() > 1) temperatureManager.add(0.002);
        else if (temperatureManager.getAmbientCache() > 1.5F) {
            if (statusManager.getRecentHasColdWaterBagTicks() > 0)
                temperatureManager.add(0.0001);
            else {
                if (temperatureManager.getAmbientCache() >= 9.9F)
                    temperatureManager.add(0.003);
                else temperatureManager.add(0.0005);
            }
        } else if (envTemp - playerTemp > span1) {
            temperatureManager.setTrendType(1);
            temperatureManager.add(span1);
        } else if (envTemp - playerTemp < span2) {
            temperatureManager.setTrendType(-1);
            temperatureManager.add(span2);
        } else {
            temperatureManager.setTrendType(0);
            temperatureManager.set(envTemp);
        }

        final double currStamina = staminaManager.get();
        if (ServerS2C.d2i(thirstManager.get()) <= 10 && currTime % 400 == 0) {
            DamageSource damageSource = ((IDamageSources) this.level().damageSources()).dehydrate();
            if (damageSource != null) this.hurt(damageSource, 1.0F);
        }

        thirstManager.updateThirstRateAffectedByTemp(envTemp, (float) playerTemp);
        if (thirstManager.get() <= 0.3) EntityHelper.addHcsDebuff(this, HcsEffects.DEHYDRATED.get());
        if (this.getFoodData().getFoodLevel() <= 6) EntityHelper.addHcsDebuff(this, HcsEffects.STARVING.get());
        if (currStamina <= 0.3) EntityHelper.addHcsDebuff(this, HcsEffects.EXHAUSTED.get(), currStamina < 0.1 ? 1 : 0);
        if (playerTemp >= 1.0) EntityHelper.addHcsDebuff(this, HcsEffects.HEATSTROKE.get(), (int) tempSatuPercent);
        else if (playerTemp <= 0.0) EntityHelper.addHcsDebuff(this, HcsEffects.HYPOTHERMIA.get(), (int) tempSatuPercent);

        if (this.isUsingItem() && (this.getMainHandItem().getItem() instanceof ShieldItem || this.getOffhandItem().getItem() instanceof ShieldItem))
            staminaManager.pauseRestoring();

        if (biome.getModifiedClimateSettings().downfall() <= 0.0F || TemperatureHelper.isSpecialSunshineArea(biomeName)) {
            if (sunshineIntensity > 0 && skyLightLevel >= 15)
                EntityHelper.addHcsDebuff(this, HcsEffects.STRONG_SUN.get(), Math.max(0, sunshineIntensity - 1));
        }
        if (biome.coldEnoughToSnow(this.blockPosition()) && windchillLevel > 0)
            EntityHelper.addHcsDebuff(this, HcsEffects.CHILLY_WIND.get(), windchillLevel - 1);

        SanityManager sanityManager = ((StatAccessor) this).getSanityManager();
        final double currSan = sanityManager.get();
        if (currSan < 0.3) {
            EntityHelper.addHcsDebuff(this, HcsEffects.INSANITY.get(), currSan < 0.15 ? (currSan < 0.1 ? (currSan < 0.05 ? 3 : 2) : 1) : 0);
            if (currSan < 0.05)
                this.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 50, 0, false, false, false));
        }

        double vegetable = ((StatAccessor) this).getNutritionManager().getVegetable();
        if (vegetable < 0.00001) EntityHelper.addHcsDebuff(this, HcsEffects.MALNUTRITION.get());

        double wet = ((StatAccessor) this).getWetnessManager().get();
        if (wet > 0.72) EntityHelper.addHcsDebuff(this, HcsEffects.WET.get(), 2);
        else if (wet > 0.4) EntityHelper.addHcsDebuff(this, HcsEffects.WET.get(), 1);
        else if (wet > 0.1) EntityHelper.addHcsDebuff(this, HcsEffects.WET.get(), 0);

        InjuryManager injuryManager = ((StatAccessor) this).getInjuryManager();
        MoodManager moodManager = ((StatAccessor) this).getMoodManager();

        if (!isRelaxingMode) {
            int soulImpairedStat = statusManager.getSoulImpairedStat();
            if (soulImpairedStat > 0) {
                if (this.getHealth() > this.getMaxHealth()) this.setHealth(this.getMaxHealth());
                EntityHelper.addHcsDebuff(this, HcsEffects.SOUL_IMPAIRED.get(), soulImpairedStat - 1);
            }

            if (((StatAccessor) this).getConfigManager().get(Configs.INJURY)) {
                float hpPercent = this.getHealth() / this.getMaxHealth();
                if (hpPercent < 0.1F) EntityHelper.addHcsDebuff(this, HcsEffects.INJURY.get(), 3);
                else if (hpPercent < 0.25F) EntityHelper.addHcsDebuff(this, HcsEffects.INJURY.get(), 2);
                else if (hpPercent < 0.45F) EntityHelper.addHcsDebuff(this, HcsEffects.INJURY.get(), 1);
                else if (hpPercent < 0.7F) EntityHelper.addHcsDebuff(this, HcsEffects.INJURY.get(), 0);
            }

            final double pain = injuryManager.getRealPain();
            if (pain > 0.0) EntityHelper.addHcsDebuff(this, HcsEffects.PAIN.get(), Mth.clamp((int) pain, 0, 3));
            injuryManager.tick();
            sanityManager.tickEnemies(this);

            final double bleeding = injuryManager.getBleeding() - 0.5;
            if (bleeding > 0.0)
                EntityHelper.addHcsDebuff(this, HcsEffects.BLEEDING.get(), Mth.clamp((int) bleeding, 0, 3));

            // Panic
            final boolean isDarkEnv = this.hasEffect(HcsEffects.DARKNESS_ENVELOPED.get());
            AtomicBoolean canSeeWither = new AtomicBoolean(false);
            EntityHelper.getOthersEntitiesInRange(this, WitherBoss.class, 3.0).forEach(wither -> canSeeWither.set(this.hasLineOfSight(wither)));
            final boolean shouldInExtremePanic = isDarkEnv || canSeeWither.get();
            final double currRawPanic = moodManager.getRawPanic();
            final double currRealPanic = moodManager.getRealPanic();
            final double expectedRawPanic;
            if (this.hasEffect(HcsEffects.FEARLESSNESS.get())) expectedRawPanic = 0.0;
            else expectedRawPanic = shouldInExtremePanic ? 4 : Mth.clamp(sanityManager.countEnemies() * 0.5, 0.0, 4);

            final double panicDiff = Math.abs(currRawPanic - expectedRawPanic);
            moodManager.tick(currRawPanic, expectedRawPanic, panicDiff, currSan);
            if (currRealPanic > 0.0) {
                double finalPanic = currRealPanic;
                if (!shouldInExtremePanic) {
                    finalPanic -= 0.06 * Mth.clamp(statusManager.getMaxExpLevelReached() + this.stats.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS)) / 5.0, 0.0, 30.0);
                    if (expectedRawPanic > 0.0)
                        sanityManager.add(-Mth.clamp(0.00003 * finalPanic, 0.000008, 0.0001));
                }
                if (finalPanic > 0.0)
                    EntityHelper.addHcsDebuff(this, HcsEffects.PANIC.get(), Mth.clamp((int) finalPanic, 0, 3));
            }
        }

        if (injuryManager.getFracture() > 0.0) EntityHelper.addHcsDebuff(this, HcsEffects.FRACTURE.get());

        DiseaseManager diseaseManager = ((StatAccessor) this).getDiseaseManager();
        final double currParasite = diseaseManager.getParasite(), currCold = diseaseManager.getCold();
        if (currParasite > 0.1)
            EntityHelper.addHcsDebuff(this, HcsEffects.PARASITE_INFECTION.get(), Mth.clamp((int) currParasite, 0, 2));
        if (currCold > 1.0) EntityHelper.addHcsDebuff(this, HcsEffects.COLD.get());

        if (moodManager.getHappiness() < 0.5) EntityHelper.addHcsDebuff(this, HcsEffects.UNHAPPY.get());
        if (statusManager.hasDarknessEnvelopedDebuff()) EntityHelper.addHcsDebuff(this, HcsEffects.DARKNESS_ENVELOPED.get());
        if (statusManager.hasHeavyLoadDebuff() && configs.get(Configs.HEAVY_LOAD))
            EntityHelper.addHcsDebuff(this, HcsEffects.HEAVY_LOAD.get());
    }
}