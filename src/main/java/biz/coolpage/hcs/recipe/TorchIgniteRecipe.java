package biz.coolpage.hcs.recipe;

import biz.coolpage.hcs.Reg;
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

public class TorchIgniteRecipe extends SpecialCraftingRecipe {
    private int damageFAS = 0, currSlotFAS = 0;
    private boolean isCrudeTorch = false;

    public TorchIgniteRecipe(Identifier id, CraftingRecipeCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(@NotNull CraftingInventory inventory, World world) {
        int flintSteel = 0, unlitTorch = 0, others = 0;
        this.isCrudeTorch = false;
        this.damageFAS = 0;
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isOf(Items.FLINT_AND_STEEL)) {
                ++flintSteel;
                this.damageFAS = stack.getDamage() + 1;
                this.currSlotFAS = i;
            } else if (stack.isOf(Reg.UNLIT_TORCH_ITEM) || stack.isOf(Reg.CRUDE_TORCH_ITEM)) {
                this.isCrudeTorch = stack.isOf(Reg.CRUDE_TORCH_ITEM);
                ++unlitTorch;
            } else if (!stack.isEmpty()) ++others;
        }
        return flintSteel == 1 && unlitTorch == 1 && others == 0;
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory, DynamicRegistryManager dynamicRegistryManager) {
        return isCrudeTorch ? Reg.BURNING_CRUDE_TORCH_ITEM.getDefaultStack() : Items.TORCH.getDefaultStack();
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(@NotNull CraftingInventory inventory) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        if (this.damageFAS <= Items.FLINT_AND_STEEL.getMaxDamage()) {
            ItemStack stack = new ItemStack(Items.FLINT_AND_STEEL);
            stack.setDamage(this.damageFAS);
            list.set(this.currSlotFAS >= inventory.size() ? 0 : this.currSlotFAS, stack);
        }
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Reg.TORCH_IGNITE_RECIPE;
    }
}
