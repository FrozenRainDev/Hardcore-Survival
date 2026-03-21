package biz.coolpage.hcs.mixin.entity.dragon;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderDragonPhaseManager.class)
public abstract class PhaseManagerMixin {
    @Shadow
    @Nullable
    private DragonPhaseInstance currentPhase;

    @Final
    @Shadow
    private EnderDragon dragon;

    @Inject(method = "getCurrentPhase", at = @At("HEAD"))
    public void hcs$getCurrentPhase(CallbackInfoReturnable<DragonPhaseInstance> cir) {
        if (this.currentPhase != null) {
            var currType = this.currentPhase.getPhase();
            if (currType != null) {
                String currTypeName = currType.toString();
                if (currTypeName != null) {
                    if (currTypeName.toLowerCase().contains("sitting") || currType.equals(EnderDragonPhase.LANDING)) {
                        // Disable staying on bedrock pillars, force the dragon to charge towards the player
                        EntityHelper.letEnderDragonChargeAtTheClosestPlayer(this.dragon);
                    }
                }
            }
        }
    }
}