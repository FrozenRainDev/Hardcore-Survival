package biz.coolpage.hcs.block;

import biz.coolpage.hcs.Reg;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class BurntCampfireBlock extends CampfireBlock {
    public BurntCampfireBlock() {
        super(true, 1, AbstractBlock.Settings.of(Material.WOOD, MapColor.SPRUCE_BROWN).strength(2.0f).sounds(BlockSoundGroup.WOOD).luminance(Blocks.createLightLevelFromLitBlockState(0)).nonOpaque());
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        // Just do nothing
    }

    @Override
    protected Block asBlock() {
        return Reg.BURNT_CAMPFIRE_BLOCK;
    }

    @Override
    public Item asItem() {
        return Reg.BURNT_CAMPFIRE;
    }

}
