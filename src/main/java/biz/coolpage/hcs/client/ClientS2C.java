package biz.coolpage.hcs.client;

import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;

import static biz.coolpage.hcs.status.ServerS2C.*;


@Environment(EnvType.CLIENT)
public class ClientS2C {
    public static float i2f(int val) {
        // int to float
        return (float) val / TRANS_MULTIPLIER;
    }

    public static double i2d(int val) {
        // int to double
        return (double) val / TRANS_MULTIPLIER;
    }

    public static boolean i2b(int val) {
        // int to boolean
        return val == 1;
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(THIRST_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.getWorld().getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.getWorld().getEntityById(bufArr[0]);
                    if (player != null) {
                        ThirstManager thirstManager = ((StatAccessor) player).getThirstManager();
                        thirstManager.set(i2d(bufArr[1]));
                        thirstManager.setSaturation(i2f(bufArr[2]));
                        thirstManager.setThirstRateAffectedByTemp(i2f(bufArr[3]));
                    }
                }
            });
        });

        // todo redundant codes :( consider strategy design pattern???
        ClientPlayNetworking.registerGlobalReceiver(STAMINA_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.getWorld().getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.getWorld().getEntityById(bufArr[0]);
                    if (player != null) {
                        StaminaManager staminaManager = ((StatAccessor) player).getStaminaManager();
                        staminaManager.set(i2d(bufArr[1]));
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(TEMPERATURE_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.getWorld().getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.getWorld().getEntityById(bufArr[0]);
                    if (player != null) {
                        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
                        temperatureManager.set(i2d(bufArr[1]));
                        temperatureManager.setEnvTempCache(i2f(bufArr[2]));
                        temperatureManager.setSaturation(i2f(bufArr[3]));
                        temperatureManager.setFeelTempCache(i2f(bufArr[4]));
                        temperatureManager.setTrendType(bufArr[5]);
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(STATUS_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.getWorld().getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.getWorld().getEntityById(bufArr[0]);
                    if (player != null) {
                        // todo refactor :( SHIT
                        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
                        OxygenManager oxygenManager = ((StatAccessor) player).getOxygenManager();
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
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SANITY_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.getWorld().getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.getWorld().getEntityById(bufArr[0]);
                    if (player != null) {
                        SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
                        sanityManager.set(i2d(bufArr[1]));
                        sanityManager.setDifference(i2d(bufArr[2]));
                    }
                }
            });
        });


        ClientPlayNetworking.registerGlobalReceiver(NUTRITION_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.getWorld().getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.getWorld().getEntityById(bufArr[0]);
                    if (player != null) {
                        NutritionManager nutritionManager = ((StatAccessor) player).getNutritionManager();
                        nutritionManager.setVegetable(i2d(bufArr[1]));
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(WETNESS_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.getWorld().getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.getWorld().getEntityById(bufArr[0]);
                    if (player != null) {
                        WetnessManager wetnessManager = ((StatAccessor) player).getWetnessManager();
                        wetnessManager.set(i2d(bufArr[1]));
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PAIN_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.getWorld().getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.getWorld().getEntityById(bufArr[0]);
                    if (player != null) {
                        InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
                        injuryManager.setRawPain(i2d(bufArr[1]));
                        injuryManager.setAlleviationCache(i2d(bufArr[2]));
                        injuryManager.setPainkillerApplied(bufArr[3]);
                        injuryManager.setBleeding(i2d(bufArr[4]));
                        injuryManager.setFracture(i2d(bufArr[5]));
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(MOOD_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.getWorld().getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.getWorld().getEntityById(bufArr[0]);
                    if (player != null) {
                        MoodManager moodManager = ((StatAccessor) player).getMoodManager();
                        moodManager.setPanic(i2d(bufArr[1]));
                        moodManager.setPanicAlleviation(i2d(bufArr[2]));
                        moodManager.setHappiness(i2d(bufArr[3]));
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(DISEASE_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.getWorld().getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.getWorld().getEntityById(bufArr[0]);
                    if (player != null) {
                        DiseaseManager diseaseManager = ((StatAccessor) player).getDiseaseManager();
                        diseaseManager.setParasite(i2d(bufArr[1]));
                        diseaseManager.setCold(i2d(bufArr[2]));
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(CONFIG_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.getWorld().getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.getWorld().getEntityById(bufArr[0]);
                    if (player != null) {
                        ConfigManager configManager = ((StatAccessor) player).getConfigManager();
                        for (Enum<Configs> val : Configs.values()) {
                            configManager.set(val, i2b(bufArr[val.ordinal() + 1]));
                        }
                    }
                }
            });
        });
    }
}
