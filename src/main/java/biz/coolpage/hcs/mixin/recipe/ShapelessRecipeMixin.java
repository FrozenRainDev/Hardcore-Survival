package biz.coolpage.hcs.mixin.recipe;

import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeMixin {

    @Mutable
    @Final
    @Shadow
    ItemStack output;

    @SuppressWarnings("SameReturnValue")
    @Shadow
    public DefaultedList<Ingredient> getIngredients() {
        return null;
    }

    @Unique
    private static World theWorld = null;
    @Unique
    private static float freshSum = 0.0F;
    @Unique
    private static int freshCou = 0;

    public ShapelessRecipeMixin(ItemStack output) {
        this.output = output;
    }

    @Inject(at = @At("HEAD"), method = "matches(Lnet/minecraft/inventory/CraftingInventory;Lnet/minecraft/world/World;)Z")
    private void matches(CraftingInventory inventory, World world, CallbackInfoReturnable<Boolean> cir) {
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

    @Inject(at = @At("HEAD"), method = "getOutput", cancellable = true)
    private void getOutPut(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stackOut = this.output.copy();
        if (RotHelper.canRot(stackOut.getItem()) && theWorld != null && freshCou > 0) {
            RotHelper.setFresh(theWorld, stackOut, Math.max(freshSum / freshCou, 0.1F));
            cir.setReturnValue(stackOut);
        }
    }

    @Inject(at = @At("HEAD"), method = "craft*", cancellable = true)
    private void craft(CallbackInfoReturnable<ItemStack> cir) {
        getOutPut(cir);
    }

}
