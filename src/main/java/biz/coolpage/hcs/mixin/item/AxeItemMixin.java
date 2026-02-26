package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
public abstract class AxeItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"))
    public void hcs$useOn(@NotNull UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        EntityHelper.dropBark(context);
    }

}