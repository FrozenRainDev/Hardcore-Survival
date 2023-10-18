package biz.coolpage.hcs.status.network;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;

import static biz.coolpage.hcs.status.network.ServerS2C.*;


@Environment(EnvType.CLIENT)
public class ClientS2C {
    public static float itof(int val) {
        //int to float
        return (float) val / TRANS_MULTIPLIER;
    }

    public static double itod(int val) {
        //int to double
        return (double) val / TRANS_MULTIPLIER;
    }

    public static boolean itob(int val) {
        //int to boolean
        return val == 1;
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(THIRST_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.world.getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.world.getEntityById(bufArr[0]);
                    if (player != null) {
                        ThirstManager thirstManager = ((StatAccessor) player).getThirstManager();
                        thirstManager.set(itod(bufArr[1]));
                        thirstManager.setSaturation(itof(bufArr[2]));
                        thirstManager.setThirstRateAffectedByTemp(itof(bufArr[3]));
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(STAMINA_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.world.getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.world.getEntityById(bufArr[0]);
                    if (player != null) {
                        StaminaManager staminaManager = ((StatAccessor) player).getStaminaManager();
                        staminaManager.set(itod(bufArr[1]));
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(TEMPERATURE_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.world.getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.world.getEntityById(bufArr[0]);
                    if (player != null) {
                        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
                        temperatureManager.set(itod(bufArr[1]));
                        temperatureManager.setEnvTempCache(itof(bufArr[2]));
                        temperatureManager.setSaturation(itof(bufArr[3]));
                        temperatureManager.setFeelTempCache(itof(bufArr[4]));
                        temperatureManager.setTrendType(bufArr[5]);
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(STATUS_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.world.getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.world.getEntityById(bufArr[0]);
                    if (player != null) {
                        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
                        statusManager.setExhaustion(itof(bufArr[1]));
                        statusManager.setRecentAttackTicks(bufArr[2]);
                        statusManager.setRecentMiningTicks(bufArr[3]);
                        statusManager.setRecentHasColdWaterBagTicks(bufArr[4]);
                        statusManager.setRecentHasHotWaterBagTicks(bufArr[5]);
                        statusManager.setMaxExpLevelReached(bufArr[6]);
                        statusManager.setRecentLittleOvereatenTicks(bufArr[7]);
                        statusManager.setHasDecimalFoodLevel(itob(bufArr[8]));
                        statusManager.setOxygenLackLevel(bufArr[9]);
                        statusManager.setOxygenGenLevel(bufArr[10]);
                        statusManager.setRecentSleepTicks(bufArr[11]);
                        statusManager.setRecentWetTicks(bufArr[12]);
                        statusManager.setInDarknessTicks(bufArr[13]);
                        statusManager.setEnterCurrWldTimes(bufArr[14]);
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SANITY_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.world.getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.world.getEntityById(bufArr[0]);
                    if (player != null) {
                        SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
                        sanityManager.set(itod(bufArr[1]));
                        sanityManager.setDifference(itod(bufArr[2]));
                    }
                }
            });
        });


        ClientPlayNetworking.registerGlobalReceiver(NUTRITION_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.world.getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.world.getEntityById(bufArr[0]);
                    if (player != null) {
                        NutritionManager nutritionManager = ((StatAccessor) player).getNutritionManager();
                        nutritionManager.setVegetable(itod(bufArr[1]));
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(WETNESS_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.world.getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.world.getEntityById(bufArr[0]);
                    if (player != null) {
                        WetnessManager wetnessManager = ((StatAccessor) player).getWetnessManager();
                        wetnessManager.set(itod(bufArr[1]));
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PAIN_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.world.getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.world.getEntityById(bufArr[0]);
                    if (player != null) {
                        InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
                        injuryManager.setRawPain(itod(bufArr[1]));
                        injuryManager.setAlleviationCache(itod(bufArr[2]));
                        injuryManager.setPainkillerApplied(bufArr[3]);
                        injuryManager.setBleeding(itod(bufArr[4]));
                        injuryManager.setFracture(itod(bufArr[5]));
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(MOOD_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.world.getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.world.getEntityById(bufArr[0]);
                    if (player != null) {
                        MoodManager moodManager = ((StatAccessor) player).getMoodManager();
                        moodManager.setPanic(itod(bufArr[1]));
                        moodManager.setPanicAlle(itod(bufArr[2]));
                        moodManager.setHappiness(itod(bufArr[3]));
                    }
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(DISEASE_ID, (client, handler, buffer, responseSender) -> {
            int[] bufArr = buffer.readIntArray();
            client.execute(() -> {
                if (client.player != null && client.player.world.getEntityById(bufArr[0]) != null) {
                    PlayerEntity player = (PlayerEntity) client.player.world.getEntityById(bufArr[0]);
                    if (player != null) {
                        DiseaseManager diseaseManager = ((StatAccessor) player).getDiseaseManager();
                        diseaseManager.setParasite(itod(bufArr[1]));
                        diseaseManager.setCold(itod(bufArr[2]));
                    }
                }
            });
        });
    }
}
