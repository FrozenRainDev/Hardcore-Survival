package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_LIKE;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class JukeboxSanityEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.@NotNull PlayerTickEvent event) {
        // Ensure the logic only runs on the server side and exactly once per tick at the end phase
        if (event.phase == TickEvent.Phase.END && event.side.isServer()) {
            Player player = event.player;

            if (EntityHelper.isExistent(player) && IS_SURVIVAL_LIKE.test(player)) {
                Level level = player.level();
                if (level instanceof ServerLevel serverWorld) {
                    int chunkX = player.chunkPosition().x;
                    int chunkZ = player.chunkPosition().z;

                    // Variables to track the strongest effects to prevent stacking
                    double maxPositiveChange = 0.0;
                    double maxNegativeChange = 0.0;

                    // A 24-block radius can span at most 2 chunks away in any direction
                    for (int x = chunkX - 2; x <= chunkX + 2; x++) {
                        for (int z = chunkZ - 2; z <= chunkZ + 2; z++) {
                            LevelChunk chunk = serverWorld.getChunkSource().getChunkNow(x, z);

                            if (chunk != null) {
                                // Only iterate through existing block entities in the chunk for optimal performance
                                for (BlockEntity be : chunk.getBlockEntities().values()) {
                                    if (be instanceof JukeboxBlockEntity jukebox && jukebox.isRecordPlaying()) {
                                        BlockPos pos = jukebox.getBlockPos();
                                        double distance = Math.sqrt(player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));

                                        if (distance < 24.0) {
                                            ItemStack stack = jukebox.getItem(0); // todo test here

                                            if (stack != null && !stack.isEmpty()) {
                                                // Listening to music restores or reduces sanity
                                                double baseSanChange = stack.is(Items.MUSIC_DISC_5) || stack.is(Items.MUSIC_DISC_11) || stack.is(Items.MUSIC_DISC_13) ? -0.00005 : 0.0001;
                                                double actualChange = baseSanChange * Math.max(1 - distance / 24.0, 0.0);

                                                // Record the strongest positive and negative values instead of applying them immediately
                                                if (actualChange < 0) {
                                                    maxNegativeChange = Math.min(maxNegativeChange, actualChange); // Keep the most negative value
                                                } else if (actualChange > 0) {
                                                    maxPositiveChange = Math.max(maxPositiveChange, actualChange); // Keep the most positive value
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Apply the sanity change logic
                    if (maxNegativeChange < 0) {
                        // Negative sanity debuff overrides any positive recovery
                        ((StatAccessor) player).getSanityManager().add(maxNegativeChange);
                    } else if (maxPositiveChange > 0) {
                        // Only apply positive recovery if there is no negative debuff present
                        ((StatAccessor) player).getSanityManager().add(maxPositiveChange);
                    }
                }
            }
        }
    }
}