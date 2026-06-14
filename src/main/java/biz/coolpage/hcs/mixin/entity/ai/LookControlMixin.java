package biz.coolpage.hcs.mixin.entity.ai;

import biz.coolpage.hcs.status.accessor.ILookControl;
import net.minecraft.world.entity.ai.control.LookControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LookControl.class)
public class LookControlMixin implements ILookControl {
    @Shadow protected double wantedX;
    @Shadow protected double wantedY;
    @Shadow protected double wantedZ;
    @Shadow protected float yMaxRotSpeed;
    @Shadow protected float xMaxRotAngle;
    @Shadow protected int lookAtCooldown;

    @Unique
    private boolean hcs$isLookLocked = false;

    @Override
    public void hcs$setLookLock(boolean locked) {
        this.hcs$isLookLocked = locked;
    }

    @Override
    public void hcs$forceLookAt(double x, double y, double z, float deltaYaw, float deltaPitch) {
        // Manually assign values, identical to Vanilla's base setLookAt behavior
        this.wantedX = x;
        this.wantedY = y;
        this.wantedZ = z;
        this.yMaxRotSpeed = deltaYaw;
        this.xMaxRotAngle = deltaPitch;
        this.lookAtCooldown = 2;
    }

    @Inject(method = "setLookAt(DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void hcs$onSetLookAt(double pX, double pY, double pZ, float pDeltaYaw, float pDeltaPitch, CallbackInfo ci) {
        // Cancel other look requests if the look is currently locked by BreakBlockGoal
        if (this.hcs$isLookLocked) {
            ci.cancel();
        }
    }
}