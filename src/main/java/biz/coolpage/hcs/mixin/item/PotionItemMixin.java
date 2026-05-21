package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_LIKE;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin extends Item {
    public PotionItemMixin(Properties settings) {
        super(settings);
    }

    @Inject(method = "finishUsingItem", at = @At(value = "HEAD"))
    public void finishUsingMixin(ItemStack stack, Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof ServerPlayer player && IS_SURVIVAL_LIKE.test(player)) {
            Potion potion = PotionUtils.getPotion(stack);
            if (potion.getEffects().isEmpty()) {
                player.addEffect(new MobEffectInstance(HcsEffects.DIARRHEA.get(), 600, 0, false, false, true));
//                if (Math.random() < 0.003) ((StatAccessor) player).getDiseaseManager().addParasite(0.12);
            }
            ((StatAccessor) player).getThirstManager().addDirectly(0.3);
            TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
            if (temperatureManager.get() > 0.7) temperatureManager.add(-0.15);
        }
    }

    @Inject(method = "appendHoverText", at = @At(value = "HEAD"))
    public void appendHoverTextMixin(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context, CallbackInfo ci) {
        super.appendHoverText(stack, world, tooltip, context); // Show freshness info
    }
}