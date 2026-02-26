package biz.coolpage.hcs.status.manager.disabled;

import biz.coolpage.hcs.status.manager.InjuryManager;

public class DisabledInjuryManager extends InjuryManager {
    @Override
    public void tick() {
    }

    @Override
    public double getPainkillerAlleviation() {
        return 0D;
    }

    @Override
    public double getRawPain() {
        return 0D;
    }

    @Override
    public double getRealPain() {
        return 0D;
    }

    @Override
    public void setRawPain(double val) {
    }

    @Override
    public void setAlleviationCache(double val) {
    }

    @Override
    public void addRawPain(double val) {
    }

    @Override
    public void applyPainkiller() {
    }

    @Override
    public int getPainkillerApplied() {
        return 0;
    }

    @Override
    public void setPainkillerApplied(int val) {
    }

    @Override
    public double getBleeding() {
        return 0D;
    }

    @Override
    public void setBleeding(double val) {
    }

    @Override
    public void addBleeding(double val) {
    }

    @Override
    public double getFracture() {
        return 0D;
    }

    @Override
    public void setFracture(double val) {
    }

    @Override
    public void addFracture(double val) {
    }
}
