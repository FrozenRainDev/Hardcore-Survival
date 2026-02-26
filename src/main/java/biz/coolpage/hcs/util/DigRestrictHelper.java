package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.Hcs;
import net.minecraft.world.level.block.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.tags.BlockTags;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import static biz.coolpage.hcs.util.DigRestrictHelper.Predicates.*;

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
            for (Item i : UNDIGABLE_TOOLS) if (item == i) return true;
            return false;
        };

        public static final Predicate<Block> IS_BREAKABLE_FUNCTIONAL = block -> (block instanceof BaseEntityBlock || block instanceof CraftingTableBlock || block instanceof AnvilBlock) && block.defaultDestroyTime() >= 0 || block == Hcs.ICEBOX || block == Hcs.DRYING_RACK;
        public static final Predicate<Block> IS_PLANT = block -> block instanceof BushBlock || block instanceof LeavesBlock || block instanceof VineBlock;
    }


    public static boolean canBreakExceptShovel(@Nullable Item mainHand, @Nullable net.minecraft.world.level.block.state.BlockState state) {
        if (mainHand == null || state == null) {
            Hcs.error("DigRestrictHelper/canBreak;mainHand==null||state==null");
            return false;
        }
        Block block = state.getBlock();
        if (IS_BREAKABLE_FUNCTIONAL.or(IS_BEAKABLE_MISC).test(block)) return true;
        if (mainHand instanceof DiggerItem) { //prev: stack.isEnchantable
            if (IS_UNDIGABLE_TOOLS.test(mainHand)) return false;
            if (block == Blocks.BAMBOO) return true;
            boolean isPlant = IS_PLANT.test(block);
            if (mainHand instanceof SwordItem)
                return isPlant || block instanceof WebBlock;
            if (mainHand == Hcs.STONE_CONE || mainHand == Hcs.FLINT_CONE || mainHand == Hcs.SHARP_BROKEN_BONE || mainHand == Items.WOODEN_SHOVEL)
                return isPlant || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
            if (mainHand == Hcs.FLINT_HATCHET)
                return isPlant || state.is(BlockTags.MINEABLE_WITH_AXE);
            return true;
        }
        float hardness = block.defaultDestroyTime();
        if (Float.compare(hardness, 0.0F) == -1) return false;
        if (hardness <= 0.41F) return true;
        return state.is(BlockTags.WOOL) || block instanceof ButtonBlock || block instanceof PressurePlateBlock;
    }

    public static boolean canBreak(@Nullable Item mainHand, @Nullable net.minecraft.world.level.block.state.BlockState state) {
        if (state == null) return false;
        return canBreakExceptShovel(mainHand, state) || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

}