package com.hcs.status.network;

import com.hcs.status.accessor.StatAccessor;
import com.hcs.status.manager.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;

import static com.hcs.status.network.ServerS2C.*;


@Environment(EnvType.CLIENT)
public class ClientS2C {
    public static float intToFloat(int val) {
        return (float) val / TRANS_MULTIPLIER;
    }

    public static double intToDouble(int val) {
        return (double) val / TRANS_MULTIPLIER;
    }

    public static boolean intToBoolean(int val) {
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
                        thirstManager.set(intToDouble(bufArr[1]));
                        thirstManager.setSaturation(intToFloat(bufArr[2]));
                        thirstManager.setThirstRateAffectedByTemp(intToFloat(bufArr[3]));
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
                        staminaManager.set(intToDouble(bufArr[1]));
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
                        temperatureManager.set(intToDouble(bufArr[1]));
                        temperatureManager.setEnvTempCache(intToFloat(bufArr[2]));
                        temperatureManager.setSaturation(intToFloat(bufArr[3]));
                        temperatureManager.setFeelTempCache(intToFloat(bufArr[4]));
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
                        statusManager.setExhaustion(intToFloat(bufArr[1]));
                        statusManager.setRecentAttackTicks(bufArr[2]);
                        statusManager.setRecentMiningTicks(bufArr[3]);
                        statusManager.setRecentHasColdWaterBagTicks(bufArr[4]);
                        statusManager.setRecentHasHotWaterBagTicks(bufArr[5]);
                        statusManager.setMaxExpLevelReached(bufArr[6]);
                        statusManager.setRecentLittleOvereatenTicks(bufArr[7]);
                        statusManager.setHasDecimalFoodLevel(intToBoolean(bufArr[8]));
                        statusManager.setOxygenLackLevel(bufArr[9]);
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
                        sanityManager.set(intToDouble(bufArr[1]));
                        sanityManager.setDifference(intToDouble(bufArr[2]));
                        sanityManager.setPanicTicks(bufArr[3]);
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
                        nutritionManager.setVegetable(intToDouble(bufArr[1]));
                    }
                }
            });
        });
    }
}
