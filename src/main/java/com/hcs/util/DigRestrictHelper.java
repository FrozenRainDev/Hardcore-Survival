package com.hcs.util;

import com.hcs.Reg;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.tag.BlockTags;

import java.util.function.Predicate;

public class DigRestrictHelper {
    public static class Predicates {
        private static final Block[] BEAKABLE_MISC = {
                Blocks.SAND, Blocks.RED_SAND, Blocks.SPONGE, Blocks.HAY_BLOCK, Blocks.MANGROVE_ROOTS, Blocks.CLAY, Blocks.COBWEB,
                Blocks.BAMBOO, Blocks.BAMBOO_SAPLING, Blocks.CAKE,
                Blocks.MELON, Blocks.PUMPKIN, Blocks.CARVED_PUMPKIN, Blocks.LANTERN, Blocks.SOUL_LANTERN,
                Blocks.LEVER, Blocks.TARGET
        };

        private static final Item[] UNDIGABLE_TOOLS = {
                Items.WOODEN_AXE, Items.WOODEN_HOE, Items.WOODEN_PICKAXE,
                Items.STONE_AXE, Items.STONE_HOE, Items.STONE_PICKAXE, Items.STONE_SHOVEL, Items.STONE_SWORD,
                Items.BOW, Items.CROSSBOW, Items.FISHING_ROD, Items.CARROT_ON_A_STICK, Items.WARPED_FUNGUS_ON_A_STICK, Items.SHIELD, Items.TRIDENT
        };

        public static final Predicate<Block> IS_BEAKABLE_MISC = block -> {
            for (Block block1 : BEAKABLE_MISC) if (block == block1) return true;
            return false;
        };

        public static final Predicate<Item> IS_UNDIGABLE_TOOLS = item -> {
            for (Item item1 : UNDIGABLE_TOOLS) if (item == item1) return true;
            return false;
        };

        public static final Predicate<Block> IS_BREAKABLE_FUNCTIONAL = block -> (block instanceof BlockWithEntity || block instanceof CraftingTableBlock || block instanceof AnvilBlock) && block.getHardness() >= 0 || block == Reg.ICEBOX || block == Reg.DRYING_RACK;
    }


    public static boolean canBreak(Item mainHand, BlockState state) {
        if (mainHand == null || state == null) {
            Reg.LOGGER.error("DigRestrictHelper/canBreak;mainHand==null||state==null");
            return false;
        }
        Block block = state.getBlock();
        if (Predicates.IS_BREAKABLE_FUNCTIONAL.test(block) || Predicates.IS_BEAKABLE_MISC.test(block)) return true;
        if (mainHand.isEnchantable(new ItemStack(mainHand))) { //tools
            if (Predicates.IS_UNDIGABLE_TOOLS.test(mainHand)) return false;
            if (block == Blocks.BAMBOO) return true;
            boolean isPlant = block instanceof PlantBlock || block instanceof LeavesBlock || block instanceof VineBlock;
            if (mainHand instanceof SwordItem) // mainHand == Reg.STONE_KNIFE || mainHand == Reg.FLINT_KNIFE || mainHand == Reg.STONE_SPEAR || mainHand == Reg.FLINT_SPEAR || mainHand == Items.WOODEN_SWORD || mainHand == Reg.SPIKED_CLUB
                return isPlant || block instanceof CobwebBlock;
            if (mainHand == Reg.STONE_CONE || mainHand == Reg.FLINT_CONE || mainHand == Reg.SHARP_BROKEN_BONE || mainHand == Items.WOODEN_SHOVEL)
                return isPlant || state.isIn(BlockTags.SHOVEL_MINEABLE);
            if (mainHand == Reg.FLINT_HATCHET)
                return isPlant || state.isIn(BlockTags.AXE_MINEABLE);
            return true;
        }
        float hardness = block.getHardness();
        if (hardness <= 0.41 && hardness >= 0) return true;
        return state.getMaterial() == Material.WOOL || block instanceof ButtonBlock || block instanceof PressurePlateBlock;
    }

}
