package com.hcs.item.tool;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;

import java.util.EnumMap;

public class CopperArmorMaterial implements ArmorMaterial {
    public static final EnumMap<ArmorItem.Type, Integer> PROTECTION = Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.HELMET, 3);
        map.put(ArmorItem.Type.CHESTPLATE, 3);
        map.put(ArmorItem.Type.LEGGINGS, 1);
        map.put(ArmorItem.Type.BOOTS, 1);
    });

    @Override
    public int getDurability(ArmorItem.Type type) {
        return ArmorMaterials.BASE_DURABILITY.get(type) * 12;
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return PROTECTION.get(type);
    }

    @Override
    public int getEnchantability() {
        return 9;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(Items.COPPER_INGOT);
    }

    @Override
    public String getName() {// Must be all lowercase
        return "hcs_copper";
    }

    @Override
    public float getToughness() {
        return 0.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0F;
    }

}