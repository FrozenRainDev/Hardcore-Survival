package com.hcs.item.tools;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class SharpBrokenBoneMaterial implements ToolMaterial {
    public static final SharpBrokenBoneMaterial INSTANCE = new SharpBrokenBoneMaterial();

    @Override
    public int getDurability() {
        return 8;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 0.36f;
    }

    @Override
    public float getAttackDamage() {
        return 0;
    }

    @Override
    public int getMiningLevel() {
        return 0;
    }

    @Override
    public int getEnchantability() {
        return 0;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return null;
    }
}
