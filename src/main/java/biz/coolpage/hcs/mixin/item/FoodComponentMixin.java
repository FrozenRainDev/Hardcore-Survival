package biz.coolpage.hcs.mixin.item;

import net.minecraft.item.FoodComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FoodComponent.class)
public class FoodComponentMixin {
    @Inject(method = "isAlwaysEdible", at = @At("HEAD"), cancellable = true)
    public void isAlwaysEdible(@NotNull CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
