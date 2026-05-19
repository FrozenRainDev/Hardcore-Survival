package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CropGrowthEventHandler {

    /**
     * Replaces the Mixins for CropBlock, SweetBerryBushBlock, and Cave Vines (Glow Berries) randomTick.
     */
    @SubscribeEvent
    public static void onCropGrow(BlockEvent.CropGrowEvent.@NotNull Pre event) {
        Block block = event.getState().getBlock();

        // Ensure we are targeting CropBlocks, SweetBerry bushes, and Cave Vines
        if (block instanceof CropBlock || block instanceof SweetBerryBushBlock || block instanceof CaveVines) {
            // Slow down growing speed
            if (Math.random() > 0.2) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    /**
     * Replaces the CropBlock#performBonemeal Mixin.
     * Retains original boundary conditions for crops.
     */
    @SubscribeEvent
    public static void onBonemeal(@NotNull BonemealEvent event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState stateDown = level.getBlockState(pos.below());

        // Check boundary condition to target only CropBlocks
        if (event.getBlock().getBlock() instanceof CropBlock) {
            if (stateDown.is(Blocks.FARMLAND)
                    && stateDown.getValues().containsKey(WorldHelper.FERTILIZER_FREE)
                    && !stateDown.getValue(WorldHelper.FERTILIZER_FREE)) {

                event.setCanceled(true);
            }
        }
    }
}