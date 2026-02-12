package biz.coolpage.hcs.data;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

// https://www.bilibili.com/video/BV1Vw411s7h4?t=814.0&p=12
public class HcsRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public HcsRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> pWriter) {

    }
}
