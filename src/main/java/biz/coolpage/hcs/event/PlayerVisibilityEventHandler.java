package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerVisibilityEventHandler {

    @SubscribeEvent
    public static void onLivingVisibility(LivingEvent.@NotNull LivingVisibilityEvent event) {
        // Check if the entity being targeted/looked at is a player
        if (event.getEntity() instanceof Player player) {

            // Check if the player is currently sneaking (crouching)
            if (player.isCrouching()) {
                /*
                 * Vanilla Minecraft already applies a 0.8 visibility modifier when the player is sneaking.
                 * To make the final detection distance exactly 2/3 (0.666...) of the ORIGINAL (unsneaking) distance,
                 * we must multiply the current visibility by: (2/3) / 0.8 = 5/6 (approx 0.8333...).
                 *
                 * If you meant to reduce the ALREADY REDUCED vanilla sneak distance by a further 2/3,
                 * you can just use: event.modifyVisibility(2.0D / 3.0D);
                 */
                double targetModifier = 2.0D / 3.0D;
                double vanillaSneakModifier = 0.8D;

                event.modifyVisibility(targetModifier / vanillaSneakModifier);
            }
        }
    }
}