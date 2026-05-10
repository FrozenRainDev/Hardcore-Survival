package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.InjuryManager;
import biz.coolpage.hcs.status.manager.StatusManager;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID)
public class EntitySleepEvent {
    @SubscribeEvent
    public static void onPlayerWakeUp(@NotNull PlayerWakeUpEvent event) {
        // Forge does not have a direct 1:1 for Fabric's ALLOW_RESETTING_TIME that fires per-player easily,
        // but PlayerWakeUpEvent combined with getSleepTimer() >= 100 perfectly handles the morning transition.
        Player player = event.getEntity();

        // Ensure it runs on the server side and the player has successfully completed a full sleep (sleep timer >= 100)
        if (!player.level().isClientSide && player.getSleepTimer() >= 100) {
            StatusManager statusManager = ((StatAccessor) player).getStatusManager();
            if (statusManager.getRecentSleepTicks() <= 0) {
                statusManager.setRecentSleepTicks(600);
                player.heal(player.getMaxHealth());
                ((StatAccessor) player).getStaminaManager().reset();
                ((StatAccessor) player).getThirstManager().addDirectly(-0.25);
                ((StatAccessor) player).getSanityManager().reset();
                FoodData hungerManager = player.getFoodData();
                hungerManager.setExhaustion(0.0F);
                hungerManager.setFoodLevel(Math.max(0, hungerManager.getFoodLevel() - 4));
                TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
                if (temperatureManager.get() < 0.5) temperatureManager.set(0.5);
                InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
                injuryManager.addRawPain(-1);
                injuryManager.setPainkillerApplied(0);
            }
        }
    }
}