package biz.coolpage.hcs.mixin.client;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StatusManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_LIKE;

@OnlyIn(Dist.CLIENT)
@Mixin(MouseHandler.class)
public class MouseMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onPress", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (this.minecraft.player == null) return;
        StatusManager statusManager = ((StatAccessor) this.minecraft.player).getStatusManager();
        if (button == 1 && this.minecraft.mouseHandler.isLeftPressed() && this.minecraft.screen == null/*In game*/ && IS_SURVIVAL_LIKE.test(this.minecraft.player)) {
            statusManager.setLockDestroying(true);
        } else if ((button == 0 || button == 1) && action == 1 && statusManager.lockDestroying()) {
            statusManager.setLockDestroying(false);
            if (this.minecraft.gameMode != null) this.minecraft.gameMode.stopDestroyBlock();
        }
    }
}