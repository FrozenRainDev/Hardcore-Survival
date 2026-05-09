package biz.coolpage.hcs.mixin.entity.dragon;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystal.class)
public class EndCrystalEntityMixin {
    // Need netherite pickaxe to break end crystal
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void hurt(@NotNull DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getEntity() instanceof ServerPlayer player) {
            if (!player.getMainHandItem().is(Items.NETHERITE_PICKAXE)) {
                EntityHelper.msgById(player, "tip.hcsurvival.need_netherite_pickaxe");
                cir.setReturnValue(false);
            } else EntityHelper.lightningStrike(player);
        } else cir.setReturnValue(false);
    }
}
