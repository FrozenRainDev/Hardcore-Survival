package biz.coolpage.hcs.mixin.entity.dragon;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystalEntity.class)
public class EndCrystalEntityMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void damage(@NotNull DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getAttacker() instanceof ServerPlayerEntity player) {
            if (!player.getMainHandStack().isOf(Items.NETHERITE_PICKAXE)) {
                EntityHelper.msgById(player, "hcs.tip.need_netherite_pickaxe");
                cir.setReturnValue(false);
            } else EntityHelper.lightningStrike(player);
        } else cir.setReturnValue(false);
    }
}
