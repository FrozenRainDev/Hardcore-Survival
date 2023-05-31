package com.hcs.mixin.entity.player;

import com.hcs.item.HotWaterBottleItem;
import com.hcs.main.helper.RotHelper;
import com.hcs.main.manager.TemperatureManager;
import com.hcs.misc.accessor.StatAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow
    public PlayerEntity player;

    protected PlayerInventoryMixin(PlayerEntity player) {
        this.player = player;
    }

    @Inject(method = "updateItems", at = @At("HEAD"))
    public void updateItems(CallbackInfo ci) {
        RotHelper.update(this.player.world, this.player.getInventory());
        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
        /*
        float envTemp = temperatureManager.getEnvTempCache();
        float playerTemp = temperatureManager.get();
        boolean shouldSlowDown = (envTemp > 0.5 && playerTemp > 0.5) || (envTemp < 0.5 && playerTemp < 0.5);
         */
        HotWaterBottleItem.update(this.player.world, this.player.getInventory(), temperatureManager.getTrendType());
    }
}
