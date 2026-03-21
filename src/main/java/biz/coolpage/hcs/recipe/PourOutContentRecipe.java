package biz.coolpage.hcs.recipe;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.util.RotHelper;
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

public class PourOutContentRecipe extends CustomRecipe {
    public PourOutContentRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(@NotNull CraftingContainer inventory, Level world) {
        int count = 0;
        boolean hasItemMatch = false;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.isEmpty()) continue;
            ++count;
            Item item = stack.getItem();
            String name = item.getDescriptionId();
            if (RotHelper.canRot(item)) {
                if (RotHelper.getFresh(world, stack) <= 0.0001F && RotHelper.getPackageType(name) == 1 && (name.contains("stew") || name.contains("salad") || name.contains("soup") || name.contains("bucket") || name.contains("bottle") || name.contains("juice")))
                    hasItemMatch = true;
            } else if (item == Hcs.SALTWATER_BOTTLE) hasItemMatch = true;
        }
        return count == 1 && hasItemMatch;
    }


    @Override
    public ItemStack assemble(@NotNull CraftingContainer inventory, RegistryAccess registryManager) {
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            Item item = stack.getItem();
            String name = item.getDescriptionId();
            if (name.contains("stew") || name.contains("salad") || name.contains("soup"))
                return new ItemStack(Items.BOWL);
            if (name.contains("bucket")) return new ItemStack(Items.BUCKET);
            if (name.contains("bottle") || name.contains("juice"))
                return new ItemStack(Items.GLASS_BOTTLE);
        }
        return new ItemStack(Items.GLASS_BOTTLE);
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
        return Hcs.POUR_OUT_CONTENT_RECIPE;
    }
}