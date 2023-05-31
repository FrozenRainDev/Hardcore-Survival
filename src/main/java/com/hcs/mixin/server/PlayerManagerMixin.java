package com.hcs.mixin.server;

import com.hcs.item.HotWaterBottleItem;
import com.hcs.main.Reg;
import com.hcs.main.helper.RotHelper;
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
