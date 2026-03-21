package biz.coolpage.hcs.mixin.recipe;

import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapelessRecipe.class)
public abstract class ShapelessRecipeMixin {
    @Mutable
    @Final
    @Shadow
    private ItemStack result;

    @Unique
    private static Level theWorld = null;
    @Unique
    private static float freshSum = 0.0F;
    @Unique
    private static int freshCou = 0;

    // 尊重原有代码结构，保留构造函数（注：在Mixin中此类构造函数通常仅用于Shadow模拟）
    public ShapelessRecipeMixin(ItemStack result) {
        this.result = result;
    }

    // TODO test whether works
    @Inject(at = @At("HEAD"), method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z")
    private void matches(@NotNull CraftingContainer inventory, Level world, CallbackInfoReturnable<Boolean> cir) {
        theWorld = world;
        freshSum = 0.0F;
        freshCou = 0;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (RotHelper.canRot(stack.getItem())) {
                freshSum += RotHelper.getFresh(world, stack);
                ++freshCou;
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "getResultItem(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", cancellable = true)
    private void getOutPut(RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stackOut = this.result.copy();
        if (RotHelper.canRot(stackOut.getItem()) && theWorld != null && freshCou > 0) {
            RotHelper.setFresh(theWorld, stackOut, Math.max(freshSum / freshCou, 0.1F));
            cir.setReturnValue(stackOut);
        }
    }

    @Inject(at = @At("HEAD"), method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", cancellable = true)
    private void craft(CraftingContainer inventory, RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> cir) {
        getOutPut(registryAccess, cir);
    }

}