package biz.coolpage.hcs.mixin.block;

import net.minecraft.block.AbstractBlock.Settings;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Settings.class)
public abstract class AbstractBlockSettingsMixin {
    //There is no better solution up to now
    @Inject(method = "breakInstantly", at = @At("RETURN"), cancellable = true)
    private void breakInstantly(@NotNull CallbackInfoReturnable<Settings> cir) {
        Settings sets = cir.getReturnValue();
        sets.strength(0.06F);
        cir.setReturnValue(sets);
    }
}
