package biz.coolpage.hcs.status.accessor;

public interface ICampfireBlockEntity {
    long getBurnOutTime();

    void resetBurnOutTime();

    void setBurnOutTime(long val);
}
