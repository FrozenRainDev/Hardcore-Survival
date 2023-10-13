package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoneyBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoneyBottleItem.class)
public class HoneyBottleItemMixin {
    @Inject(at = @At("RETURN"), method = "finishUsing")
    public void finishUsing(ItemStack stack, @NotNull World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (!world.isClient) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20 * 5, 0));
            if (user instanceof PlayerEntity player) {
                ((StatAccessor) player).getThirstManager().add(0.1);
                ((StatAccessor) player).getSanityManager().add(0.1);
            }
        }
    }
}
