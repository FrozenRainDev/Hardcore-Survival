package biz.coolpage.hcs.recipe;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import org.jetbrains.annotations.NotNull;

public record BetterBrewingRecipe(Potion input, Item ingredient, Potion output) implements IBrewingRecipe {

    @Override
    public boolean isInput(@NotNull ItemStack stack) {
        return PotionUtils.getPotion(stack) == input;
    }

    @Override
    public boolean isIngredient(@NotNull ItemStack stack) {
        return stack.getItem() == ingredient;
    }

    @Override
    public @NotNull ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack ingredient) {
        if (!isInput(input) || !isIngredient(ingredient)) return ItemStack.EMPTY;
        ItemStack res = input.copy();
        PotionUtils.setPotion(res, output);
        return res;
    }
}
