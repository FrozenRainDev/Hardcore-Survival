package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.config.Configs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PreventJumpPlaceEvent {

    // Prevent players from placing blocks while jumping or in the air
    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.@NotNull RightClickBlock event) {
        if (!Configs.isEnabled(Configs.NO_JUMP_PLACEMENT)) return;
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();
        // Check if the player is holding a block and is not on the ground
        if (itemStack.getItem() instanceof BlockItem && !player.onGround()) {
            // Cancel the placement event
            event.setCanceled(true);
        }
    }
}