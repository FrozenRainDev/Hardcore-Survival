package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

// Improvised shield can only reduce damage by 80%.
@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ImprovisedShieldEventHandler {
    @SubscribeEvent
    public static void onShieldBlock(@NotNull ShieldBlockEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack activeItem = entity.getUseItem();
        if (activeItem.getItem() == Hcs.IMPROVISED_SHIELD.get()) {
            if (event.getDamageSource().is(DamageTypeTags.IS_EXPLOSION)) {
                float originalBlockedDamage = event.getBlockedDamage();
                float newBlockedDamage = originalBlockedDamage * 0.8F;
                event.setBlockedDamage(newBlockedDamage);
            }
        }
    }
}