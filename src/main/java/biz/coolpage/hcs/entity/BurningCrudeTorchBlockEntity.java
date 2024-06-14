package biz.coolpage.hcs.entity;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.CombustionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class BurningCrudeTorchBlockEntity extends BlockEntity implements BlockEntityProvider {
    private long lastLitTime;
    private static final String LIT_NBT = "hcs_torch_last_lit";

    public BurningCrudeTorchBlockEntity(BlockPos pos, BlockState state) {
        super(Reg.BURNING_CRUDE_TORCH_BLOCK_ENTITY, pos, state);
    }

    public long getLastLitTime() {
        return this.lastLitTime;
    }

    public void setLastLitTime(long time) {
        this.lastLitTime = time;
    }

    public boolean shouldExtinguish() {
        return this.world == null || this.world.getTime() - this.lastLitTime > CombustionHelper.MAX_BURNING_LENGTH;
    }

    public void extinguish() {
        this.lastLitTime = 0L;
    }

    public void ignite() {
        this.lastLitTime = applyNullable(this.getWorld(), World::getTime, 0L) + CombustionHelper.MAX_BURNING_LENGTH;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BurningCrudeTorchBlockEntity(pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(LIT_NBT, NbtElement.LONG_TYPE)) this.lastLitTime = nbt.getLong(LIT_NBT);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong(LIT_NBT, this.lastLitTime);
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putLong(LIT_NBT, this.lastLitTime);
        return nbt;
    }

    public void onLit(PlayerEntity player) {
        World world1 = this.getWorld();
        if (player == null || world1 == null || player.isSneaking()) return;
        this.ignite();
        this.markDirty();
        this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_LISTENERS);
    }
}
