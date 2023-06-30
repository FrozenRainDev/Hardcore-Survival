package com.hcs.status.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;

import static net.minecraft.sound.SoundEvents.ENTITY_GENERIC_DRINK;

@Environment(EnvType.CLIENT)
public class ClientC2S {
    public static void writeC2SPacketOnDrinkWater(Object playerObj, int x, int y, int z) {
        if (playerObj instanceof ClientPlayerEntity player) {
            if (player.world != null && player.isSneaking() && player.getMainHandStack().isEmpty())
                player.world.playSound(x, y, z, ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 10, 1, true);
            PacketByteBuf buf1 = new PacketByteBuf(Unpooled.buffer());
            buf1.writeIntArray(new int[]{player.getId(), x, y, z});
            ClientPlayNetworking.send(ServerC2S.DRINK_WATER_WITH_BARE_HAND, buf1);
        }
    }

    public static void writeC2SPacketOnPlayerEnter(Object playerObj) {
        if (playerObj instanceof ClientPlayerEntity player) {
            PacketByteBuf buf1 = new PacketByteBuf(Unpooled.buffer());
            buf1.writeIntArray(new int[]{player.getId()});
            ClientPlayNetworking.send(ServerC2S.ON_PLAYER_ENTER, buf1);
        }
    }
}