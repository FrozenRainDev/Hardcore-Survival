package biz.coolpage.hcs.status.manager.disabled;

import biz.coolpage.hcs.status.manager.TemperatureManager;

public class DisabledTemperatureManager extends TemperatureManager {
    @Override
    public double get() {
        return 0.5;
    }

    @Override
    public void set(double val) {
    }

    @Override
    public void add(double val) {
    }

    @Override
    public void addAmbient(float val) {
    }

    @Override
    public void updateAmbient() {
    }

    @Override
    public float getAmbientCache() {
        return 0F;
    }

    @Override
    public float getEnvTempCache() {
        return 0.5F;
    }

    @Override
    public void setEnvTempCache(float val) {
    }

    @Override
    public float getSaturation() {
        return 0F;
    }

    @Override
    public float getSaturationPercentage() {
        return 0.0F;
    }

    @Override
    public void addSaturation(double val) {
    }

    @Override
    public void addSaturation(float val) {
    }

    @Override
    public void setSaturation(float val) {
    }

    @Override
    public float getFeelTempCache() {
        return 0.5F;
    }

    @Override
    public void setFeelTempCache(float val) {
    }

    @Override
    public int getTrendType() {
        return 0;
    }

    @Override
    public void setTrendType(int val) {
    }

}
