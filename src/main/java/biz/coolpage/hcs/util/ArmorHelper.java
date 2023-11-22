package biz.coolpage.hcs.util;

import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StatusManager;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class ArmorHelper {
    public static float getDamageLeft(float damage, float protection, float toughness) {
        /* Edit damage left mechanism ( DamageUtil/getDamageLeft() ):
            diff: 1. damage <= 6.0F ? 0.0F : damage -- damage
                  2. Math.pow(0.5, g / 6)          -- 1.0f - g / 25.0f
          NOTE: DamageUtil/getInflictedDamage() will ONLY be called when in armor(s) with protection enchantment

          Explanation of armor - damage / f:
            See https://www.mcmod.cn/item/577939.html
            1. damage / f: armor protection decrement -- damage↑ → protection↓
            2. f: armor toughness↑ → armor protection decrement↓
         */
        float f = 2.0f + toughness / 4.0f; //÷4: Every slot may have armor toughness → need avg
        float g = MathHelper.clamp(protection - (damage <= 6.0F ? 0.0F : damage) / f, protection * 0.2f, 20.0f);
        return damage * (float) Math.pow(0.5, g / 6);
    }

    public static float getDamageLeft(@Nullable PlayerEntity player, float damage) {
        return applyNullable(player,
                plr -> getDamageLeft(damage, getFinalProtection(plr), (float) plr.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)), //Preprocess for adjusting protection amount according to durability of armors
                0.0F); //If player == null, return 0
    }

    public static float getDamageLeftWithReducedArmor(@Nullable PlayerEntity player, float damage) {
        return applyNullable(player,
                plr -> {
                    float reducedArmor = getFinalProtection(plr);
                    if (reducedArmor > 4) reducedArmor = Math.max(4.0F, reducedArmor * 0.75F);
                    return getDamageLeft(damage, reducedArmor, (float) plr.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
                }, //Preprocess for adjusting protection amount according to durability of armors
                0.0F); //If player == null, return 0
    }

    public static float getAdjustedProtectionDelta(@Nullable PlayerEntity player) {
        AtomicReference<Float> delta = new AtomicReference<>(0.0F);
        if (player != null) {
            var armors = player.getArmorItems();
            if (armors != null) {
                armors.forEach(stack -> {
                    if (stack != null) {
                        var item = stack.getItem();
                        if (item instanceof ArmorItem armor) {
                            int maxDamage = armor.getMaxDamage(), durability = maxDamage - stack.getDamage();
                            float durPercent = durability / (float) maxDamage;
                            if (durPercent < 0.5F)
                                delta.set(delta.get() - armor.getProtection() * MathHelper.clamp(1 - durPercent * 2, 0.0F, 1.0F));
//                            if (player.hasStatusEffect(StatusEffects.RESISTANCE))
//                                delta.set(delta.get() + EntityHelper.getEffectAmplifier(player, StatusEffects.RESISTANCE) * 5);
                        }
                    }
                });
            }
        }
        return delta.get();
    }

    public static float getFinalProtection(@Nullable PlayerEntity player) {
        return applyNullable(player, plr -> {
            StatusManager statusManager = ((StatAccessor) plr).getStatusManager();
            //S2C Packet, see StatusManager
            if (plr instanceof ServerPlayerEntity sPlr) { //Server Side
                float protection = sPlr.getArmor() + getAdjustedProtectionDelta(sPlr);
                statusManager.setRealProtection(protection);
                return protection;
            }
            return statusManager.getRealProtection(); //Client Side
        }, 0.0F); //If player == null, return 0
    }
}
