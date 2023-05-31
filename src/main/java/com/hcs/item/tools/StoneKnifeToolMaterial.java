package com.hcs.item.tools;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class StoneKnifeToolMaterial implements ToolMaterial {
    public static final StoneKnifeToolMaterial INSTANCE = new StoneKnifeToolMaterial();

    @Override
    public int getDurability() {
        return 16;
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
