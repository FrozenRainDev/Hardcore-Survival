package biz.coolpage.hcs.status;

import biz.coolpage.hcs.event.UseBlockEvent;
import biz.coolpage.hcs.util.HcsFactory; // 导入工厂类
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class ServerC2S {

    public static final ResourceLocation DRINK_WATER_WITH_BARE_HAND = HcsFactory.createResourceLocation("c2s_drink_water_with_bare_hand");
    public static final ResourceLocation ON_PLAYER_ENTER = HcsFactory.createResourceLocation("c2s_on_player_enter");
    public static final ResourceLocation LIT_HOLDING_TORCH_IN_LAVA = HcsFactory.createResourceLocation("c2s_lit_holding_torch_lava");

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            HcsFactory.createResourceLocation("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        int id = 0;
        CHANNEL.registerMessage(id++, DrinkWaterPacket.class, DrinkWaterPacket::encode, DrinkWaterPacket::new, DrinkWaterPacket::handle);
        CHANNEL.registerMessage(id++, PlayerEnterPacket.class, PlayerEnterPacket::encode, PlayerEnterPacket::new, PlayerEnterPacket::handle);
        CHANNEL.registerMessage(id++, LitTorchPacket.class, LitTorchPacket::encode, LitTorchPacket::new, LitTorchPacket::handle);
    }

    // --- 辅助工具：手动读写 int 数组 ---
    private static void writeIntArray(FriendlyByteBuf buf, int[] arr) {
        buf.writeVarInt(arr.length);
        for (int i : arr) buf.writeInt(i);
    }

    private static int[] readIntArray(FriendlyByteBuf buf) {
        int length = buf.readVarInt();
        int[] arr = new int[length];
        for (int i = 0; i < length; i++) arr[i] = buf.readInt();
        return arr;
    }

    // --- Packet Classes ---

    public static class DrinkWaterPacket {
        private final int[] bufArr;
        public DrinkWaterPacket(FriendlyByteBuf buf) { this.bufArr = readIntArray(buf); }
        public DrinkWaterPacket(int[] bufArr) { this.bufArr = bufArr; }
        public void encode(FriendlyByteBuf buf) { writeIntArray(buf, bufArr); }
        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.level() != null) {
                    Entity targetEntity = player.level().getEntity(bufArr[0]);
                    if (targetEntity instanceof ServerPlayer serverPlayerEntity)
                        UseBlockEvent.onDrinkWaterWithBareHand(serverPlayerEntity, new BlockPos(bufArr[1], bufArr[2], bufArr[3]));
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class PlayerEnterPacket {
        private final int[] bufArr;
        public PlayerEnterPacket(FriendlyByteBuf buf) { this.bufArr = readIntArray(buf); }
        public PlayerEnterPacket(int[] bufArr) { this.bufArr = bufArr; }
        public void encode(FriendlyByteBuf buf) { writeIntArray(buf, bufArr); }
        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.level() != null) {
                    Entity targetEntity = player.level().getEntity(bufArr[0]);
                    if (targetEntity instanceof ServerPlayer serverPlayerEntity) {
                        Iterator<MobEffect> iterator = getStatusEffectIterator(serverPlayerEntity);
                        while (iterator.hasNext()) {
                            serverPlayerEntity.removeEffect(iterator.next());
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static class LitTorchPacket {
        private final int[] bufArr;
        public LitTorchPacket(FriendlyByteBuf buf) { this.bufArr = readIntArray(buf); }
        public LitTorchPacket(int[] bufArr) { this.bufArr = bufArr; }
        public void encode(FriendlyByteBuf buf) { writeIntArray(buf, bufArr); }
        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.level() != null) {
                    Entity targetEntity = player.level().getEntity(bufArr[0]);
                    if (targetEntity instanceof ServerPlayer sp) {
                        ItemStack stack = bufArr[1] == 1 ? sp.getMainHandItem() : sp.getOffhandItem();
                        stack.shrink(1);
                        sp.level().playSound(null, sp.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
                        sp.serverLevel().sendParticles(ParticleTypes.LARGE_SMOKE, sp.getX(), sp.getY(), sp.getZ(), 5, 0.0, 0.0, 0.0, 0.0);
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    private static @NotNull Iterator<MobEffect> getStatusEffectIterator(@NotNull ServerPlayer serverPlayerEntity) {
        List<MobEffect> list = new ArrayList<>();
        for (MobEffectInstance effect : serverPlayerEntity.getActiveEffects()) {
            MobEffect type = effect.getEffect();
            if (type.getDescriptionId().contains("effect.hcs.") && type.getCategory() == MobEffectCategory.HARMFUL)
                list.add(type);
        }
        return list.iterator();
    }
}