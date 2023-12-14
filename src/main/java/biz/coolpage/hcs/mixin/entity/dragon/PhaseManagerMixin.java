package biz.coolpage.hcs.mixin.entity.dragon;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PhaseManager.class)

public abstract class PhaseManagerMixin {
    @Shadow
    @Nullable
    private Phase current;
    @Final
    @Shadow
    private EnderDragonEntity dragon;

    @Inject(method = "getCurrent", at = @At("HEAD"))
    public void getCurrent(CallbackInfoReturnable<Phase> cir) {
        if (this.current != null) {
            var currType = this.current.getType();
            if (currType != null) {
                String currTypeName = this.current.getType().name;
                if (currTypeName != null) {
                    if (currTypeName.toLowerCase().contains("sitting") || currType.equals(PhaseType.LANDING)) {
                        //Disable sitting on the bedrock pillar
                        EntityHelper.letEnderDragonChargeAtTheClosestPlayer(this.dragon);
                    }
                }
            }
        }
    }
}
