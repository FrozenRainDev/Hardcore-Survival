package com.hcs.recipe;

import com.hcs.Reg;
import com.hcs.util.WorldHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.tag.ItemTags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static com.hcs.util.CommUtil.optElse;

public class CustomDryingRackRecipe {
    public static Predicate<String> HAS_COOKED = name -> name.contains("cooked_") || name.contains("baked_") || name.contains("roasted_") || name.contains("steamed_") || name.contains("fried_");

    public static Item getOutput(Item input) {
        if (input == null) return Items.AIR;
        //Give priority to special recipes
        if (input == Items.ROTTEN_FLESH) return Items.LEATHER;
        if (input == Items.KELP) return Items.DRIED_KELP;
        //Reject items that don't meet standard
        FoodComponent component = input.getFoodComponent();
        String name = input.getTranslationKey();
        ItemStack stack = input.getDefaultStack();
        if (name.contains("jerky") || name.contains("viscera") || name.contains("innards") || input == Items.PUFFERFISH)
            return Items.AIR;
        if (!input.isFood() || component == null) return Items.AIR;
        if (!component.isMeat() && !stack.isIn(ItemTags.FISHES)) return Items.AIR;
        //Return corresponding jerky
        boolean isCooked = HAS_COOKED.test(name);
        Item cookedItem = isCooked ? input : getCooked(input);
        FoodComponent cookedComponent = cookedItem.getFoodComponent();
        boolean isSmall = optElse(cookedComponent, FoodComponents.COOKED_CHICKEN).getHunger() < 6 || cookedItem == Items.COOKED_SALMON;
        if (isCooked) {
            if (isSmall) return Reg.SMALL_JERKY;
            return Reg.JERKY;
        } else {
            if (isSmall) return Reg.RAW_SMALL_JERKY;
            return Reg.RAW_JERKY;
        }
    }

    public static @NotNull Item getCooked(Item rawMaterial) {
        if (WorldHelper.theWorld == null) {
            Reg.LOGGER.error("CustomDryingRackRecipe/getCooked;RotHelper.theWorld==null");
            return rawMaterial;
        }
        ItemStack stack = new ItemStack(rawMaterial);
        Inventory inventory = new SimpleInventory(stack);
        return optElse(RecipeManager.createCachedMatchGetter(RecipeType.CAMPFIRE_COOKING).getFirstMatch(inventory, WorldHelper.theWorld).map((recipe) -> recipe.craft(inventory, WorldHelper.theWorld.getRegistryManager())).orElse(stack).getItem(), Items.AIR);
    }

}
