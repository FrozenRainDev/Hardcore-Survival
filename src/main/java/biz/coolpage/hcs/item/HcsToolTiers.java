package biz.coolpage.hcs.item;

import biz.coolpage.hcs.util.HcsFactory;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;

public final class HcsToolTiers {
    public static final Tier FLINT = TierSortingRegistry.registerTier(
            new ForgeTier(1, 1145, 5F, 4F, 25,
                    BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.of(Items.FLINT)),
            HcsFactory.createResourceLocation("flint"),
            List.of(Tiers.STONE),
            List.of(Tiers.STONE) // todo try IRON......
    );

    /* need 3 tier definitions:
    1. relative(registerTier: before<=, after>=, DAG algorithm)
    2. absolute(level 0-4 Wood Stone Iron Diamond Netherite)
    3. add my custom block in src/main/resources/data/minecraft/tags/blocks/needs_diamond_tool.json etc
    */

    // do not use custom registry method currently
    //    private static Tier
}
