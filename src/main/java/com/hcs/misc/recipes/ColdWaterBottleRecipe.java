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

public class ColdWaterBottleRecipe extends SpecialCraftingRecipe {

    public ColdWaterBottleRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(@NotNull CraftingInventory inventory, World world) {
        boolean b1 = false, b2 = false;
        int count = 0;
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            Item item = stack.getItem();
            if (!stack.isEmpty()) {
                ++count;
                //Watter bottle
                if (stack.getItem() == Items.POTION && stack.getOrCreateNbt().equals(Items.POTION.getDefaultStack().getOrCreateNbt()))
                    b1 = true;
                else if (item == Items.SNOWBALL) b2 = true;
            }
        }
        return b1 && b2 && count == 2;
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory, DynamicRegistryManager dynamicRegistryManager) {
        return Reg.COLD_WATER_BOTTLE.getDefaultStack();
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(@NotNull CraftingInventory inventory) {
        return DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Reg.COLD_WATER_BOTTLE_RECIPE;
    }
}
