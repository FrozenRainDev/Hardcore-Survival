package com.hcs.item.tools;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;

import java.util.EnumMap;

public class WoolClothingMaterial implements ArmorMaterial {
    public static final EnumMap<ArmorItem.Type, Integer> PROTECTION = Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.HELMET, 1);
        map.put(ArmorItem.Type.CHESTPLATE, 1);
        map.put(ArmorItem.Type.LEGGINGS, 0);
        map.put(ArmorItem.Type.BOOTS, 0);
    });

    @Override
    public int getDurability(ArmorItem.Type type) {
        return ArmorMaterials.BASE_DURABILITY.get(type) * 5;
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return PROTECTION.get(type);
    }

    @Override
    public int getEnchantability() {
        return 15;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(Items.BLACK_WOOL, Items.BLUE_WOOL, Items.BROWN_WOOL, Items.CYAN_WOOL, Items.GRAY_WOOL, Items.GREEN_WOOL, Items.LIGHT_BLUE_WOOL, Items.LIGHT_GRAY_WOOL, Items.LIME_WOOL, Items.MAGENTA_WOOL, Items.ORANGE_WOOL, Items.PINK_WOOL, Items.PURPLE_WOOL, Items.RED_WOOL, Items.WHITE_WOOL, Items.YELLOW_WOOL);
    }

    @Override
    public String getName() {// Must be all lowercase
        return "hcs_wool";
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