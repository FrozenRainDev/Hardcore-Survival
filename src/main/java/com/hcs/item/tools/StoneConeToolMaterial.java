package com.hcs.item.tools;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class StoneConeToolMaterial implements ToolMaterial {
    public static final StoneConeToolMaterial INSTANCE = new StoneConeToolMaterial();

    @Override
    public int getDurability() {
        return 8;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 0.24f;
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
