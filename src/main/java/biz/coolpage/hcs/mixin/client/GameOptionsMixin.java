package biz.coolpage.hcs.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(value = EnvType.CLIENT)
@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
    @Shadow
    @Final
    private SimpleOption<Double> gamma;

    @Inject(method = "getGamma", at = @At("HEAD"))
    public void getGamma(@NotNull CallbackInfoReturnable<SimpleOption<Double>> cir) {
        this.gamma.setValue(0.0); // Also see VideoOptionsScreenMixin
    }
}
