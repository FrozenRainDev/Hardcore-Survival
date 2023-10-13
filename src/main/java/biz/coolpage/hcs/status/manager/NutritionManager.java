package biz.coolpage.hcs.status.manager;

import biz.coolpage.hcs.Reg;

public class NutritionManager {
    private double vegetable = 1.0;
    public static final String NUTRITION_VEGETABLE_NBT = "hcs_nutrition_vegetable";

    public double getVegetable() {
        if (vegetable > 1.0) vegetable = 1.0;
        else if (vegetable < 0.0) vegetable = 0.0;
        return vegetable;
    }

    public void setVegetable(double val) {
        if (Double.isNaN(val)) {
            Reg.LOGGER.error(this.getClass().getSimpleName() + ": Val is NaN");
            return;
        }
        if (val > 1.0) val = 1.0;
        else if (val < 0.0) val = 0.0;
        vegetable = val;
    }

    public void addVegetable(double val) {
        setVegetable(vegetable + val);
    }

    public void reset() {
        addVegetable(1.0);
    }

}