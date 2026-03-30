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

public class ExtractWaterFromBambooRecipe extends CustomRecipe {
    public static final int slotBamboo = 0;

    public ExtractWaterFromBambooRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(@NotNull CraftingContainer inventory, Level world) {
        boolean b1 = false, b2 = false, b3 = false;
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            Item item = stack.getItem();
            if (!stack.isEmpty()) {
                ++count;
                if (stack.getItem() == Items.BAMBOO) b1 = true;
                else if (item == Items.GLASS_BOTTLE) b2 = true;
                else if (item == Hcs.FLINT_KNIFE.get() || item == Hcs.STONE_KNIFE.get()) b3 = true;
            }
        }
        return b1 && b2 && b3 && count == 3;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingInventory, RegistryAccess dynamicRegistryManager) {
        return Items.POTION.getDefaultInstance();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer inventory) {
        NonNullList<ItemStack> list = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        list.set(slotBamboo, new ItemStack(Hcs.WORM.get(), 1));
        return list;
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return Hcs.EXTRACT_WATER_FROM_BAMBOO_RECIPE.get();
    }
}