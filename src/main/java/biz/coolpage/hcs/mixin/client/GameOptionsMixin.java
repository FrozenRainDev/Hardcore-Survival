package biz.coolpage.hcs.mixin.client;

import net.minecraft.client.Options;
import net.minecraft.client.OptionInstance;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(Options.class)
public abstract class GameOptionsMixin {
    @Shadow
    @Final
    private OptionInstance<Double> gamma;

    @Inject(method = "gamma", at = @At("HEAD"))
    public void getGamma(@NotNull CallbackInfoReturnable<OptionInstance<Double>> cir) {
        this.gamma.set(0.0); // Also see VideoOptionsScreenMixin
    }
}