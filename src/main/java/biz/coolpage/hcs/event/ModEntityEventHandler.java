package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityEventHandler {
    @SubscribeEvent
    public static void modifyEntityAttributes(@NotNull EntityAttributeModificationEvent event) {
        // ChickenMixin
        event.add(EntityType.CHICKEN, Attributes.MAX_HEALTH, 6.0);
        // PigMixin
        event.add(EntityType.PIG, Attributes.MAX_HEALTH, 20.0);
        // SheepMixin
        event.add(EntityType.SHEEP, Attributes.MAX_HEALTH, 16.0);
        event.add(EntityType.COW, Attributes.MAX_HEALTH, 20.0);
    }
}