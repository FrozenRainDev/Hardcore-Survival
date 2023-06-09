package com.hcs.mixin.client;

import com.hcs.status.accessor.StatAccessor;
import com.hcs.util.EntityHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    private GameMode gameMode;

    @Shadow
    @Final
    private MinecraftClient client;

    /*
    The code of getReachDistance, as well as "MinecraftClientMixin, method doAttack" is from https://github.com/Kelvin285/MITE-Reborn
    Copyright (C) 2020 Kelvin285
    license: https://mit-license.org/
    */

    @Inject(method = "getReachDistance", at = @At("RETURN"), cancellable = true)
    public void getReachDistance(CallbackInfoReturnable<Float> cir) {
        if (client.player == null || !this.gameMode.isSurvivalLike()) return;
        cir.setReturnValue(2.0F + EntityHelper.getReachRangeAddition(client.player.getMainHandStack()));
    }

    /*
    @Inject(method = "cancelBlockBreaking", at = @At("HEAD"), cancellable = true)
    public void cancelBlockBreaking(CallbackInfo ci) {
        if (this.client.player != null && ((StatAccessor) this.client.player).getStatusManager().lockDestroying())
            ci.cancel();
    }
     */
}
