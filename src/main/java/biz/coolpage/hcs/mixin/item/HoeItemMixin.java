package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StaminaManager;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoeItem.class)
public class HoeItemMixin {
    // useOnBlock -> useOn (Mojang)
    // 目标方法调用 InteractionResult.sidedSuccess (Mojang 映射中 success(Z) 通常指向此)
    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionResult;sidedSuccess(Z)Lnet/minecraft/world/InteractionResult;"))
    public void useOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context == null) return;
        Player player = context.getPlayer();
        if (player == null) return;

        // getWorld() -> level() (Mojang)
        if (!player.level().isClientSide && !player.getAbilities().invulnerable) {
            StaminaManager staminaManager = ((StatAccessor) player).getStaminaManager();
            staminaManager.add(-0.005, player);
            staminaManager.pauseRestoring();
        }
    }
}