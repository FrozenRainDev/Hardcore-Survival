package com.hcs.main.event;

import com.hcs.main.manager.TemperatureManager;
import com.hcs.misc.accessor.StatAccessor;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;

public class EntitySleepEvent {
    public static void init() {
        EntitySleepEvents.ALLOW_RESETTING_TIME.register(((playerEntity) -> {
            for (PlayerEntity player : playerEntity.getWorld().getPlayers()) {
                if (player.isSleeping()) {
                    //Add: recovery depends on how long a player slept; restore san
                    player.heal(20.0F);
                    ((StatAccessor) player).getStaminaManager().reset();
                    ((StatAccessor) player).getThirstManager().addDirectly(-0.25);
                    ((StatAccessor) player).getSanityManager().reset();
                    HungerManager hungerManager = player.getHungerManager();
                    hungerManager.setExhaustion(0.0F);
                    hungerManager.setFoodLevel(Math.max(0, hungerManager.getFoodLevel() - 4));
                    TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
                    //Warm oneself by sleeping
                    if (temperatureManager.get() < 0.5) temperatureManager.set(0.5);
                }
            }
            return true;
        }));
    }
}
