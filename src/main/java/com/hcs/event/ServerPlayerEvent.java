package com.hcs.event;

import com.hcs.status.accessor.StatAccessor;
import com.hcs.status.manager.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.player.HungerManager;

public class ServerPlayerEvent {
    public static void init() {
        ServerPlayerEvents.AFTER_RESPAWN.register(((oldPlayer, newPlayer, alive) -> {
            HungerManager oldHungerManager = oldPlayer.getHungerManager();
            ThirstManager oldThirstManager = ((StatAccessor) oldPlayer).getThirstManager();
            StaminaManager oldStaminaManager = ((StatAccessor) oldPlayer).getStaminaManager();
            TemperatureManager oldTemperatureManager = ((StatAccessor) oldPlayer).getTemperatureManager();
            SanityManager oldSanityManager = ((StatAccessor) oldPlayer).getSanityManager();
            NutritionManager oldNutritionManager = ((StatAccessor) oldPlayer).getNutritionManager();
            WetnessManager oldWetnessManager = ((StatAccessor) oldPlayer).getWetnessManager();
            HungerManager newHungerManager = newPlayer.getHungerManager();
            ThirstManager newThirstManager = ((StatAccessor) newPlayer).getThirstManager();
            StaminaManager newStaminaManager = ((StatAccessor) newPlayer).getStaminaManager();
            TemperatureManager newTemperatureManager = ((StatAccessor) newPlayer).getTemperatureManager();
            SanityManager newSanityManager = ((StatAccessor) newPlayer).getSanityManager();
            NutritionManager newNutritionManager = ((StatAccessor) newPlayer).getNutritionManager();
            WetnessManager newWetnessManager = ((StatAccessor) newPlayer).getWetnessManager();
            PainManager newPainManager = ((StatAccessor) newPlayer).getPainManager();
            MoodManager newMoodManager = ((StatAccessor) newPlayer).getMoodManager();
            if (!alive) {
                newHungerManager.setSaturationLevel(1.0F);
                newThirstManager.reset();
                newStaminaManager.reset();
                newTemperatureManager.reset();
                newSanityManager.reset();
                newNutritionManager.reset();
                newWetnessManager.reset();
                newPainManager.reset();
                newMoodManager.reset();
            } else {
                newHungerManager.setFoodLevel(oldHungerManager.getFoodLevel());
                newThirstManager.set(oldThirstManager.get());
                newStaminaManager.set(oldStaminaManager.get());
                newTemperatureManager.set(oldTemperatureManager.get());
                newSanityManager.set(oldSanityManager.get());
                newNutritionManager.setVegetable(oldNutritionManager.getVegetable());
                newWetnessManager.set(oldWetnessManager.get());
            }
            StatusManager oldStatusManager = ((StatAccessor) oldPlayer).getStatusManager();
            ((StatAccessor) newPlayer).getStatusManager().reset(oldStatusManager.getMaxExpLevelReached(), oldStatusManager.getSoulImpairedStat() + (alive ? 0 : 1));
        }));
    }
}
