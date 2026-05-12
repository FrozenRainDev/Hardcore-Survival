package biz.coolpage.hcs.mixin.server;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.item.WaterBagItem;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin { // PlayerManagerMixin
    @Inject(method = "remove", at = @At("HEAD"))
    public void remove(ServerPlayer player, CallbackInfo ci) {
        if (player == null) {
            Hcs.error("PlayerManagerMixin/remove;player==null");
            return;
        }
        RotHelper.onLeaveGame(player.level(), player.getInventory());
        WaterBagItem.onLeaveGame(player.level(), player.getInventory());
    }
}