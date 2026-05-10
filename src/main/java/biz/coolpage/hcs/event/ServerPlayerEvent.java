package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.*;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID)
public class ServerPlayerEvent {
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.@NotNull Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        boolean alive = !event.isWasDeath();

        // Cast to StatAccessor once to avoid redundant casting and resolve duplication warning
        StatAccessor oldAccessor = (StatAccessor) oldPlayer;
        StatAccessor newAccessor = (StatAccessor) newPlayer;

        FoodData newHungerManager = newPlayer.getFoodData();

        if (!alive) {
            newHungerManager.setSaturation(1.0F);
            newAccessor.getThirstManager().reset();
            newAccessor.getStaminaManager().reset();
            newAccessor.getTemperatureManager().reset();
            newAccessor.getSanityManager().reset();
            newAccessor.getNutritionManager().reset();
            newAccessor.getWetnessManager().reset();
            newAccessor.getInjuryManager().reset();
            newAccessor.getMoodManager().reset();
            newAccessor.getDiseaseManager().reset();
        } else {
            newHungerManager.setFoodLevel(oldPlayer.getFoodData().getFoodLevel());
            newAccessor.getThirstManager().set(oldAccessor.getThirstManager().get());
            newAccessor.getStaminaManager().set(oldAccessor.getStaminaManager().get());
            newAccessor.getTemperatureManager().set(oldAccessor.getTemperatureManager().get());
            newAccessor.getSanityManager().set(oldAccessor.getSanityManager().get());
            // Keep original logic untouched: only set vegetable
            newAccessor.getNutritionManager().setVegetable(oldAccessor.getNutritionManager().getVegetable());
            newAccessor.getWetnessManager().set(oldAccessor.getWetnessManager().get());
        }

        StatusManager oldStatusManager = oldAccessor.getStatusManager();
        int maxSoulImpaired = StatusManager.getMaxSoulImpaired(newPlayer);

        if (oldStatusManager.getSoulImpairedStat() > maxSoulImpaired) {
            oldStatusManager.setSoulImpairedStat(maxSoulImpaired);
        }

        newAccessor.getStatusManager().reset(
                oldStatusManager.getMaxExpLevelReached(),
                Math.min(maxSoulImpaired, oldStatusManager.getSoulImpairedStat() + (alive ? 0 : 1)),
                oldStatusManager.getStonesSmashed(),
                oldStatusManager.getHcsDifficulty(),
                oldStatusManager.hasShownInitTips(),
                oldStatusManager.getEnterCurrWldTimes()
        );
    }
}