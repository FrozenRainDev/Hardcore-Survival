package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract Item getItem();

    @Inject(at = @At("HEAD"), method = "canCombine", cancellable = true)
    private static void canCombine(@NotNull ItemStack stack, ItemStack otherStack, CallbackInfoReturnable<Boolean> cir) {
        NbtCompound nbt1 = stack.getOrCreateNbt().copy();//DO NOT FORGET!
        if (nbt1.contains(RotHelper.HFE)) nbt1.remove(RotHelper.HFE);
        if (nbt1.contains(RotHelper.HFI)) nbt1.remove(RotHelper.HFI);
        NbtCompound nbt2 = otherStack.getOrCreateNbt().copy();
        if (nbt2.contains(RotHelper.HFE)) nbt2.remove(RotHelper.HFE);
        if (nbt2.contains(RotHelper.HFI)) nbt2.remove(RotHelper.HFI);
        if (RotHelper.canRot(stack.getItem())) {
            if (stack.getItem() == otherStack.getItem() && (nbt1.toString()).equals(nbt2.toString())) {
                RotHelper.combineNBT(stack, otherStack);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "isOf", cancellable = true)
    public void isOf(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem() == Reg.IMPROVISED_SHIELD && item == Items.SHIELD) cir.setReturnValue(true);
    }

}
