package biz.coolpage.hcs.status.manager.disabled;

import biz.coolpage.hcs.status.manager.OxygenManager;

public class DisabledOxygenManager extends OxygenManager {
    @Override
    public int getOxygenLackLevel() {
        return 0;
    }

    @Override
    public void setOxygenLackLevel(int val) {
    }

    @Override
    public void addOxygenGen() {
    }

    @Override
    public int getOxygenGenLevel() {
        return 0;
    }

    @Override
    public void setOxygenGenLevel(int val) {
    }

    @Override
    public void updateOxygenGen() {
    }

    @Override
    public int getFinalOxygenLackLevel() {
        return 0;
    }
}