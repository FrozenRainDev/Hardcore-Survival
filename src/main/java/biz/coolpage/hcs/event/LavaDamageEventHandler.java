package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LavaDamageEventHandler {

    @SubscribeEvent
    public static void onLivingHurt(@NotNull LivingHurtEvent event) {
        // Check if the damage source is lava
        if (event.getSource().is(DamageTypes.LAVA)) {
            // Get the original damage amount
            float originalDamage = event.getAmount();

            // Multiply the damage by 3 and set it back to the event
            event.setAmount(originalDamage * 2.5F);
        }
    }
}