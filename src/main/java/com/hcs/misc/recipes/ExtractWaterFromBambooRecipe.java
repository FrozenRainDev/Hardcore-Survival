package com.hcs.misc.recipes;

import com.hcs.main.Reg;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ExtractWaterFromBambooRecipe extends SpecialCraftingRecipe {

    public static int slotBamboo = 0;

    public ExtractWaterFromBambooRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(@NotNull CraftingInventory inventory, World world) {
        boolean b1 = false, b2 = false, b3 = false;
        int count = 0;
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            Item item = stack.getItem();
            if (!stack.isEmpty()) {
                ++count;
                if (stack.getItem() == Items.BAMBOO) b1 = true;
                else if (item == Items.GLASS_BOTTLE) b2 = true;
                else if (item == Reg.FLINT_KNIFE || item == Reg.STONE_KNIFE) b3 = true;
            }
        }
        return b1 && b2 && b3 && count == 3;
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory, DynamicRegistryManager dynamicRegistryManager) {
        return Items.POTION.getDefaultStack();
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(@NotNull CraftingInventory inventory) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        list.set(slotBamboo, new ItemStack(Reg.WORM, 1));
        return list;
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return Reg.EXTRACT_WATER_FROM_BAMBOO_RECIPE;
    }
}
