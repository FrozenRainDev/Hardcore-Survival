package biz.coolpage.hcs.item;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.util.HcsFactory;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;
/* need 3 tier definitions:
1. relative(registerTier: before<=, after>=, DAG algorithm)
2. absolute(level 0-4 Wood Stone Iron Diamond Netherite)
3. add my custom block in src/main/resources/data/minecraft/tags/blocks/needs_diamond_tool.json etc
*/

public final class HcsTiers { // HcsArmorMaterials

    // Use custom tags for all custom tiers to prevent hijacking vanilla tiers' requirements 
    // This ensures parallel tiers (like COPPER and FLINT_HATCHET) don't block each other's inherited mining abilities.
    // https://share.gemini.google/JJqfCkyBhni4
    public static final TagKey<Block> NEEDS_COPPER_TOOL = TagKey.create(Registries.BLOCK, HcsFactory.createResourceLocation( "needs_copper_tool"));
    public static final TagKey<Block> NEEDS_FLINT_TOOL = TagKey.create(Registries.BLOCK, HcsFactory.createResourceLocation( "needs_flint_tool"));
    public static final TagKey<Block> NEEDS_BONE_TOOL = TagKey.create(Registries.BLOCK, HcsFactory.createResourceLocation( "needs_bone_tool"));
    public static final TagKey<Block> NEEDS_ROCK_TOOL = TagKey.create(Registries.BLOCK, HcsFactory.createResourceLocation( "needs_rock_tool"));

    public static final Tier COPPER = TierSortingRegistry.registerTier(
            // params: absolute mining level; durability; miningSpeedMultiplier
            new ForgeTier(1 /* absolute mining level */, 96, 4.0F, 0.0F, 10,
                    NEEDS_COPPER_TOOL, () -> Ingredient.of(Items.COPPER_INGOT)),
            HcsFactory.createResourceLocation("copper"),
            List.of(Tiers.STONE)/* Putting STONE will no longer fail because we use a custom tag now. */
            , List.of(Tiers.IRON)),

    FLINT_HATCHET = TierSortingRegistry.registerTier(
            new ForgeTier(0, 4, 0.24F, 0.0F, 0,
                    NEEDS_FLINT_TOOL, () -> Ingredient.of(Items.FLINT)),
            HcsFactory.createResourceLocation("flint"),
            List.of(Tiers.STONE), List.of(Tiers.IRON)),

    FLINT_CONE = TierSortingRegistry.registerTier(
            new ForgeTier(0, 32, 0.48F, 0.0F, 0,
                    NEEDS_FLINT_TOOL, () -> Ingredient.of(Items.FLINT)),
            HcsFactory.createResourceLocation("flint_cone"),
            List.of(Tiers.WOOD), List.of(Tiers.STONE)),

    FLINT_WEAPON = TierSortingRegistry.registerTier(
            new ForgeTier(0, 48, 0.0F, 0.0F, 0,
                    NEEDS_FLINT_TOOL, () -> Ingredient.of(Items.FLINT)),
            HcsFactory.createResourceLocation("flint_weapon"),
            List.of(Tiers.WOOD), List.of(Tiers.STONE)),

    SHARP_BROKEN_BONE = TierSortingRegistry.registerTier(
            new ForgeTier(0, 8, 0.36F, 0.0F, 0,
                    NEEDS_BONE_TOOL, () -> Ingredient.of(Items.BONE)),
            HcsFactory.createResourceLocation("sharp_broken_bone"),
            List.of(Tiers.WOOD), List.of(Tiers.STONE)),

    STONE_CONE = TierSortingRegistry.registerTier(
            new ForgeTier(0, 8, 0.5F, 0.0F, 0,
                    NEEDS_ROCK_TOOL, () -> Ingredient.of(Hcs.ROCK.get())),
            HcsFactory.createResourceLocation("stone_cone"),
            List.of(Tiers.WOOD), List.of(Tiers.STONE)),

    STONE_WEAPON = TierSortingRegistry.registerTier(
            new ForgeTier(0, 12, 0.0F, 0.0F, 0,
                    NEEDS_ROCK_TOOL, () -> Ingredient.of(Hcs.ROCK.get())),
            HcsFactory.createResourceLocation("stone_weapon"),
            List.of(Tiers.WOOD), List.of(Tiers.STONE));
}