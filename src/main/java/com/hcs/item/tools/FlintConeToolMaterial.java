package com.hcs.item.tools;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class FlintConeToolMaterial implements ToolMaterial {
    public static final FlintConeToolMaterial INSTANCE = new FlintConeToolMaterial();

    @Override
    public int getDurability() {
        return 24;
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
