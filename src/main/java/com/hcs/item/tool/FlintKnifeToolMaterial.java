package com.hcs.item.tool;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class FlintKnifeToolMaterial implements ToolMaterial {
    public static final FlintKnifeToolMaterial INSTANCE = new FlintKnifeToolMaterial();

    @Override
    public int getDurability() {
        return 24;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 0;
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
