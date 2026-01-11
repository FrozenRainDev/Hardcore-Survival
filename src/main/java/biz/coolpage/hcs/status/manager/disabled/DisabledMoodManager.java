package biz.coolpage.hcs.status.manager.disabled;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.status.manager.InjuryManager;
import biz.coolpage.hcs.status.manager.MoodManager;

import static biz.coolpage.hcs.util.EntityHelper.PLASMA_CONCENTRATION;

public class DisabledMoodManager extends MoodManager {

    @Override
    public double getRawPanic() {
        return 0D;
    }

    @Override
    public double getRealPanic() {
        return 0D;
    }

    @Override
    public double getPanicAlleCache() {
        return 0D;
    }

    @Override
    public int getPanicKillerApplied() {
        return 0;
    }

    @Override
    public void setPanic(double val) {
    }

    @Override
    public void setPanicAlleviation(double val) {
    }

    @Override
    public void setPanicKillerApplied(int val) {
    }

    @Override
    public void addPanic(double val) {
    }

    @Override
    public void applyPanicKiller() {
    }

    @Override
    public double getHappiness() {
        return 1D;
    }

    @Override
    public void setHappiness(double val) {
    }

    @Override
    public void addHappiness(double val) {
    }

    @Override
    public void tick(double currRawPanic, double expectedRawPanic, double panicDiff, double currSan) {
    }
}
