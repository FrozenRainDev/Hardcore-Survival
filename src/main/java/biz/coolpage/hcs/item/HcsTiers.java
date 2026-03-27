package biz.coolpage.hcs.item;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.util.HcsFactory;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;
/* need 3 tier definitions:
1. relative(registerTier: before<=, after>=, DAG algorithm)
2. absolute(level 0-4 Wood Stone Iron Diamond Netherite)
3. add my custom block in src/main/resources/data/minecraft/tags/blocks/needs_diamond_tool.json etc
*/

public final class HcsTiers { // HcsArmorMaterials
    public static final Tier COPPER = TierSortingRegistry.registerTier(
            // params: absolute mining level; durability; miningSpeedMultiplier
            new ForgeTier(2 /* absolute mining level */, 48, 4.0F, 0.0F, 10,
                    BlockTags.NEEDS_IRON_TOOL, () -> Ingredient.of(Items.COPPER_INGOT)),
            HcsFactory.createResourceLocation("copper"),
            List.of(Tiers.STONE), List.of(Tiers.DIAMOND)),

    FLINT_HATCHET = TierSortingRegistry.registerTier(
            new ForgeTier(0, 4, 0.24F, 0.0F, 0,
                    BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.of(Items.FLINT)),
            HcsFactory.createResourceLocation("flint"),
            List.of(Tiers.STONE), List.of(Tiers.IRON)),

    FLINT_CONE = TierSortingRegistry.registerTier(
            new ForgeTier(0, 12, 0.48F, 0.0F, 0,
                    BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.of(Items.FLINT)),
            HcsFactory.createResourceLocation("flint_cone"),
            List.of(Tiers.WOOD), List.of(Tiers.STONE)),

    FLINT_WEAPON = TierSortingRegistry.registerTier(
            new ForgeTier(0, 24, 0.0F, 0.0F, 0,
                    BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.of(Items.FLINT)),
            HcsFactory.createResourceLocation("flint_weapon"),
            List.of(Tiers.WOOD), List.of(Tiers.STONE)),

    SHARP_BROKEN_BONE = TierSortingRegistry.registerTier(
            new ForgeTier(0, 8, 0.36F, 0.0F, 0,
                    BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.of(Items.BONE)),
            HcsFactory.createResourceLocation("sharp_broken_bone"),
            List.of(Tiers.WOOD), List.of(Tiers.STONE)),

    STONE_CONE = TierSortingRegistry.registerTier(
            new ForgeTier(0, 8, 0.5F, 0.0F, 0,
                    BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.of(Hcs.ROCK)),
            HcsFactory.createResourceLocation("stone_cone"),
            List.of(Tiers.WOOD), List.of(Tiers.STONE)),

    STONE_WEAPON = TierSortingRegistry.registerTier(
            new ForgeTier(0, 16, 0.0F, 0.0F, 0,
                    BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.of(Hcs.ROCK)),
            HcsFactory.createResourceLocation("stone_weapon"),
            List.of(Tiers.WOOD), List.of(Tiers.STONE));
}