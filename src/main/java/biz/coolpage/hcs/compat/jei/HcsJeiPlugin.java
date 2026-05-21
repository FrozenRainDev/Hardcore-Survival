package biz.coolpage.hcs.compat.jei;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.util.HcsFactory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class HcsJeiPlugin implements IModPlugin {

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return HcsFactory.createResourceLocation("jei_plugin");
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        // Create string ingredient (Slot 0, 2)
        Ingredient stringIngredient = Ingredient.of(Items.STRING);

        // Create water bottle ingredient handling Vanilla, Cold, and Purified bottles (Slot 1, 4, 7)
        ItemStack vanillaWaterBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        Ingredient bottleIngredient = Ingredient.of(
                vanillaWaterBottle,
                new ItemStack(Hcs.COLD_WATER_BOTTLE.get()),
                new ItemStack(Hcs.PURIFIED_WATER_BOTTLE.get())
        );

        // Create leather ingredient (Slot 3, 5, 6, 8)
        Ingredient leatherIngredient = Ingredient.of(Items.LEATHER);

        // Construct the 3x3 recipe grid (9 slots in total)
        NonNullList<Ingredient> inputs = NonNullList.of(Ingredient.EMPTY,
                stringIngredient, bottleIngredient, stringIngredient,
                leatherIngredient, bottleIngredient, leatherIngredient,
                leatherIngredient, bottleIngredient, leatherIngredient
        );

        // Create a dummy shaped recipe solely for JEI display purposes
        ShapedRecipe dummyRecipe = new ShapedRecipe(
                HcsFactory.createResourceLocation( "jei_dummy_hot_water_bottle"),
                "", // Group (empty is fine)
                CraftingBookCategory.MISC,
                3, // Width
                3, // Height
                inputs,
                Hcs.WATER_BAG.get().getDefaultInstance()
        );

        // Register the dummy recipe to JEI's vanilla crafting category
        registration.addRecipes(RecipeTypes.CRAFTING, List.of(dummyRecipe));
    }
}