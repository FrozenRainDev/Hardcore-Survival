package biz.coolpage.hcs.mixin.entity.dragon;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AreaEffectCloudEntity.class)
public abstract class AreaEffectCloudEntityMixin {
    @Shadow
    @Final
    private Map<Entity, Integer> affectedEntities;

    @Shadow
    public abstract @Nullable Entity getOwner();

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Set;removeIf(Ljava/util/function/Predicate;)Z", shift = At.Shift.AFTER))
    public void tick(CallbackInfo ci) {
        this.affectedEntities.forEach((entity, integer) -> {
            if (entity instanceof ServerPlayerEntity && this.getOwner() instanceof EnderDragonEntity dragon)
                dragon.heal(1.0F);
        });
    }
}
