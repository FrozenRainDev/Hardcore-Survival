package biz.coolpage.hcs.recipe;

import biz.coolpage.hcs.Hcs;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class PetalsSaladRecipe extends CustomRecipe {
    public PetalsSaladRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(@NotNull CraftingContainer inventory, Level world) {
        int flower = 0, bowl = 0, count = 0;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                ++count;
                if (stack.is(ItemTags.FLOWERS) && !stack.is(Items.WITHER_ROSE)) ++flower;
                if (stack.is(Items.BOWL)) ++bowl;
            }
        }
        return flower == 3 && bowl == 1 && count == 4;
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory, RegistryAccess dynamicRegistryManager) {
        return Hcs.PETALS_SALAD.get().getDefaultInstance();
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
        return Hcs.PETALS_SALAD_RECIPE.get();
    }
}