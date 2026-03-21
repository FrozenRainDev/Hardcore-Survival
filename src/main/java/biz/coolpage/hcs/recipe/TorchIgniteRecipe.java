package biz.coolpage.hcs.recipe;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.item.BurningCrudeTorchItem;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class TorchIgniteRecipe extends CustomRecipe {
    private int flintSteelDamage = 0, flintSteelSlot = 0;
    private boolean isCrudeTorch = false;

    public TorchIgniteRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(@NotNull CraftingContainer inventory, Level world) {
        int flintSteel = 0, unlitTorch = 0, others = 0;
        this.isCrudeTorch = false;
        this.flintSteelDamage = 0;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(Items.FLINT_AND_STEEL)) {
                ++flintSteel;
                this.flintSteelDamage = stack.getDamageValue() + 1;
                this.flintSteelSlot = i;
            } else if (stack.is(Hcs.UNLIT_TORCH_ITEM) || stack.is(Hcs.CRUDE_TORCH_ITEM)) {
                this.isCrudeTorch = stack.is(Hcs.CRUDE_TORCH_ITEM);
                ++unlitTorch;
            } else if (!stack.isEmpty()) ++others;
        }
        return flintSteel == 1 && unlitTorch == 1 && others == 0;
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory, RegistryAccess dynamicRegistryManager) {
        if (isCrudeTorch && !WorldHelper.cannotGetServerWorld()) {
            ItemStack stack = Hcs.BURNING_CRUDE_TORCH_ITEM.getDefaultInstance();
            BurningCrudeTorchItem.initDurData(WorldHelper.getServerWorld(), stack);
            return stack;
        }
        return Items.TORCH.getDefaultInstance();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer inventory) {
        NonNullList<ItemStack> list = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        if (this.flintSteelDamage <= Items.FLINT_AND_STEEL.getMaxDamage()) {
            ItemStack stack = new ItemStack(Items.FLINT_AND_STEEL);
            stack.setDamageValue(this.flintSteelDamage);
            list.set(this.flintSteelSlot >= inventory.getContainerSize() ? 0 : this.flintSteelSlot, stack);
        }
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Hcs.TORCH_IGNITE_RECIPE;
    }
}