package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.block.SmolderingCampfireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CampfireBlock;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.ToIntFunction;

import static biz.coolpage.hcs.util.CombustionHelper.COMBUST_LUMINANCE;

@Mixin(Properties.class)
public abstract class BlockBehaviourPropertiesMixin {
    // AbstractBlockSettingsMixin
    @Shadow
    ToIntFunction<BlockState> lightEmission;

    // There is no better solution up to now from what I thought
    @Inject(method = "instabreak", at = @At("RETURN"), cancellable = true)
    private void breakInstantly(@NotNull CallbackInfoReturnable<Properties> cir) {
//        if(Configs.isEnabled(Configs.DIG_CONSTRAIN)) // Call here => null pointer exception :(
        // to break instantly, set PlayerEntity::getBlockBreakingSpeed return 9999...
        Properties sets = cir.getReturnValue();
        sets.strength(0.060114F);
        cir.setReturnValue(sets);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructorReturn(CallbackInfo ci) {
        this.lightEmission = state -> {
            // Annoying for optimization, but there is no better way to adjust campfire luminance dynamically, I guess
            // state.is(Blocks.CAMPFIRE) is invalid here
            if (!(state.getBlock() instanceof SmolderingCampfireBlock) && // use state.is => client crash :(
                    state.hasProperty(COMBUST_LUMINANCE) &&
                    state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT))
                return state.getValue(COMBUST_LUMINANCE);
            return lightEmission.applyAsInt(state);
        };
    }
}