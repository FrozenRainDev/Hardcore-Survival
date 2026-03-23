package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID)
public class ServerEntityEvent {
    // Also see PlayerManagerMixin for player quit event
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getLastDeathLocation().isEmpty() && player.getFoodData().getFoodLevel() == 20 && player.getFoodData().getExhaustionLevel() == 0.0F && ((StatAccessor) player).getThirstManager().get() == 1.0 && player.getScore() == 0 && player.totalExperience == 0 && player.getInventory().isEmpty()) {
                // Novice gift
                player.getFoodData().addExhaustion(2.0F);
                EntityHelper.dropItem(player, Hcs.STONE_CONE);
                EntityHelper.dropItem(player, Hcs.PURIFIED_WATER_BOTTLE, 3);
                EntityHelper.dropItem(player, Hcs.BANDAGE, 3);
                EntityHelper.dropItem(player, PotionUtils.setPotion(new ItemStack(Items.POTION), Hcs.LONG_CONSTANT_TEMPERATURE_POTION));
            }
        }
    }
}