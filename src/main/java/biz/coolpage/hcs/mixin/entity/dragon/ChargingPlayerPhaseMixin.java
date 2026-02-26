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
    private int chargingTime;

    @Inject(method = "begin", at = @At("TAIL"))
    public void begin(CallbackInfo ci) {
        this.chargingTime = -300; //Enable prolonged charging time length
    }
}
