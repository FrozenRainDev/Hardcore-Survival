package biz.coolpage.hcs.event;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.InjuryManager;
import biz.coolpage.hcs.status.manager.StatusManager;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.entity.player.HungerManager;

public class EntitySleepEvent {
    public static void init() {
        EntitySleepEvents.ALLOW_RESETTING_TIME.register(((player) -> {
            if (player != null && player.isSleeping()) {
                //Add: recovery depends on how long a player slept
                StatusManager statusManager = ((StatAccessor) player).getStatusManager();
                if (statusManager.getRecentSleepTicks() <= 0) {
                    statusManager.setRecentSleepTicks(600);
                    player.heal(player.getMaxHealth());
                    ((StatAccessor) player).getStaminaManager().reset();
                    ((StatAccessor) player).getThirstManager().addDirectly(-0.25);
                    ((StatAccessor) player).getSanityManager().reset();
                    HungerManager hungerManager = player.getHungerManager();
                    hungerManager.setExhaustion(0.0F);
                    hungerManager.setFoodLevel(Math.max(0, hungerManager.getFoodLevel() - 4));
                    TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
                    //Warm oneself by sleeping
                    if (temperatureManager.get() < 0.5) temperatureManager.set(0.5);
                    InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
                    injuryManager.addRawPain(-1);
                    injuryManager.setPainkillerApplied(0);
                }
            }
            return true;
        }));
    }
}
