package biz.coolpage.hcs.recipe;

import biz.coolpage.hcs.Reg;
import net.minecraft.block.SaplingBlock;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
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

public class SaplingToStickRecipe extends SpecialCraftingRecipe {

    public SaplingToStickRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(@NotNull CraftingInventory inventory, World world) {
        boolean b1 = false;
        int count = 0;
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                ++count;
                if (stack.getItem() instanceof BlockItem blockItem) {
                    if (blockItem.getBlock() instanceof SaplingBlock) b1 = true;
                }
            }
        }
        return b1 && count == 1;
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory, DynamicRegistryManager dynamicRegistryManager) {
        return Items.STICK.getDefaultStack();
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
        return Reg.SAPLING_TO_STICK_RECIPE;
    }
}
