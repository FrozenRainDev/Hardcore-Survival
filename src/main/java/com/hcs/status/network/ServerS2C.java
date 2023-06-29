package com.hcs.status.network;

import com.hcs.status.accessor.StatAccessor;
import com.hcs.status.manager.*;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class ServerS2C {
    public static final Identifier THIRST_ID = new Identifier("hcs", "s2c_thirst");
    public static final Identifier STAMINA_ID = new Identifier("hcs", "s2c_stamina");
    public static final Identifier TEMPERATURE_ID = new Identifier("hcs", "s2c_temperature");
    public static final Identifier STATUS_ID = new Identifier("hcs", "s2c_status");
    public static final Identifier SANITY_ID = new Identifier("hcs", "s2c_sanity");
    public static final Identifier NUTRITION_ID = new Identifier("hcs", "s2c_nutrition");
    public static final float TRANS_MULTIPLIER = 10000000.0F;

    public static int floatToInt(float val) {
        return (int) (val * TRANS_MULTIPLIER);
    }

    public static int doubleToInt(double val) {
        return (int) (val * TRANS_MULTIPLIER);
    }

    public static int booleanToInt(boolean val) {
        return val ? 1 : 0;
    }

    public static void writeS2CPacket(Object playerObj) {
        if (playerObj instanceof ServerPlayerEntity player) writeS2CPacket(player);
    }

    private static void writeS2CPacket(@NotNull ServerPlayerEntity player) {
        PacketByteBuf buf1 = new PacketByteBuf(Unpooled.buffer());
        ThirstManager thirstManager = ((StatAccessor) player).getThirstManager();
        buf1.writeIntArray(new int[]{player.getId(), doubleToInt(thirstManager.get()), floatToInt(thirstManager.getSaturation()), floatToInt(thirstManager.getThirstRateAffectedByTemp())});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(THIRST_ID, buf1));

        PacketByteBuf buf2 = new PacketByteBuf(Unpooled.buffer());
        buf2.writeIntArray(new int[]{player.getId(), doubleToInt(((StatAccessor) player).getStaminaManager().get())});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(STAMINA_ID, buf2));

        PacketByteBuf buf3 = new PacketByteBuf(Unpooled.buffer());
        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
        buf3.writeIntArray(new int[]{player.getId(), doubleToInt(temperatureManager.get()), floatToInt(temperatureManager.getEnvTempCache()), floatToInt(temperatureManager.getSaturation()), floatToInt(temperatureManager.getFeelTempCache()), temperatureManager.getTrendType()});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(TEMPERATURE_ID, buf3));

        PacketByteBuf buf4 = new PacketByteBuf(Unpooled.buffer());
        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
        statusManager.setExhaustion(player.getHungerManager().getExhaustion());
        buf4.writeIntArray(new int[]{player.getId(), floatToInt(statusManager.getExhaustion()), statusManager.getRecentAttackTicks(), statusManager.getRecentMiningTicks(), statusManager.getRecentHasColdWaterBagTicks(), statusManager.getRecentHasHotWaterBagTicks(), statusManager.getMaxExpLevelReached(), statusManager.getRecentLittleOvereatenTicks(), booleanToInt(statusManager.hasDecimalFoodLevel()), statusManager.getOxygenLackLevel(), statusManager.getOxygenGenLevel()});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(STATUS_ID, buf4));

        PacketByteBuf buf5 = new PacketByteBuf(Unpooled.buffer());
        SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
        buf5.writeIntArray(new int[]{player.getId(), doubleToInt(sanityManager.get()), doubleToInt(sanityManager.getDifference()), sanityManager.getMonsterWitnessingTicks()});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(SANITY_ID, buf5));

        PacketByteBuf buf6 = new PacketByteBuf(Unpooled.buffer());
        NutritionManager nutritionManager = ((StatAccessor) player).getNutritionManager();
        buf6.writeIntArray(new int[]{player.getId(), doubleToInt(nutritionManager.getVegetable())});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(NUTRITION_ID, buf6));
    }
}
