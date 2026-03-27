package biz.coolpage.hcs.client;

import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// 确保这里的静态导入能找到 ServerS2C 中新定义的常量
import static biz.coolpage.hcs.status.ServerS2C.*;

@OnlyIn(Dist.CLIENT)
public class ClientS2C {
    public static float i2f(int val) { return (float) val / TRANS_MULTIPLIER; }
    public static double i2d(int val) { return (double) val / TRANS_MULTIPLIER; }
    public static boolean i2b(int val) { return val == 1; }

    public static void handlePacket(ResourceLocation id, int[] bufArr) {
        Minecraft client = Minecraft.getInstance();
        // 这里的逻辑保持不变，现在编译应该能通过了
        client.execute(() -> {
            if (client.player != null && client.level != null && client.level.getEntity(bufArr[0]) != null) {
                Player player = (Player) client.level.getEntity(bufArr[0]);
                if (player == null) return;

                StatAccessor accessor = (StatAccessor) player;

                if (id.equals(THIRST_ID)) {
                    ThirstManager thirstManager = accessor.getThirstManager();
                    thirstManager.set(i2d(bufArr[1]));
                    thirstManager.setSaturation(i2f(bufArr[2]));
                    thirstManager.setThirstRateAffectedByTemp(i2f(bufArr[3]));
                } else if (id.equals(STAMINA_ID)) {
                    accessor.getStaminaManager().set(i2d(bufArr[1]));
                } else if (id.equals(TEMPERATURE_ID)) {
                    TemperatureManager temperatureManager = accessor.getTemperatureManager();
                    temperatureManager.set(i2d(bufArr[1]));
                    temperatureManager.setEnvTempCache(i2f(bufArr[2]));
                    temperatureManager.setSaturation(i2f(bufArr[3]));
                    temperatureManager.setFeelTempCache(i2f(bufArr[4]));
                    temperatureManager.setTrendType(bufArr[5]);
                } else if (id.equals(STATUS_ID)) {
                    StatusManager statusManager = accessor.getStatusManager();
                    OxygenManager oxygenManager = accessor.getOxygenManager();
                    statusManager.setExhaustion(i2f(bufArr[1]));
                    statusManager.setRecentAttackTicks(bufArr[2]);
                    statusManager.setRecentMiningTicks(bufArr[3]);
                    statusManager.setRecentHasColdWaterBagTicks(bufArr[4]);
                    statusManager.setRecentHasHotWaterBagTicks(bufArr[5]);
                    statusManager.setMaxExpLevelReached(bufArr[6]);
                    statusManager.setRecentLittleOvereatenTicks(bufArr[7]);
                    statusManager.setHasDecimalFoodLevel(i2b(bufArr[8]));
                    oxygenManager.setOxygenLackLevel(bufArr[9]);
                    oxygenManager.setOxygenGenLevel(bufArr[10]);
                    statusManager.setRecentSleepTicks(bufArr[11]);
                    statusManager.setRecentWetTicks(bufArr[12]);
                    statusManager.setInDarknessTicks(bufArr[13]);
                    statusManager.setEnterCurrWldTimes(bufArr[14]);
                    statusManager.setStonesSmashed(bufArr[15]);
                    statusManager.setHcsDifficulty(HcsDifficulty.HcsDifficultyEnum.values()[bufArr[16]]);
                    statusManager.setBlockBreakingSpeed(i2f(bufArr[17]));
                    statusManager.setRealProtection(i2f(bufArr[18]));
                    statusManager.setRecentHurtTicks(bufArr[19]);
                    statusManager.setRecentFeelingDamage(i2f(bufArr[20]));
                } else if (id.equals(SANITY_ID)) {
                    SanityManager sanityManager = accessor.getSanityManager();
                    sanityManager.set(i2d(bufArr[1]));
                    sanityManager.setDifference(i2d(bufArr[2]));
                } else if (id.equals(NUTRITION_ID)) {
                    accessor.getNutritionManager().setVegetable(i2d(bufArr[1]));
                } else if (id.equals(WETNESS_ID)) {
                    accessor.getWetnessManager().set(i2d(bufArr[1]));
                } else if (id.equals(PAIN_ID)) {
                    InjuryManager injuryManager = accessor.getInjuryManager();
                    injuryManager.setRawPain(i2d(bufArr[1]));
                    injuryManager.setAlleviationCache(i2d(bufArr[2]));
                    injuryManager.setPainkillerApplied(bufArr[3]);
                    injuryManager.setBleeding(i2d(bufArr[4]));
                    injuryManager.setFracture(i2d(bufArr[5]));
                } else if (id.equals(MOOD_ID)) {
                    MoodManager moodManager = accessor.getMoodManager();
                    moodManager.setPanic(i2d(bufArr[1]));
                    moodManager.setPanicAlleviation(i2d(bufArr[2]));
                    moodManager.setHappiness(i2d(bufArr[3]));
                } else if (id.equals(DISEASE_ID)) {
                    DiseaseManager diseaseManager = accessor.getDiseaseManager();
                    diseaseManager.setParasite(i2d(bufArr[1]));
                    diseaseManager.setCold(i2d(bufArr[2]));
                } else if (id.equals(CONFIG_ID)) {
                    // todo bad here
                    ConfigManager configManager = accessor.getConfigManager();
                    for (Configs val : Configs.values()) {
                        configManager.set(val, i2b(bufArr[val.ordinal() + 1]));
                    }
                }
            }
        });
    }

    public static void init() { }
}