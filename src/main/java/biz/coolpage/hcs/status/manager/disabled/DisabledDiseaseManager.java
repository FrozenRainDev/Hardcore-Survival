package biz.coolpage.hcs.status.manager.disabled;

import biz.coolpage.hcs.status.manager.DiseaseManager;

public class DisabledDiseaseManager extends DiseaseManager {
    @Override
    public double getParasite() {
        return 0D;
    }

    @Override
    public void setParasite(double val) {
    }

    @Override
    public void addParasite(double val) {
    }

    @Override
    public double getCold() {
        return 0D;
    }

    @Override
    public void setCold(double val) {
    }

    @Override
    public void addCold(double val) {
        setCold(getCold() + val);
    }

    @Override
    public void tick(boolean shouldReduceCold) {
    }
}
