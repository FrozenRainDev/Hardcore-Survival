package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StaminaManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoeItem.class)
public class HoeItemMixin {
    @Inject(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ActionResult;success(Z)Lnet/minecraft/util/ActionResult;"))
    public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (context == null) return;
        PlayerEntity player = context.getPlayer();
        if (player == null) return;
        if (!player.world.isClient && !player.getAbilities().invulnerable) {
            StaminaManager staminaManager = ((StatAccessor) player).getStaminaManager();
            staminaManager.add(-0.005, player);
            staminaManager.pauseRestoring();
        }
    }
}
