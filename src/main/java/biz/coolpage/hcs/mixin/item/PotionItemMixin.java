package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionItemMixin {
    @Inject(method = "finishUsing", at = @At(value = "HEAD"))
    public void finishUsingMixin(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof ServerPlayerEntity player) {
            Potion potion = PotionUtil.getPotion(stack);
            if (!world.isClient && potion.getEffects().isEmpty()) {
                ((StatAccessor) player).getThirstManager().addDirectly(0.3);
                player.addStatusEffect(new StatusEffectInstance(HcsEffects.DIARRHEA, 600, 0, false, false, true));
                if (Math.random() < 0.003) ((StatAccessor) player).getDiseaseManager().addParasite(0.12);
                TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
                if (temperatureManager.get() > 0.7) temperatureManager.add(-0.1);
            }
        }
    }
}
