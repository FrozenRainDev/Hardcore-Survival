package biz.coolpage.hcs.status.manager.disabled;

import biz.coolpage.hcs.status.manager.NutritionManager;

public class DisabledNutritionManager extends NutritionManager {
    @Override
    public double getVegetable() {
        return 1D;
    }

    @Override
    public void setVegetable(double val) {
    }

    @Override
    public void addVegetable(double val) {
    }
}