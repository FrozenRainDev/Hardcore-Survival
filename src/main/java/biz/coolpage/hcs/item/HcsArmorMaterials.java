package biz.coolpage.hcs.item;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;

import java.util.EnumMap;

public enum HcsArmorMaterials implements StringIdentifiable, ArmorMaterial {
    COPPER("hcs_copper", 12, Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.HELMET, 2);
        map.put(ArmorItem.Type.CHESTPLATE, 3);
        map.put(ArmorItem.Type.LEGGINGS, 1);
        map.put(ArmorItem.Type.BOOTS, 1);
    }), 9, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0.0F, 0.0F, Ingredient.ofItems(Items.COPPER_INGOT)),
    WOOL("hcs_wool", 5, Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.HELMET, 1);
        map.put(ArmorItem.Type.CHESTPLATE, 1);
        map.put(ArmorItem.Type.LEGGINGS, 0);
        map.put(ArmorItem.Type.BOOTS, 0);
    }), 15, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, Ingredient.fromTag(ItemTags.WOOL)),
    WOOD("hcs_wood", 2, Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.HELMET, 1);
        map.put(ArmorItem.Type.CHESTPLATE, 1);
        map.put(ArmorItem.Type.LEGGINGS, 1);
        map.put(ArmorItem.Type.BOOTS, 1);
    }), 9, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F, 0.0F, Ingredient.fromTag(ItemTags.PLANKS));

    private final String name;
    private final int durabilityMultiplier;
    private final EnumMap<ArmorItem.Type, Integer> protectionAmounts;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Ingredient repairIngredient;

    HcsArmorMaterials(String name, int durabilityMultiplier, EnumMap<ArmorItem.Type, Integer> protectionAmounts, int enchantability, SoundEvent equipSound, float toughness, float knockbackResistance, Ingredient ingredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionAmounts = protectionAmounts;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = ingredient;
    }

    @Override
    public int getDurability(ArmorItem.Type type) {
        return ArmorMaterials.BASE_DURABILITY.get(type) * this.durabilityMultiplier;
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return this.protectionAmounts.get(type);
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return this.equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }

    @Override
    public String asString() {
        return this.name;
    }

}
