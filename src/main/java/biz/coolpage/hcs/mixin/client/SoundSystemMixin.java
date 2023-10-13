package biz.coolpage.hcs.mixin.client;

import biz.coolpage.hcs.status.HcsEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(value = EnvType.CLIENT)
@Mixin(SoundSystem.class)
public class SoundSystemMixin {
    @Inject(method = "getAdjustedVolume(FLnet/minecraft/sound/SoundCategory;)F", at = @At("HEAD"), cancellable = true)
    private void getAdjustedVolume(float volume, SoundCategory category, CallbackInfoReturnable<Float> cir) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        // It won't work :(
        if (category == SoundCategory.AMBIENT && volume == 1145.0F && player != null && player.hasStatusEffect(HcsEffects.DARKNESS_ENVELOPED))
            cir.setReturnValue(3.0F);
    }
}
