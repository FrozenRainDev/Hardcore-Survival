package biz.coolpage.hcs.mixin.server;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.item.HotWaterBottleItem;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    public void remove(ServerPlayerEntity player, CallbackInfo ci) {
        if (player == null) {
            Reg.LOGGER.error("PlayerManagerMixin/remove;player==null");
            return;
        }
        RotHelper.onLeaveGame(player.getWorld(), player.getInventory());
        HotWaterBottleItem.onLeaveGame(player.getWorld(), player.getInventory());
    }
}
