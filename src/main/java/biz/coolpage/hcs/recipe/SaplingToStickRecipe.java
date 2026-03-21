package biz.coolpage.hcs.recipe;

import biz.coolpage.hcs.Hcs;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SaplingToStickRecipe extends CustomRecipe {

    public SaplingToStickRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(@NotNull CraftingContainer inventory, Level world) {
        boolean b1 = false;
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
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
    public ItemStack assemble(CraftingContainer inventory, RegistryAccess dynamicRegistryManager) {
        return Items.STICK.getDefaultInstance();
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
        return Hcs.SAPLING_TO_STICK_RECIPE;
    }
}