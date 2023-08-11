package com.hcs.mixin.recipe;
import com.hcs.Reg;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingRecipeRegistry.class)
public class BrewingRecipeRegistryMixin {
    @Inject(at = @At("HEAD"),method="registerDefaults")
    private static void registerDefaults(CallbackInfo callbackInfo){
        BrewingRecipeRegistryMixin.registerPotionRecipe(Potions.AWKWARD, Items.IRON_NUGGET, Reg.IRONSKIN_POTION);
        BrewingRecipeRegistryMixin.registerPotionRecipe(Potions.AWKWARD, Items.SALMON, Reg.RETURN_POTION);
        BrewingRecipeRegistryMixin.registerPotionRecipe(Potions.AWKWARD, Items.MANGROVE_PROPAGULE, Reg.MINING_POTION);
        BrewingRecipeRegistryMixin.registerPotionRecipe(Reg.IRONSKIN_POTION, Items.GLOWSTONE_DUST, Reg.STRONG_IRONSKIN_POTION);
        BrewingRecipeRegistryMixin.registerPotionRecipe(Reg.IRONSKIN_POTION, Items.REDSTONE, Reg.LONG_IRONSKIN_POTION);
        BrewingRecipeRegistryMixin.registerPotionRecipe(Reg.MINING_POTION, Items.GLOWSTONE_DUST, Reg.STRONG_MINING_POTION);
        BrewingRecipeRegistryMixin.registerPotionRecipe(Reg.MINING_POTION, Items.REDSTONE, Reg.LONG_MINING_POTION);
    }

//    @Invoker("registerPotionRecipe")
    @SuppressWarnings("EmptyMethod")
    @Shadow
    public static void registerPotionRecipe(Potion input, Item item, Potion output){}
}
