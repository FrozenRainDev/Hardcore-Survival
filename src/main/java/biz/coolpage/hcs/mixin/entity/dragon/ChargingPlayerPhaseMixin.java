package biz.coolpage.hcs.mixin.entity.dragon;

import net.minecraft.entity.boss.dragon.phase.ChargingPlayerPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChargingPlayerPhase.class)
public class ChargingPlayerPhaseMixin {
    @Shadow
    private int chargingTicks;

    @Inject(method = "beginPhase", at = @At("TAIL"))
    public void beginPhase(CallbackInfo ci) {
        this.chargingTicks = -300; //Enable prolonged charging time length
    }
}
