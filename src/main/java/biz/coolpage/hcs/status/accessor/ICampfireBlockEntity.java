package biz.coolpage.hcs.status.accessor;

public interface ICampfireBlockEntity {
    long getBurnOutTime();

    void resetBurnOutTime();

    boolean setBurnOutTime(long val);
}
