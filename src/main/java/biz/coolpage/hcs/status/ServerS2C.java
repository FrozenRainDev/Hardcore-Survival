package biz.coolpage.hcs.status;

import biz.coolpage.hcs.client.ClientS2C;
import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.*;
import biz.coolpage.hcs.util.HcsFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ServerS2C {
    // 显式定义所有客户端需要的 ID 常量
    public static final ResourceLocation THIRST_ID = HcsFactory.createResourceLocation("s2c_thirst");
    public static final ResourceLocation STAMINA_ID = HcsFactory.createResourceLocation("s2c_stamina");
    public static final ResourceLocation TEMPERATURE_ID = HcsFactory.createResourceLocation("s2c_temperature");
    public static final ResourceLocation STATUS_ID = HcsFactory.createResourceLocation("s2c_status");
    public static final ResourceLocation SANITY_ID = HcsFactory.createResourceLocation("s2c_sanity");
    public static final ResourceLocation NUTRITION_ID = HcsFactory.createResourceLocation("s2c_nutrition");
    public static final ResourceLocation WETNESS_ID = HcsFactory.createResourceLocation("s2c_wetness");
    public static final ResourceLocation PAIN_ID = HcsFactory.createResourceLocation("s2c_pain");
    public static final ResourceLocation MOOD_ID = HcsFactory.createResourceLocation("s2c_mood");
    public static final ResourceLocation DISEASE_ID = HcsFactory.createResourceLocation("s2c_disease");
    public static final ResourceLocation CONFIG_ID = HcsFactory.createResourceLocation("s2c_config");

    public static final float TRANS_MULTIPLIER = 10000000.0F;

    public static int f2i(float val) { return (int) (val * TRANS_MULTIPLIER); }
    public static int d2i(double val) { return (int) (val * TRANS_MULTIPLIER); }
    public static int b2i(boolean val) { return val ? 1 : 0; }

    public static void writeS2CPacket(Object playerObj) {
        if (playerObj instanceof ServerPlayer player) writeS2CPacket(player);
    }

    private static void writeS2CPacket(@NotNull ServerPlayer player) {
        StatAccessor accessor = (StatAccessor) player;

        // Thirst
        ThirstManager thirstManager = accessor.getThirstManager();
        sendPacket(player, new GenericS2CPacket(THIRST_ID, new int[]{player.getId(), d2i(thirstManager.get()), f2i(thirstManager.getSaturation()), f2i(thirstManager.getThirstRateAffectedByTemp())}));

        // Stamina
        sendPacket(player, new GenericS2CPacket(STAMINA_ID, new int[]{player.getId(), d2i(accessor.getStaminaManager().get())}));

        // Temperature
        TemperatureManager temperatureManager = accessor.getTemperatureManager();
        sendPacket(player, new GenericS2CPacket(TEMPERATURE_ID, new int[]{player.getId(), d2i(temperatureManager.get()), f2i(temperatureManager.getEnvTempCache()), f2i(temperatureManager.getSaturation()), f2i(temperatureManager.getFeelTempCache()), temperatureManager.getTrendType()}));

        // Status & Oxygen
        StatusManager statusManager = accessor.getStatusManager();
        OxygenManager oxygenManager = accessor.getOxygenManager();
        statusManager.setExhaustion(player.getFoodData().getExhaustionLevel());
        sendPacket(player, new GenericS2CPacket(STATUS_ID, new int[]{player.getId(), f2i(statusManager.getExhaustion()), statusManager.getRecentAttackTicks(), statusManager.getRecentMiningTicks(), statusManager.getRecentHasColdWaterBagTicks(), statusManager.getRecentHasHotWaterBagTicks(), statusManager.getMaxExpLevelReached(), statusManager.getRecentLittleOvereatenTicks(), b2i(statusManager.hasDecimalFoodLevel()), oxygenManager.getOxygenLackLevel(), oxygenManager.getOxygenGenLevel(), statusManager.getRecentSleepTicks(), statusManager.getRecentWetTicks(), statusManager.getInDarknessTicks(), statusManager.getEnterCurrWldTimes(), statusManager.getStonesSmashed(), statusManager.getHcsDifficulty().ordinal(), f2i(statusManager.getBlockBreakingSpeed()), f2i(statusManager.getRealProtection()), statusManager.getRecentHurtTicks(), f2i(statusManager.getRecentFeelingDamage())}));

        // Sanity
        SanityManager sanityManager = accessor.getSanityManager();
        sendPacket(player, new GenericS2CPacket(SANITY_ID, new int[]{player.getId(), d2i(sanityManager.get()), d2i(sanityManager.getDifference())}));

        // Nutrition, Wetness, Injury, Mood, Disease
        NutritionManager nutritionManager = accessor.getNutritionManager();
        sendPacket(player, new GenericS2CPacket(NUTRITION_ID, new int[]{player.getId(), d2i(nutritionManager.getVegetable())}));

        WetnessManager wetnessManager = accessor.getWetnessManager();
        sendPacket(player, new GenericS2CPacket(WETNESS_ID, new int[]{player.getId(), d2i(wetnessManager.get())}));

        InjuryManager injuryManager = accessor.getInjuryManager();
        sendPacket(player, new GenericS2CPacket(PAIN_ID, new int[]{player.getId(), d2i(injuryManager.getRawPain()), d2i(injuryManager.getPainkillerAlleviation()), injuryManager.getPainkillerApplied(), d2i(injuryManager.getBleeding()), d2i(injuryManager.getFracture())}));

        MoodManager moodManager = accessor.getMoodManager();
        sendPacket(player, new GenericS2CPacket(MOOD_ID, new int[]{player.getId(), d2i(moodManager.getRawPanic()), d2i(moodManager.getPanicAlleCache()), d2i(moodManager.getHappiness())}));

        DiseaseManager diseaseManager = accessor.getDiseaseManager();
        sendPacket(player, new GenericS2CPacket(DISEASE_ID, new int[]{player.getId(), d2i(diseaseManager.getParasite()), d2i(diseaseManager.getCold())}));

        // Configs
        int[] arr11 = new int[1 + Configs.values().length];
        arr11[0] = player.getId();
        for (Configs val : Configs.values()) {
            arr11[val.ordinal() + 1] = b2i(accessor.getConfigManager().get(val));
        }
        sendPacket(player, new GenericS2CPacket(CONFIG_ID, arr11));
    }

    private static void sendPacket(ServerPlayer player, GenericS2CPacket packet) {
        ServerC2S.CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static class GenericS2CPacket {
        private final ResourceLocation id;
        private final int[] data;

        public GenericS2CPacket(ResourceLocation id, int[] data) {
            this.id = id;
            this.data = data;
        }

        public GenericS2CPacket(FriendlyByteBuf buf) {
            this.id = buf.readResourceLocation();
            int length = buf.readVarInt();
            this.data = new int[length];
            for (int i = 0; i < length; i++) this.data[i] = buf.readInt();
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(id);
            buf.writeVarInt(data.length);
            for (int i : data) buf.writeInt(i);
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> ClientS2C.handlePacket(this.id, this.data));
            ctx.get().setPacketHandled(true);
        }
    }
}