package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneyBottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoneyBottleItem.class)
public class HoneyBottleItemMixin {
    // finishUsing -> finishUsingItem (Mojang)
    @Inject(at = @At("RETURN"), method = "finishUsingItem")
    public void finishUsing(ItemStack stack, @NotNull Level level, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (!level.isClientSide) {
            user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 0));
            if (user instanceof Player player) {
                ((StatAccessor) player).getThirstManager().add(0.1);
                ((StatAccessor) player).getSanityManager().add(0.1);
            }
        }
    }
}