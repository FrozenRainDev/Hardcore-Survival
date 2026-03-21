package biz.coolpage.hcs.recipe;

import biz.coolpage.hcs.Hcs;
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

public class HotWaterBottleRecipe extends CustomRecipe {

    public HotWaterBottleRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(@NotNull CraftingContainer inventory, Level world) {
        boolean matchAll = true;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            matchAll = matchAll && (switch (i) {
                case 0, 2 ->
                        stack.getItem().getDescriptionId().contains(".string") || stack.getItem().getDescriptionId().contains("_string");
                case 1, 4, 7 ->
                        (stack.getItem() == Items.POTION && stack.getOrCreateTag().equals(Items.POTION.getDefaultInstance().getOrCreateTag())) || stack.is(Hcs.COLD_WATER_BOTTLE) || stack.is(Hcs.PURIFIED_WATER_BOTTLE);
                case 3, 5, 6, 8 -> stack.is(Items.LEATHER);
                default -> true;
            });
            /*
             string  bottle  string
             leather bottle leather
             leather bottle leather
             */
        }
        return matchAll;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingInventory, RegistryAccess dynamicRegistryManager) {
        return Hcs.HOT_WATER_BOTTLE.getDefaultInstance();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inventory) {
        return NonNullList.of(ItemStack.EMPTY,
                ItemStack.EMPTY, Items.GLASS_BOTTLE.getDefaultInstance(), ItemStack.EMPTY,
                ItemStack.EMPTY, Items.GLASS_BOTTLE.getDefaultInstance(), ItemStack.EMPTY,
                ItemStack.EMPTY, Items.GLASS_BOTTLE.getDefaultInstance(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Hcs.HOT_WATER_BOTTLE_RECIPE;
    }
}