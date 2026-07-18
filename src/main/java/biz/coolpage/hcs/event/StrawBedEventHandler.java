package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.block.StrawBedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StrawBedEventHandler {
    /**
     * Intercept and cancel the spawn point setting if the player tries to set it via a Straw Bed.
     */
    @SubscribeEvent
    public static void onPlayerSetSpawn(@NotNull PlayerSetSpawnEvent event) {
        Level level = event.getEntity().level();
        BlockPos pos = event.getNewSpawn();

        // Ensure we are checking a valid position on the server side
        if (pos != null && !level.isClientSide) {
            BlockState state = level.getBlockState(pos);

            // If the target block is our custom Straw Bed, cancel the spawn setting event
            if (state.getBlock() instanceof StrawBedBlock) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * Destroy the Straw Bed after the player wakes up to make it a single-use item.
     */
    @SubscribeEvent
    public static void onPlayerWakeUp(@NotNull PlayerWakeUpEvent event) {
        Level level = event.getEntity().level();

        if (!level.isClientSide) {
            event.getEntity().getSleepingPos().ifPresent(pos -> {
                BlockState state = level.getBlockState(pos);

                // Check if the player was sleeping in a Straw Bed
                if (state.getBlock() instanceof StrawBedBlock) {

                    // Destroy the primary part of the bed (false = do not drop item)
                    level.destroyBlock(pos, false);

                    // BedBlocks are double blocks; we must also destroy the connected half
                    BlockPos otherPartPos = pos.relative(BedBlock.getConnectedDirection(state));
                    BlockState otherPartState = level.getBlockState(otherPartPos);

                    if (otherPartState.is(state.getBlock())) {
                        level.destroyBlock(otherPartPos, false);
                    }
                }
            });
        }
    }
}