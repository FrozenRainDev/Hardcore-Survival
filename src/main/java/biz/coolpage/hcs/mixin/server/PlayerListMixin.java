package biz.coolpage.hcs.mixin.server;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.item.HotWaterBottleItem;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    public void remove(ServerPlayer player, CallbackInfo ci) {
        if (player == null) {
            Reg.LOGGER.error("PlayerListMixin/remove;player==null");
            return;
        }
        RotHelper.onLeaveGame(player.level(), player.getInventory());
        HotWaterBottleItem.onLeaveGame(player.level(), player.getInventory());
    }
}