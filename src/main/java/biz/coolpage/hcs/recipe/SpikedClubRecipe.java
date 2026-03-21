package biz.coolpage.hcs.recipe;

import biz.coolpage.hcs.Hcs;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SpikedClubRecipe extends CustomRecipe {
    public SpikedClubRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    private CompoundTag nbt;

    @Override
    public boolean matches(@NotNull CraftingContainer inventory, Level world) {
        int club = 0, spike = 0, count = 0;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                ++count;
                if (stack.is(Items.WOODEN_SWORD)) {
                    ++club;
                    this.nbt = stack.getOrCreateTag();
                }
                if (stack.is(Hcs.SHARP_BROKEN_BONE) || stack.is(Hcs.SHARP_FLINT)) ++spike;
            }
        }
        return club == 1 && spike == 1 && count == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingInventory, RegistryAccess dynamicRegistryManager) {
        ItemStack output = Hcs.SPIKED_CLUB.getDefaultInstance();
        output.setTag(this.nbt);
        return output;
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
        return Hcs.SPIKED_CLUB_RECIPE;
    }
}