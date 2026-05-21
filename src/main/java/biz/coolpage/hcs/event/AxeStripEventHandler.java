package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AxeStripEventHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onBlockToolModification(BlockEvent.@NotNull BlockToolModificationEvent event) {
        // Only proceed if it is an actual axe strip action and not just a simulated check or cancelled event
        if (event.isSimulated() || event.isCanceled() || event.getToolAction() != ToolActions.AXE_STRIP) {
            return;
        }

        Level level = event.getContext().getLevel();
        if (level.isClientSide) {
            return;
        }

        BlockState originalState = event.getState();

        // Simulate the check to see if this specific block CAN be stripped
        BlockState strippedState = originalState.getToolModifiedState(event.getContext(), ToolActions.AXE_STRIP, true);

        // If the block is valid for stripping and changing its state
        if (strippedState != null && strippedState != originalState) {
            // Get exact click location and clicked face to prevent item stuck in block
            Vec3 hit = event.getContext().getClickLocation();
            Direction face = event.getContext().getClickedFace();

            // Offset slightly outwards from the clicked face (closer to the player)
            double spawnX = hit.x + face.getStepX() * 0.15;
            double spawnY = hit.y + face.getStepY() * 0.15;
            double spawnZ = hit.z + face.getStepZ() * 0.15;

            // 1. Always drop normal bark
            ItemStack barkStack = new ItemStack(Hcs.BARK.get());
            ItemEntity barkEntity = new ItemEntity(level, spawnX, spawnY, spawnZ, barkStack);
            // Optionally, add a slight velocity towards the player
            // barkEntity.setDeltaMovement(face.getStepX() * 0.05, 0.05, face.getStepZ() * 0.05);
            level.addFreshEntity(barkEntity);

            // 2. 15% chance to additionally drop willow bark
            if (level.random.nextFloat() < 0.15F) {
                ItemStack willowStack = new ItemStack(Hcs.WILLOW_BARK.get());
                ItemEntity willowEntity = new ItemEntity(level, spawnX, spawnY, spawnZ, willowStack);
                level.addFreshEntity(willowEntity);
            }
        }
    }
}