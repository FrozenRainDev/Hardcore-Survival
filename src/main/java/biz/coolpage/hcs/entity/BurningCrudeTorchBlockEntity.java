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
import static biz.coolpage.hcs.item.BurningCrudeTorchItem.EXTINGUISH_NBT;

public class BurningCrudeTorchBlockEntity extends BlockEntity implements BlockEntityProvider {
    private long extinguishTime;

    public BurningCrudeTorchBlockEntity(BlockPos pos, BlockState state) {
        super(Reg.BURNING_CRUDE_TORCH_BLOCK_ENTITY, pos, state);
    }

    public long getExtinguishTime() {
        return this.extinguishTime;
    }

    public void setExtinguishTime(long time) {
        this.extinguishTime = time;
    }

    public boolean shouldExtinguish() {
        return this.world == null ||  this.extinguishTime < this.world.getTime();
    }

    public void extinguish() {
        this.extinguishTime = 0L;
    }

    public void ignite() {
        this.extinguishTime = applyNullable(this.getWorld(), World::getTime, 0L) + CombustionHelper.MAX_TORCH_BURNING_LENGTH;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BurningCrudeTorchBlockEntity(pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains(EXTINGUISH_NBT, NbtElement.LONG_TYPE)) this.extinguishTime = nbt.getLong(EXTINGUISH_NBT);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong(EXTINGUISH_NBT, this.extinguishTime);
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putLong(EXTINGUISH_NBT, this.extinguishTime);
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
