package biz.coolpage.hcs.entity;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.util.CombustionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static biz.coolpage.hcs.config.Configs.BURN;
import static biz.coolpage.hcs.item.BurningCrudeTorchItem.EXTINGUISH_NBT;
import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class BurningCrudeTorchBlockEntity extends BlockEntity implements EntityBlock {
    private long extinguishTime;

    public BurningCrudeTorchBlockEntity(BlockPos pos, BlockState state) {
        super(Hcs.BURNING_CRUDE_TORCH_BLOCK_ENTITY, pos, state);
    }

    public long getExtinguishTime() {
        return this.extinguishTime;
    }

    public void setExtinguishTime(long time) {
        this.extinguishTime = time;
    }

    public boolean shouldExtinguish() {
        if (this.getLevel() == null) return false;
        if (!Configs.isEnabled(BURN)) return false;
        return this.extinguishTime < this.getLevel().getGameTime();
    }

    public void extinguish() {
        this.extinguishTime = 0L;
    }

    public void ignite() {
        this.extinguishTime = applyNullable(this.getLevel(), Level::getGameTime, 0L) + CombustionHelper.MAX_TORCH_BURNING_LENGTH;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BurningCrudeTorchBlockEntity(pos, state);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains(EXTINGUISH_NBT, Tag.TAG_LONG)) this.extinguishTime = nbt.getLong(EXTINGUISH_NBT);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putLong(EXTINGUISH_NBT, this.extinguishTime);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong(EXTINGUISH_NBT, this.extinguishTime);
        return nbt;
    }

    public void onLit(Player player) {
        Level world1 = this.getLevel();
        if (player == null || world1 == null || player.isShiftKeyDown()) return;
        this.ignite();
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }
}