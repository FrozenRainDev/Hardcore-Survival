package biz.coolpage.hcs.status.manager;

public class OxygenManager {
    private int oxygenLackLevel = 0;
    private int oxygenGenLevelAccumulation = 0;
    private int oxygenGenLevel = 0;

    public void reset(){
        oxygenLackLevel = 0;
        oxygenGenLevelAccumulation = 0;
        oxygenGenLevel = 0;
    }

    public int getOxygenLackLevel() {
        return oxygenLackLevel;
    }

    public void setOxygenLackLevel(int val) {
        oxygenLackLevel = val;
    }

    public void addOxygenGen() {
        oxygenGenLevelAccumulation += 1;
    }

    public int getOxygenGenLevel() {
        return oxygenGenLevel;
    }

    public void setOxygenGenLevel(int val) {
        oxygenGenLevel = val;
    }

    public void updateOxygenGen() {
        oxygenGenLevel = Math.min(2, oxygenGenLevelAccumulation);
        oxygenGenLevelAccumulation = 0;
    }

    public int getFinalOxygenLackLevel() {
        return oxygenLackLevel - oxygenGenLevel;
    }
}