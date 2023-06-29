package com.hcs.mixin.recipe;

import com.hcs.util.RotHelper;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
    @Mutable
    @Final
    @Shadow
    ItemStack output;

    @Shadow
    public DefaultedList<Ingredient> getIngredients() {
        return null;
    }

    private static World theWorld = null;
    private static float freshSum = 0.0F;
    private static int freshCou = 0;

    public ShapedRecipeMixin(ItemStack output) {
        this.output = output;
    }

    @Inject(method = "matches(Lnet/minecraft/inventory/CraftingInventory;Lnet/minecraft/world/World;)Z", at = @At("HEAD"))
    private void matches(@NotNull CraftingInventory inventory, World world, CallbackInfoReturnable<Boolean> cir) {
        theWorld = world;
        freshSum = 0.0F;
        freshCou = 0;
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack stack = inventory.getStack(i);
            if (RotHelper.canRot(stack.getItem())) {
                freshSum += RotHelper.getFresh(world, stack);
                ++freshCou;
            }
        }
    }

    @Inject(method = "getOutput", at = @At("HEAD"), cancellable = true)
    private void getOutPut(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stackOut = this.output.copy();
        if (RotHelper.canRot(stackOut.getItem()) && theWorld != null && freshCou > 0) {
            RotHelper.setFresh(theWorld, stackOut, Math.max(freshSum / freshCou, 0.1F));
            cir.setReturnValue(stackOut);
        }
    }

    @Inject(method = "craft*", at = @At("HEAD"), cancellable = true)
    private void craft(CallbackInfoReturnable<ItemStack> cir) {
        getOutPut(cir);
    }

}
