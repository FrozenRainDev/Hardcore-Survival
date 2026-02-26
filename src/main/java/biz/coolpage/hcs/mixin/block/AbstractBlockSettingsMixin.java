package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.block.SmolderingCampfireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.ToIntFunction;

import static biz.coolpage.hcs.util.CombustionHelper.COMBUST_LUMINANCE;

@Mixin(BlockBehaviour.Properties.class)
public abstract class AbstractBlockSettingsMixin {
    @Shadow
    public ToIntFunction<BlockState> lightEmission;

    // There is no better solution up to now from what I thought
    @Inject(method = "instabreak", at = @At("RETURN"), cancellable = true)
    private void instabreak(@NotNull CallbackInfoReturnable<BlockBehaviour.Properties> cir) {
//        if(Configs.isEnabled(Configs.DIG_CONSTRAIN)) // Call here => null pointer exception :(
        // to break instantly, set Player::getDestroySpeed return 9999...
        BlockBehaviour.Properties props = cir.getReturnValue();
        props.strength(0.060114F);
        cir.setReturnValue(props);
    }

    @Inject(method = "lightLevel", at = @At("RETURN"))
    private void lightLevel(ToIntFunction<BlockState> lightEmission, CallbackInfoReturnable<BlockBehaviour.Properties> cir) {
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
