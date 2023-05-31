package com.hcs.misc.recipes;

import com.hcs.main.Reg;
import net.minecraft.inventory.CraftingInventory;
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

public class HotWaterBottleRecipe extends SpecialCraftingRecipe {

    public HotWaterBottleRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(@NotNull CraftingInventory inventory, World world) {
        boolean matchAll = true;
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            matchAll = matchAll && (switch (i) {
                case 0, 2 ->
                        stack.getItem().getTranslationKey().contains(".string") || stack.getItem().getTranslationKey().contains("_string");
                case 1, 4, 7 ->
                        (stack.getItem() == Items.POTION && stack.getOrCreateNbt().equals(Items.POTION.getDefaultStack().getOrCreateNbt())) || stack.isOf(Reg.COLD_WATER_BOTTLE) || stack.isOf(Reg.PURIFIED_WATER_BOTTLE);
                case 3, 5, 6, 8 -> stack.isOf(Items.LEATHER);
                default -> true;
            });
            /*
             string  bottle  string
             leather bottle leather
             leather bottle leather
             */
        }
        return matchAll;
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory, DynamicRegistryManager dynamicRegistryManager) {
        //Be aware of the overriding of HOT_WATER_BOTTLE.getDefaultStack()
        return Reg.HOT_WATER_BOTTLE.getDefaultStack();
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingInventory inventory) {
        return DefaultedList.copyOf(ItemStack.EMPTY,
                ItemStack.EMPTY, Items.GLASS_BOTTLE.getDefaultStack(), ItemStack.EMPTY,
                ItemStack.EMPTY, Items.GLASS_BOTTLE.getDefaultStack(), ItemStack.EMPTY,
                ItemStack.EMPTY, Items.GLASS_BOTTLE.getDefaultStack(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Reg.HOT_WATER_BOTTLE_RECIPE;
    }
}
