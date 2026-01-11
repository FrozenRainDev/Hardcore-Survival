package biz.coolpage.hcs.status.manager.disabled;

import biz.coolpage.hcs.status.manager.ThirstManager;

public class DisabledThirstManager extends ThirstManager {
    @Override
    public double get() {
        return 1D;
    }

    @Override
    public void set(double val) {
    }

    @Override
    public void add(double val) {
    }

    @Override
    public void addDirectly(double val) {
    }

    @Override
    public float getSaturation() {
        return 0F;
    }

    @Override
    public void setSaturation(float val) {
    }

    @Override
    public float getThirstRateAffectedByTemp() {
        return 1.0F;
    }

    @Override
    public void setThirstRateAffectedByTemp(float val) {
    }

    @Override
    public void updateThirstRateAffectedByTemp(float envTemp, float bodyTemp) {
    }
}
