package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID)
public class WolfAngerEventHandler {

    // Unique UUID for the custom angry wolf speed modifier
    private static final UUID ANGRY_SPEED_MODIFIER_UUID = UUID.fromString("d7a2f1b4-9c8e-4a3d-b5f7-6e5d4c3b2a1f");

    // Add 50% (0.5D) to the total speed, which makes it 1.5 times the original speed
    private static final AttributeModifier ANGRY_SPEED_MODIFIER = new AttributeModifier(
            ANGRY_SPEED_MODIFIER_UUID,
            "Angry wolf speed boost",
            0.5D,
            AttributeModifier.Operation.MULTIPLY_TOTAL
    );

    @SubscribeEvent
    public static void onWolfTick(LivingEvent.@NotNull LivingTickEvent event) {
        // Check if the current entity is a Wolf
        if (event.getEntity() instanceof Wolf wolf) {

            // Get the wolf's movement speed attribute
            AttributeInstance speedAttribute = wolf.getAttribute(Attributes.MOVEMENT_SPEED);

            if (speedAttribute != null) {
                // Check if the wolf is currently angry
                boolean isAngry = wolf.isAngry();
                boolean hasModifier = speedAttribute.hasModifier(ANGRY_SPEED_MODIFIER);

                // If angry and doesn't have the modifier, add it temporarily
                if (isAngry && !hasModifier) {
                    speedAttribute.addTransientModifier(ANGRY_SPEED_MODIFIER);
                }
                // If no longer angry but still has the modifier, remove it
                else if (!isAngry && hasModifier) {
                    speedAttribute.removeModifier(ANGRY_SPEED_MODIFIER_UUID);
                }
            }
        }
    }
}