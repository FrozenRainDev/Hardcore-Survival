package biz.coolpage.hcs.mixin.block;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.ToIntFunction;

import static biz.coolpage.hcs.util.CombustionHelper.COMBUST_LUMINANCE;

@Mixin(Settings.class)
public abstract class AbstractBlockSettingsMixin {
    @Shadow
    public ToIntFunction<BlockState> luminance;

    // There is no better solution up to now
    @Inject(method = "breakInstantly", at = @At("RETURN"), cancellable = true)
    private void breakInstantly(@NotNull CallbackInfoReturnable<Settings> cir) {
        Settings sets = cir.getReturnValue();
        sets.strength(0.06F);
        cir.setReturnValue(sets);
    }

    @Inject(method = "luminance", at = @At("RETURN"))
    private void luminance(ToIntFunction<BlockState> luminance, CallbackInfoReturnable<Settings> cir) {
        this.luminance = state -> {
            // Annoying for optimization, but there is no better way to adjust campfire luminance dynamically, I guess
            // state.isOf(Blocks.CAMPFIRE) is invalid here
            if (state.contains(COMBUST_LUMINANCE) && state.contains(CampfireBlock.LIT) && state.get(CampfireBlock.LIT))
                return state.get(COMBUST_LUMINANCE);
            return luminance.applyAsInt(state);
        };
    }
}
