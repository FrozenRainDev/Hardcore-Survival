package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
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
    public PotionItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "finishUsing", at = @At(value = "HEAD"))
    public void finishUsingMixin(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof ServerPlayerEntity player && IS_SURVIVAL_LIKE.test(player)) {
            Potion potion = PotionUtil.getPotion(stack);
            if (potion.getEffects().isEmpty()) {
                double rand = Math.random();
                if (rand < 0.08)
                    player.addStatusEffect(new StatusEffectInstance(HcsEffects.FOOD_POISONING, 1200, 0, false, false, true));
                else player.addStatusEffect(new StatusEffectInstance(HcsEffects.DIARRHEA, 600, 0, false, false, true));
                if (Math.random() < 0.003) ((StatAccessor) player).getDiseaseManager().addParasite(0.12);
            }
            ((StatAccessor) player).getThirstManager().addDirectly(0.3);
            TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
            if (temperatureManager.get() > 0.7) temperatureManager.add(-0.15);
        }
    }

    @Inject(method = "appendTooltip", at = @At(value = "HEAD"))
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        super.appendTooltip(stack, world, tooltip, context); // Show freshness info
    }
}


