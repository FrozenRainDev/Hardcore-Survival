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
    @Shadow
    ToIntFunction<BlockState> lightEmission;

    @Inject(method = "instabreak", at = @At("RETURN"), cancellable = true)
    private void breakInstantly(@NotNull CallbackInfoReturnable<Properties> cir) {
        // Properties 的方法通常返回 this，支持链式调用
        Properties sets = cir.getReturnValue();
        sets.strength(0.060114F);
        cir.setReturnValue(sets);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructorReturn(CallbackInfo ci) {
        // 重要：保存旧的引用以避免 Lambda 递归调用自身
        final ToIntFunction<BlockState> originalLightEmission = this.lightEmission;

        this.lightEmission = state -> {
            // 检查顺序优化：先检查是否包含属性，再获取值
            // 使用 instanceof 检查 Block 是安全的
            try {
                if (state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
                    if (state.hasProperty(COMBUST_LUMINANCE)) {
                        if (!(state.getBlock() instanceof SmolderingCampfireBlock)) {
                            return state.getValue(COMBUST_LUMINANCE);
                        }
                    }
                }
            } catch (Exception e) {
                // 在极少数初始化阶段，state 可能处于不完整状态，捕获异常以保安全
                return originalLightEmission.applyAsInt(state);
            }

            return originalLightEmission.applyAsInt(state);
        };
    }
}