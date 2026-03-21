package biz.coolpage.hcs.mixin.entity.dragon;

import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonChargePlayerPhase.class)
public class ChargingPlayerPhaseMixin {
    @Shadow
    private int timeSinceCharge;

    @Inject(method = "begin", at = @At("TAIL"))
    public void begin(CallbackInfo ci) {
        // Set timeSinceCharge to a negative value to extend charge preparation/duration
        this.timeSinceCharge = -300;
    }
}