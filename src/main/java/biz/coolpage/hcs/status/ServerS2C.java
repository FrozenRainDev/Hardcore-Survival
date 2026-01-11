package biz.coolpage.hcs.status;

import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.*;
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
    public static final Identifier WETNESS_ID = new Identifier("hcs", "s2c_wetness");
    public static final Identifier PAIN_ID = new Identifier("hcs", "s2c_pain");
    public static final Identifier MOOD_ID = new Identifier("hcs", "s2c_mood");
    public static final Identifier DISEASE_ID = new Identifier("hcs", "s2c_disease");
    public static final Identifier CONFIG_ID = new Identifier("hcs", "s2c_config");
    public static final float TRANS_MULTIPLIER = 10000000.0F;

    public static int f2i(float val) {
        // float to int
        return (int) (val * TRANS_MULTIPLIER);
    }

    public static int d2i(double val) {
        // double to int
        return (int) (val * TRANS_MULTIPLIER);
    }

    public static int b2i(boolean val) {
        // boolean to int
        return val ? 1 : 0;
    }

    public static void writeS2CPacket(Object playerObj) {
        if (playerObj instanceof ServerPlayerEntity player) writeS2CPacket(player);
    }

    private static void writeS2CPacket(@NotNull ServerPlayerEntity player) {
        // todo refactor SHIT >A<
        PacketByteBuf buf1 = new PacketByteBuf(Unpooled.buffer());
        ThirstManager thirstManager = ((StatAccessor) player).getThirstManager();
        buf1.writeIntArray(new int[]{player.getId(), d2i(thirstManager.get()), f2i(thirstManager.getSaturation()), f2i(thirstManager.getThirstRateAffectedByTemp())});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(THIRST_ID, buf1));

        PacketByteBuf buf2 = new PacketByteBuf(Unpooled.buffer());
        buf2.writeIntArray(new int[]{player.getId(), d2i(((StatAccessor) player).getStaminaManager().get())});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(STAMINA_ID, buf2));

        PacketByteBuf buf3 = new PacketByteBuf(Unpooled.buffer());
        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
        buf3.writeIntArray(new int[]{player.getId(), d2i(temperatureManager.get()), f2i(temperatureManager.getEnvTempCache()), f2i(temperatureManager.getSaturation()), f2i(temperatureManager.getFeelTempCache()), temperatureManager.getTrendType()});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(TEMPERATURE_ID, buf3));

        PacketByteBuf buf4 = new PacketByteBuf(Unpooled.buffer());
        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
        OxygenManager oxygenManager = ((StatAccessor) player).getOxygenManager();
        statusManager.setExhaustion(player.getHungerManager().getExhaustion());
        buf4.writeIntArray(new int[]{player.getId(), f2i(statusManager.getExhaustion()), statusManager.getRecentAttackTicks(), statusManager.getRecentMiningTicks(), statusManager.getRecentHasColdWaterBagTicks(), statusManager.getRecentHasHotWaterBagTicks(), statusManager.getMaxExpLevelReached(), statusManager.getRecentLittleOvereatenTicks(), b2i(statusManager.hasDecimalFoodLevel()), oxygenManager.getOxygenLackLevel(), oxygenManager.getOxygenGenLevel(), statusManager.getRecentSleepTicks(), statusManager.getRecentWetTicks(), statusManager.getInDarknessTicks(), statusManager.getEnterCurrWldTimes(), statusManager.getStonesSmashed(), statusManager.getHcsDifficulty().ordinal(), f2i(statusManager.getBlockBreakingSpeed()), f2i(statusManager.getRealProtection()), statusManager.getRecentHurtTicks(), f2i(statusManager.getRecentFeelingDamage())});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(STATUS_ID, buf4));

        PacketByteBuf buf5 = new PacketByteBuf(Unpooled.buffer());
        SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
        buf5.writeIntArray(new int[]{player.getId(), d2i(sanityManager.get()), d2i(sanityManager.getDifference())});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(SANITY_ID, buf5));

        PacketByteBuf buf6 = new PacketByteBuf(Unpooled.buffer());
        NutritionManager nutritionManager = ((StatAccessor) player).getNutritionManager();
        buf6.writeIntArray(new int[]{player.getId(), d2i(nutritionManager.getVegetable())});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(NUTRITION_ID, buf6));

        PacketByteBuf buf7 = new PacketByteBuf(Unpooled.buffer());
        WetnessManager wetnessManager = ((StatAccessor) player).getWetnessManager();
        buf7.writeIntArray(new int[]{player.getId(), d2i(wetnessManager.get())});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(WETNESS_ID, buf7));

        PacketByteBuf buf8 = new PacketByteBuf(Unpooled.buffer());
        InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
        buf8.writeIntArray(new int[]{player.getId(), d2i(injuryManager.getRawPain()), d2i(injuryManager.getPainkillerAlleviation()), injuryManager.getPainkillerApplied(), d2i(injuryManager.getBleeding()), d2i(injuryManager.getFracture())});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(PAIN_ID, buf8));

        PacketByteBuf buf9 = new PacketByteBuf(Unpooled.buffer());
        MoodManager moodManager = ((StatAccessor) player).getMoodManager();
        buf9.writeIntArray(new int[]{player.getId(), d2i(moodManager.getRawPanic()), d2i(moodManager.getPanicAlleCache()), d2i(moodManager.getHappiness())});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(MOOD_ID, buf9));

        PacketByteBuf buf10 = new PacketByteBuf(Unpooled.buffer());
        DiseaseManager diseaseManager = ((StatAccessor) player).getDiseaseManager();
        buf10.writeIntArray(new int[]{player.getId(), d2i(diseaseManager.getParasite()), d2i(diseaseManager.getCold())});
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(DISEASE_ID, buf10));

        // Awful operations :( factory design pattern
        PacketByteBuf buf11 = new PacketByteBuf(Unpooled.buffer());
        int[] arr11 = new int[1 + Configs.values().length];
        arr11[0] = player.getId();
        for (Enum<Configs> val : Configs.values()) {
            arr11[val.ordinal() + 1] = b2i(((StatAccessor) player).getConfigManager().get(val));
        }
        buf11.writeIntArray(arr11);
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(CONFIG_ID, buf11));
    }
}
