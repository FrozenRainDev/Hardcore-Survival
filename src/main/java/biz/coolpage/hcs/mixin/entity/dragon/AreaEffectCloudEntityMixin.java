package biz.coolpage.hcs.mixin.entity.dragon;

import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AreaEffectCloud.class)
public abstract class AreaEffectCloudEntityMixin {
    @Shadow
    @Final
    private Map<Entity, Integer> victims;

    @Shadow
    public abstract @Nullable Entity getOwner();

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Set;removeIf(Ljava/util/function/Predicate;)Z", shift = At.Shift.AFTER))
    public void tick(CallbackInfo ci) {
        this.victims.forEach((entity, integer) -> {
            if (entity instanceof ServerPlayer && this.getOwner() instanceof EnderDragon dragon)
                dragon.heal(1.0F);
        });
    }
}
