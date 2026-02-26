package biz.coolpage.hcs.mixin.recipe;

import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
    @Mutable
    @Final
    @Shadow
    ItemStack result;

    @Unique
    private static Level theLevel = null;
    @Unique
    private static float freshSum = 0.0F;
    @Unique
    private static int freshCou = 0;

    @Inject(method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"))
    private void matches(@NotNull CraftingContainer container, Level level, CallbackInfoReturnable<Boolean> cir) {
        theLevel = level;
        freshSum = 0.0F;
        freshCou = 0;
        for (int i = 0; i < container.getContainerSize(); ++i) {
            ItemStack stack = container.getItem(i);
            if (RotHelper.canRot(stack.getItem())) {
                freshSum += RotHelper.getFresh(level, stack);
                ++freshCou;
            }
        }
    }

    @Inject(method = "getResultItem", at = @At("HEAD"), cancellable = true)
    private void getResultItemInject(RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir) {
        applyRotLogic(cir);
    }

    @Inject(method = "assemble", at = @At("HEAD"), cancellable = true)
    private void assembleInject(CraftingContainer container, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir) {
        applyRotLogic(cir);
    }

    @Unique
    private void applyRotLogic(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stackOut = this.result.copy();
        if (RotHelper.canRot(stackOut.getItem()) && theLevel != null && freshCou > 0) {
            RotHelper.setFresh(theLevel, stackOut, Math.max(freshSum / freshCou, 0.1F));
            cir.setReturnValue(stackOut);
        }
    }
}