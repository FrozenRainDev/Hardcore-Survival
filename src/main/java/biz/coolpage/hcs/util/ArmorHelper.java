package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.item.HcsArmorMaterials;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StatusManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;
import static biz.coolpage.hcs.util.CommUtil.optElse;

public class ArmorHelper {
    public static class CustomDecimalProtection {
        private static final HashMap<ArmorMaterial, HashMap<Enum<EquipmentSlot>, Float>> PROTECTIONS = new HashMap<>() {{
            this.put(HcsArmorMaterials.WOOL, ofProtection(0.6F, 0.8F, 0.4F, 0.2F));
            this.put(ArmorMaterials.LEATHER, ofProtection(0.3F, 0.4F, 0.2F, 0.1F));
            this.put(HcsArmorMaterials.WOOD, ofProtection(1.2F, 1.6F, 0.8F, 0.4F));
        }};

        @Contract("_, _, _, _ -> new")
        private static @NotNull HashMap<Enum<EquipmentSlot>, Float> ofProtection(float head, float chest, float legs, float feet) {
            return new HashMap<>() {{
                this.put(EquipmentSlot.HEAD, head);
                this.put(EquipmentSlot.CHEST, chest);
                this.put(EquipmentSlot.LEGS, legs);
                this.put(EquipmentSlot.FEET, feet);
            }};
        }

        public static boolean contains(ArmorMaterial material) {
            return PROTECTIONS.containsKey(material);
        }

        public static float get(ArmorMaterial material, Enum<EquipmentSlot> slot) {
            if (!contains(material)) {
                Reg.LOGGER.error(ArmorHelper.class + "/" + CustomDecimalProtection.class + "/get()F: !contains(" + material + ")");
                return 0.0F;
            }
            return optElse(PROTECTIONS.get(material).get(slot), 0.0F);
        }
    }

    public static void eachArmorDeltaProcess(ItemStack stack, AtomicReference<Float> delta) {
        if (stack != null && stack.getItem() instanceof ArmorItem armor) {
            int maxDamage = armor.getMaxDamage(), durability = maxDamage - stack.getDamage();
            float durPercent = durability / (float) maxDamage;
            if (durPercent < 0.5F)
                delta.set(delta.get() - armor.getProtection() * MathHelper.clamp(1 - durPercent * 2, 0.0F, 1.0F));
        }
    }

    public static float getDamageLeft(float damage, float protection, float toughness) {
        /* Edit damage left mechanism ( DamageUtil/getDamageLeft() ):
            diff: 1. damage <= 6.0F ? 0.0F : damage -- damage
                  2. Math.pow(0.5, g / 6)           -- 1.0f - g / 25.0f
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
            if (armors != null) armors.forEach(stack -> eachArmorDeltaProcess(stack, delta));
            int ironskin = EntityHelper.getEffectAmplifier(player, HcsEffects.IRONSKIN);
            if (ironskin > -1) delta.set(delta.get() + ironskin > 0 ? 1.6F : 1.2F);
        }
        return delta.get();
    }

    public static float getProtectionWithCustomDecimals(@Nullable PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            AtomicReference<Float> protection = new AtomicReference<>(0.0F);
            applyNullable(serverPlayer.getArmorItems(), stacks -> stacks.forEach(stack -> {
                if (stack.getItem() instanceof ArmorItem armor) {
                    var material = armor.getMaterial();
                    if (CustomDecimalProtection.contains(material))
                        protection.updateAndGet(val -> val + CustomDecimalProtection.get(material, armor.getSlotType()));
                    else protection.updateAndGet(val -> val + armor.getProtection());
                }
            }));
            return protection.get();
        }
        Reg.LOGGER.error(ArmorHelper.class + "/getProtectionWithCustomDecimals(): !(player instanceof ServerPlayerEntity)");
        return (float) applyNullable(player, LivingEntity::getArmor, 0);
    }

    public static float getFinalProtection(@Nullable PlayerEntity player) {
        return applyNullable(player, p -> {
            StatusManager statusManager = ((StatAccessor) p).getStatusManager();
            //S2C Packet, see StatusManager
            if (p instanceof ServerPlayerEntity sp) { //Server Side
                float protection = getProtectionWithCustomDecimals(sp) + getAdjustedProtectionDelta(sp);
                statusManager.setRealProtection(protection);
                return protection;
            }
            return statusManager.getRealProtection(); //Client Side
        }, 0.0F); //If player == null, return 0
    }
}
