package com.hcs.misc.recipes;

import com.hcs.main.Reg;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class SpikedClubRecipe extends SpecialCraftingRecipe {
    public SpikedClubRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    private NbtCompound nbt;

    @Override
    public boolean matches(@NotNull CraftingInventory inventory, World world) {
        int club = 0, spike = 0, count = 0;
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                ++count;
                if (stack.isOf(Items.WOODEN_SWORD)) {
                    ++club;
                    this.nbt = stack.getOrCreateNbt();
                }
                if (stack.isOf(Reg.SHARP_BROKEN_BONE)) ++spike;
            }
        }
        return club == 1 && spike == 1 && count == 2;
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory, DynamicRegistryManager dynamicRegistryManager) {
        ItemStack output = Reg.SPIKED_CLUB.getDefaultStack();
        output.setNbt(this.nbt);
        return output;
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
        return Reg.SPIKED_CLUB_RECIPE;
    }

}
