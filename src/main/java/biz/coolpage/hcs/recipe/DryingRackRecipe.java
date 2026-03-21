package biz.coolpage.hcs.recipe;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.util.CommUtil;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class DryingRackRecipe {
    public static Predicate<String> HAS_COOKED = name -> name.contains("cooked_") || name.contains("baked_") || name.contains("roasted_") || name.contains("steamed_") || name.contains("fried_");
    public static Predicate<Item> IS_RAW = item -> item != null && getCooked(item) != item;
    public static Predicate<Item> IS_RAW_MEAT = IS_RAW.and(item -> item.getFoodProperties() != null && item.getFoodProperties().isMeat());

    public static Item getOutput(Item input) {
        if (input == null) return Items.AIR;
        //Give priority to special recipes
        if (input == Items.ROTTEN_FLESH) return Items.LEATHER;
        if (input == Items.KELP) return Items.DRIED_KELP;
        //Reject items that don't meet standard
        FoodProperties component = input.getFoodProperties();
        String name = input.getDescriptionId();
        ItemStack stack = input.getDefaultInstance();
        if (name.contains("jerky") || name.contains("viscera") || name.contains("innards") || input == Items.PUFFERFISH)
            return Items.AIR;
        if (!input.isEdible() || component == null) return Items.AIR;
        if (!component.isMeat() && !stack.is(ItemTags.FISHES)) return Items.AIR;
        //Return corresponding jerky
        boolean isCooked = HAS_COOKED.test(name);
        Item cookedItem = isCooked ? input : getCooked(input);
        FoodProperties cookedComponent = cookedItem.getFoodProperties();
        boolean isSmall = CommUtil.optElse(cookedComponent, Foods.COOKED_CHICKEN).getNutrition() < 6 || cookedItem == Items.COOKED_SALMON;
        if (isCooked) {
            if (isSmall) return Hcs.SMALL_JERKY;
            return Hcs.JERKY;
        } else {
            if (isSmall) return Hcs.RAW_SMALL_JERKY;
            return Hcs.RAW_JERKY;
        }
    }

    public static @NotNull Item getCooked(Item rawMaterial) {
        if (WorldHelper.cannotGetServerWorld()) {
            Hcs.error("CustomDryingRackRecipe/getCooked;RotHelper.theWorld==null");
            return rawMaterial;
        }
        Level world = WorldHelper.getServerWorld();
        ItemStack stack = new ItemStack(rawMaterial);
        Container inventory = new SimpleContainer(stack);
        return CommUtil.optElse(world.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, inventory, world).map((recipe) -> recipe.assemble(inventory, world.registryAccess())).orElse(stack).getItem(), rawMaterial);
    }

}