package biz.coolpage.hcs.item;

import biz.coolpage.hcs.Reg;
import net.fabricmc.yarn.constants.MiningLevels;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public enum HcsToolMaterials implements ToolMaterial {
    COPPER(MiningLevels.IRON, 48, 4.0F, 0.0F, 10, Ingredient.ofItems(Items.COPPER_INGOT)),
    FLINT_CONE(0, 12, 0.48F, 0.0F, 0, Ingredient.ofItems(Items.FLINT)),
    FLINT_HATCHET(0, 4, 0.24F, 0.0F, 0, Ingredient.ofItems(Items.FLINT)),
    FLINT_WEAPON(0, 24, 0.0F, 0.0F, 0, Ingredient.ofItems(Items.FLINT)),
    SHARP_BROKEN_BONE(0, 8, 0.36F, 0.0F, 0, Ingredient.ofItems(Items.BONE)),
    STONE_CONE(0, 8, 0.5F, 0.0F, 0, Ingredient.ofItems(Reg.ROCK)),
    STONE_WEAPON(0, 16, 0.0F, 0.0F, 0, Ingredient.ofItems(Reg.ROCK));

    private final int miningLevel;
    private final int durability;
    private final float miningSpeedMultiplier;
    private final float additionalAttackDamage;
    private final int enchantability;
    private final Ingredient repairIngredient;

    HcsToolMaterials(int miningLevel, int durability, float miningSpeedMultiplier, float attackDamage, int enchantability, Ingredient ingredient) {
        this.miningLevel = miningLevel;
        this.durability = durability;
        this.miningSpeedMultiplier = miningSpeedMultiplier;
        this.additionalAttackDamage = attackDamage;
        this.enchantability = enchantability;
        this.repairIngredient = ingredient;
    }


    @Override
    public int getDurability() {
        return this.durability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return this.miningSpeedMultiplier;
    }

    @Override
    public float getAttackDamage() {
        return this.additionalAttackDamage;
    }

    @Override
    public int getMiningLevel() {
        return this.miningLevel;
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient;
    }
}
