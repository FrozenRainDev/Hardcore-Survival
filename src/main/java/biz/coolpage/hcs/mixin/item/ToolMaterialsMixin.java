package biz.coolpage.hcs.mixin.item;

import net.minecraft.item.ToolMaterials;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ToolMaterials.class)
public class ToolMaterialsMixin {
    @Inject(method = "getDurability", at = @At("RETURN"), cancellable = true)
    public void getDurability(@NotNull CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValueI() == 250) cir.setReturnValue(128); //iron
        else if (cir.getReturnValueI() == 59) cir.setReturnValue(18); //wooden
    }
}
