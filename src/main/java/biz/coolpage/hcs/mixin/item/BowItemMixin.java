package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StaminaManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public class BowItemMixin {
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionResultHolder;consume(Ljava/lang/Object;)Lnet/minecraft/world/InteractionResultHolder;"))
    public void use(Level level, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (user != null) {
            // 在 Mojang 映射中，world 变为 level()，abilities.invulnerable 通常对应 instabuild (创造模式)
            if (!level.isClientSide && !user.getAbilities().instabuild) {
                StaminaManager staminaManager = ((StatAccessor) user).getStaminaManager();
                staminaManager.add(-0.02, user);
                staminaManager.pauseRestoring();
            }
        }
    }
}