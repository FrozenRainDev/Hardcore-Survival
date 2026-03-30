package biz.coolpage.hcs.recipe;

import biz.coolpage.hcs.Hcs;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ExtractWaterFromSnowRecipe extends CustomRecipe {
    public ExtractWaterFromSnowRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(@NotNull CraftingContainer inventory, Level world) {
        boolean b1 = false, b2 = false;
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            Item item = stack.getItem();
            if (!stack.isEmpty()) {
                ++count;
                if (item == Items.SNOWBALL) b1 = true;
                else if (item == Items.GLASS_BOTTLE) b2 = true;
            }
        }
        return b1 && b2 && count == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory, RegistryAccess dynamicRegistryManager) {
        return Items.POTION.getDefaultInstance();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer inventory) {
        return NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return Hcs.EXTRACT_WATER_FROM_SNOW_RECIPE.get();
    }
}