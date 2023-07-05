package com.hcs.event;

import com.hcs.status.accessor.StatAccessor;
import com.hcs.status.manager.StatusManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public class ServerPlayerEvent {
    public static void init() {
        ServerPlayerEvents.AFTER_RESPAWN.register(((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                newPlayer.getHungerManager().setSaturationLevel(1.0F);
                ((StatAccessor) newPlayer).getThirstManager().reset();
                ((StatAccessor) newPlayer).getStaminaManager().reset();
                ((StatAccessor) newPlayer).getTemperatureManager().reset();
                ((StatAccessor) newPlayer).getSanityManager().reset();
                ((StatAccessor) newPlayer).getNutritionManager().reset();
                ((StatAccessor) newPlayer).getWetnessManager().reset();
            }
            StatusManager oldStatusManager = ((StatAccessor) oldPlayer).getStatusManager();
            ((StatAccessor) newPlayer).getStatusManager().reset(oldStatusManager.getMaxExpLevelReached(), oldStatusManager.getSoulImpairedStat() + (alive ? 0 : 1));
        }));
    }
}
