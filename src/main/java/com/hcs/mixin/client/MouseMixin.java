package com.hcs.mixin.client;

import com.hcs.status.accessor.StatAccessor;
import com.hcs.status.manager.StatusManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value = EnvType.CLIENT)
@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (this.client.player == null) return;
        StatusManager statusManager = ((StatAccessor) this.client.player).getStatusManager();
        if (button == 1 && this.client.mouse.wasLeftButtonClicked() && this.client.currentScreen == null/*In game*/ && !(this.client.player.isCreative() || this.client.player.isSpectator())) {
            statusManager.setLockDestroying(true);
        } else if ((button == 0 || button == 1) && action == 1 && statusManager.lockDestroying()) {
            statusManager.setLockDestroying(false);
            if (this.client.interactionManager != null) this.client.interactionManager.cancelBlockBreaking();
        }
    }
}
