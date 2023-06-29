package com.hcs.util;

import com.hcs.Reg;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.BlockSoundGroup;

public class DigRestrictHelper {
    @Deprecated
    public static final Block[] TOOL_BLOCKS = {
            Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.SMOKER, Blocks.BLAST_FURNACE, Blocks.CHEST, Blocks.BARREL, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.ANVIL, Blocks.JUKEBOX, Blocks.NOTE_BLOCK, Blocks.STONECUTTER, Blocks.SMITHING_TABLE, Blocks.BREWING_STAND
    };
    public static final Block[] BEAKABLE_MISC = {
            Blocks.SAND, Blocks.RED_SAND, Blocks.SPONGE, Blocks.HAY_BLOCK, Blocks.MANGROVE_ROOTS, Blocks.CLAY, Blocks.COBWEB,
            Blocks.BAMBOO, Blocks.BAMBOO_SAPLING, Blocks.CAKE,
            Blocks.MELON, Blocks.PUMPKIN, Blocks.CARVED_PUMPKIN, Blocks.LANTERN, Blocks.SOUL_LANTERN,
            Blocks.LEVER, Blocks.TARGET
    };

    public static final Item[] UNDIGABLE_TOOLS = {
            Items.WOODEN_AXE, Items.WOODEN_HOE, Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL,
            Items.STONE_AXE, Items.STONE_HOE, Items.STONE_PICKAXE, Items.STONE_SHOVEL, Items.STONE_SWORD,
            Items.BOW, Items.CROSSBOW, Items.FISHING_ROD, Items.CARROT_ON_A_STICK, Items.WARPED_FUNGUS_ON_A_STICK
    };

    public static boolean isBreakableFunctionalBlock(Block block) {
        return (block instanceof BlockWithEntity || block instanceof CraftingTableBlock || block instanceof AnvilBlock) && block.getHardness() >= 0;
    }

    public static boolean canBreak(Item mainHand, BlockState state) {
        if (mainHand == null || state == null) {
            Reg.LOGGER.error("DigRestrictHelper/canBreak;mainHand==null||state==null");
            return false;
        }
        Block block = state.getBlock();
        float hardness = block.getHardness();
        Material material = state.getMaterial();
        ItemStack blockStack = state.getBlock().asItem().getDefaultStack();
        if (isBreakableFunctionalBlock(block)) return true;
        for (Block blk : BEAKABLE_MISC) if (blk == block) return true;
        if (mainHand.isEnchantable(new ItemStack(mainHand))) {//Is by tool
            for (Item itm : UNDIGABLE_TOOLS) if (itm == mainHand) return false;
            if (block == Blocks.BAMBOO) return true;
            if (mainHand == Reg.STONE_KNIFE || mainHand == Reg.FLINT_KNIFE)
                return block instanceof GrassBlock || block instanceof TallPlantBlock || material == Material.PLANT || material == Material.REPLACEABLE_PLANT || material == Material.REPLACEABLE_UNDERWATER_PLANT || material == Material.COBWEB;
            if (mainHand == Reg.STONE_CONE || mainHand == Reg.FLINT_CONE || mainHand == Reg.SHARP_BROKEN_BONE)
                return material == Material.PLANT || material == Material.REPLACEABLE_PLANT || material == Material.REPLACEABLE_UNDERWATER_PLANT || block.getSoundGroup(state) == BlockSoundGroup.GRAVEL || block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM || block == Blocks.BRICKS || block == Blocks.BRICK_SLAB || block == Blocks.BRICK_STAIRS || block == Blocks.DIRT_PATH;
            if (mainHand == Reg.FLINT_HATCHET)
                return material == Material.PLANT || material == Material.REPLACEABLE_PLANT || material == Material.REPLACEABLE_UNDERWATER_PLANT || block.getSoundGroup(state) == BlockSoundGroup.GRAVEL || block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM || material == Material.WOOD || material == Material.NETHER_WOOD;
            return true;
        }
        if (hardness <= 0.41 && hardness >= 0) return true;
        return material == Material.WOOL || blockStack.isIn(ItemTags.BUTTONS) || blockStack.isIn(ItemTags.WOODEN_PRESSURE_PLATES);
    }

}
