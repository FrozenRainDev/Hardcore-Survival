package biz.coolpage.hcs.status.accessor;

public interface IKickCoolDown {
    boolean canKick();
    void notifyKick();
    void updateCooldown();
}
