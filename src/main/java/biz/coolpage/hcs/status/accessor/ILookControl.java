package biz.coolpage.hcs.status.accessor;

public interface ILookControl {
    // Lock or unlock the LookControl
    void hcs$setLookLock(boolean locked);

    // Force set look target, bypassing the lock
    void hcs$forceLookAt(double x, double y, double z, float deltaYaw, float deltaPitch);
}