package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
public abstract class AxeItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"))
    public void useOnBlock(@NotNull ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        EntityHelper.dropBark(context);
    }

}
