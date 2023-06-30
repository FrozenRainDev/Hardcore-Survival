package com.hcs.event;

import com.hcs.status.accessor.StatAccessor;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public class ServerPlayerEvent {
    public static void init() {
        ServerPlayerEvents.AFTER_RESPAWN.register(((oldPlayer, newPlayer, alive) -> {
            ((StatAccessor) newPlayer).getThirstManager().reset();
            ((StatAccessor) newPlayer).getStaminaManager().reset();
            ((StatAccessor) newPlayer).getTemperatureManager().reset();
            ((StatAccessor) newPlayer).getStatusManager().reset(((StatAccessor) oldPlayer).getStatusManager().getMaxExpLevelReached());
            ((StatAccessor) newPlayer).getSanityManager().reset();
            ((StatAccessor) newPlayer).getNutritionManager().reset();
            ((StatAccessor) newPlayer).getWetnessManager().reset();
        }));
    }
}
