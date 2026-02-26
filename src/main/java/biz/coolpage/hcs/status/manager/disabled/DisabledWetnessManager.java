package biz.coolpage.hcs.status.manager.disabled;

import biz.coolpage.hcs.status.manager.WetnessManager;

public class DisabledWetnessManager extends WetnessManager {
    @Override
    public double get() {
        return 0D;
    }

    @Override
    public void set(double val) {
    }

    @Override
    public void add(double val) {
    }
}
