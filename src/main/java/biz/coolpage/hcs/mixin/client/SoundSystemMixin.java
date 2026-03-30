package biz.coolpage.hcs.mixin.client;

import biz.coolpage.hcs.status.HcsEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(SoundEngine.class)
public class SoundSystemMixin {
    @Inject(method = "calculateVolume(FLnet/minecraft/sounds/SoundSource;)F", at = @At("HEAD"), cancellable = true)
    private void getAdjustedVolume(float volumeMultiplier, SoundSource category, CallbackInfoReturnable<Float> cir) {
        LocalPlayer player = Minecraft.getInstance().player;
        // It won't work :(
        // 这里的 volumeMultiplier 对应原方法的第一个参数 pVolumeMultiplier
        if (category == SoundSource.AMBIENT && volumeMultiplier == 1145.0F && player != null && player.hasEffect(HcsEffects.DARKNESS_ENVELOPED.get())) {
            // 注意：原方法中会对结果进行 Mth.clamp(..., 0.0F, 1.0F)
            // 通过 cir.setReturnValue 直接返回 3.0F 会绕过原法的限制
            cir.setReturnValue(3.0F);
        }
    }
}