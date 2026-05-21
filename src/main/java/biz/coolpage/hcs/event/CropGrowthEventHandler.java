package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.world.FertilizerSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import static biz.coolpage.hcs.config.Configs.SLOW_PLANT_GROWTH;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CropGrowthEventHandler {

    // Helper method to filter out grass, tall grass, flowers, etc.
    // Only target actual crops, berries, vines, stems, and saplings.
    private static boolean isApplicablePlant(Block block) {
        return block instanceof CropBlock ||
                block instanceof SweetBerryBushBlock ||
                block instanceof CaveVines ||
                block instanceof StemBlock ||
                block instanceof SaplingBlock;
    }

    // Helper method to check if the plant has reached its maximum growth stage
    private static boolean isPlantFullyGrown(@NotNull BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        } else if (block instanceof SweetBerryBushBlock) {
            // Sweet berries max age is 3
            return state.getValue(SweetBerryBushBlock.AGE) >= 3;
        }
        return false;
    }

    // Slow down the growth rate of crops, but slightly speed up if fertilized
    @SubscribeEvent
    public static void onCropGrow(BlockEvent.CropGrowEvent.@NotNull Pre event) {
        if (!Configs.isEnabled(SLOW_PLANT_GROWTH)) return;
        Block block = event.getState().getBlock();
        double rateMultiplier = 1.0;

        if (isApplicablePlant(block)) {
            // Apply different base rate multipliers based on plant type
            if (block instanceof CropBlock) {
                rateMultiplier = 0.06;
            } else if (block instanceof SweetBerryBushBlock || block instanceof CaveVines) {
                rateMultiplier = 0.01;
            } else {
                rateMultiplier = 0.05; // Default fallback for stems, saplings, etc.
            }

            if (event.getLevel() instanceof ServerLevel serverLevel) {
                BlockPos pos = event.getPos(); // Getting the plant's position directly

                // Check if the plant itself is fertilized to increase growth speed by 20%
                if (FertilizerSavedData.get(serverLevel).isFertilized(pos)) {
                    rateMultiplier *= 1.25;
                }
            }
        }

        if (rateMultiplier != 1.0 && Math.random() > rateMultiplier) {
            event.setResult(Event.Result.DENY);
        }
    }

    // Remove fertilizer data when the plant reaches max age
    @SubscribeEvent
    public static void onCropGrowPost(BlockEvent.CropGrowEvent.@NotNull Post event) {
        if (!Configs.isEnabled(SLOW_PLANT_GROWTH)) return;
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            BlockState state = event.getState();
            BlockPos pos = event.getPos();

            if (isPlantFullyGrown(state)) {
                FertilizerSavedData.get(serverLevel).removeFertilized(pos);
            }
        }
    }

    // Bone meal cannot be used repeatedly, and prevent instant maturity
    @SubscribeEvent
    public static void onBonemeal(@NotNull BonemealEvent event) {
        if (!Configs.isEnabled(SLOW_PLANT_GROWTH)) return;
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Block block = event.getBlock().getBlock();

        // Check if the target is our specified growable plant
        if (isApplicablePlant(block)) {
            if (level instanceof ServerLevel serverLevel) {
                FertilizerSavedData data = FertilizerSavedData.get(serverLevel);

                if (data.isFertilized(pos)) {
                    // Cannot stack: cancel the event to prevent consuming bonemeal again
                    event.setCanceled(true);
                } else {
                    // Apply fertilizer and mark the plant's position
                    data.addFertilized(pos);
                    // Prevent instant growth but consume bonemeal item
                    // Event.Result.ALLOW tells Forge it succeeded, skipping vanilla instant growth
                    event.setResult(Event.Result.ALLOW);
                }
            } else {
                // Client side: allow to show arm swing and particle/consume animation
                event.setResult(Event.Result.ALLOW);
            }
        }
    }

    // Clean up data if the plant is manually broken
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.@NotNull BreakEvent event) {
        if (!Configs.isEnabled(SLOW_PLANT_GROWTH)) return;
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            Block block = event.getState().getBlock();
            if (isApplicablePlant(block)) {
                FertilizerSavedData.get(serverLevel).removeFertilized(event.getPos());
            }
        }
    }
}