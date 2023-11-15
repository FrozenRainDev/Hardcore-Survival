package biz.coolpage.hcs.recipe;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class PourOutContentRecipe extends SpecialCraftingRecipe {
    public PourOutContentRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(@NotNull CraftingInventory inventory, World world) {
        int count = 0;
        boolean hasItemMatch = false;
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            if (stack == null || stack.isEmpty()) continue;
            ++count;
            Item item = stack.getItem();
            String name = item.getName().toString();
            if (RotHelper.canRot(item)) {
                if (RotHelper.getFresh(world, stack) <= 0.0001F && RotHelper.getPackageType(name) == 1 && (name.contains("stew") || name.contains("salad") || name.contains("soup") || name.contains("bucket") || name.contains("bottle") || name.contains("juice")))
                    hasItemMatch = true;
            } else if (item == Reg.SALTWATER_BOTTLE) hasItemMatch = true;
        }
        return count == 1 && hasItemMatch;
    }


    @Override
    public ItemStack craft(@NotNull CraftingInventory inventory, DynamicRegistryManager registryManager) {
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            Item item = stack.getItem();
            String name = item.getName().toString();
            if (name.contains("stew") || name.contains("salad") || name.contains("soup"))
                return new ItemStack(Items.BOWL);
            if (name.contains("bucket")) return new ItemStack(Items.BUCKET);
            if (name.contains("bottle") || name.contains("juice"))
                return new ItemStack(Items.GLASS_BOTTLE);
        }
        return new ItemStack(Items.GLASS_BOTTLE);
    }


    @Override
    public boolean fits(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Reg.POUR_OUT_CONTENT_RECIPE;
    }
}
