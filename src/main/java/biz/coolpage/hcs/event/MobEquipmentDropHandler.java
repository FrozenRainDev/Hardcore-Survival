package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID)
public class MobEquipmentDropHandler {

    @SubscribeEvent
    public static void onLivingDeath(@NotNull LivingDeathEvent event) {
        // Execute only on the logical server side
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof Mob mob) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = mob.getItemBySlot(slot);
                if (!stack.isEmpty() && isToolOrArmor(stack.getItem())) {
                    // Set drop chance to 0% only for tools and armor
                    mob.setDropChance(slot, 0.0f);
                }
            }
        }
    }

    /**
     * Checks if the item is classified as a standard tool, weapon, or armor.
     * Excludes items like Tridents (TridentItem) and Nautilus Shells (Item).
     */
    private static boolean isToolOrArmor(Item item) {
        return item instanceof ArmorItem ||                  // Helmets, chestplates, leggings, boots
                item instanceof TieredItem ||                 // Swords, pickaxes, axes, shovels, hoes
                item instanceof ProjectileWeaponItem ||       // Bows, crossbows (excludes Trident)
                item instanceof ShieldItem ||                 // Shields
                item instanceof ShearsItem ||                 // Shears
                item instanceof FishingRodItem ||             // Fishing rods
                item instanceof BrushItem ||                  // Brushes
                item instanceof FlintAndSteelItem;            // Flint and steel
    }
}