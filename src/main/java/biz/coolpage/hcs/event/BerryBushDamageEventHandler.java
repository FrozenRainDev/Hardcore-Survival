package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

// Make the damage caused by berry bushes very limited
@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BerryBushDamageEventHandler {
    @SubscribeEvent
    public static void onLivingHurt(@NotNull LivingHurtEvent event) {
        if (event.getSource().is(DamageTypes.SWEET_BERRY_BUSH)) {
            float originalDamage = event.getAmount();
            float limitedDamage = Math.min(originalDamage, 0.1F);
            event.setAmount(limitedDamage);
        }
    }
}