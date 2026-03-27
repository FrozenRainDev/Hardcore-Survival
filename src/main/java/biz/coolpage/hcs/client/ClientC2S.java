package biz.coolpage.hcs.client;

import biz.coolpage.hcs.status.ServerC2S;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientC2S {
    public static void writeC2SPacketOnDrinkWater(Object playerObj, int x, int y, int z) {
        if (playerObj instanceof LocalPlayer player) {
            if (player.level() != null && player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty())
                player.level().playLocalSound(x, y, z, SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 10.0F, 1.0F, true);

            ServerC2S.CHANNEL.sendToServer(new ServerC2S.DrinkWaterPacket(new int[]{player.getId(), x, y, z}));
        }
    }

    public static void writeC2SPacketOnPlayerEnter(Object playerObj) {
        if (playerObj instanceof LocalPlayer player) {
            ServerC2S.CHANNEL.sendToServer(new ServerC2S.PlayerEnterPacket(new int[]{player.getId()}));
        }
    }

    public static void writeC2SPacketOnLitHoldingTorchInLava(Object playerObj, int hand) {
        if (playerObj instanceof LocalPlayer player) {
            ServerC2S.CHANNEL.sendToServer(new ServerC2S.LitTorchPacket(new int[]{player.getId(), hand}));
        }
    }
}