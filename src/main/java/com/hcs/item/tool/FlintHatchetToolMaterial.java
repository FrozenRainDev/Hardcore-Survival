package com.hcs.item.tool;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class FlintHatchetToolMaterial implements ToolMaterial {
    public static final FlintHatchetToolMaterial INSTANCE = new FlintHatchetToolMaterial();

    @Override
    public int getDurability() {
        return 4;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 0.24F;
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
