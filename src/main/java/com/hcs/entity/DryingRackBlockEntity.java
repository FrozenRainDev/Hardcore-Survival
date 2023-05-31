package com.hcs.entity;

import com.hcs.main.Reg;
import com.hcs.main.helper.EntityHelper;
import com.hcs.main.helper.RotHelper;
import com.hcs.misc.recipes.CustomDryingRackRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;


public class DryingRackBlockEntity extends BlockEntity implements BlockEntityProvider {
    public static final String DRYING_DEADLINE = "hcs_drying_deadline";
    public static final long DRYING_LENGTH = 24000 * 2; // 2 days

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(Reg.DRYING_RACK_BLOCK_ENTITY, pos, state);
    }

    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public DefaultedList<ItemStack> getInventory() {
        return this.inventory;
    }

    public ItemStack getInventoryStack() {
        this.markDirty();
        return this.inventory.get(0);
    }

    public void setInventoryStack(ItemStack stack) {
        stack = stack.copy();
        if (stack.getCount() > 1) stack.setCount(1);
        this.inventory.set(0, stack);
        this.markDirty();
    }

    public boolean use(PlayerEntity player) {
        if (player == null || this.getWorld() == null) return false;
        if (player.isSneaking()) return false;
        ItemStack stack = player.getMainHandStack();
        if (getInventoryStack().isEmpty()) {
            if (CustomDryingRackRecipe.getOutput(stack.getItem()) == Items.AIR) return false;
            this.setInventoryStack(stack);
            if (!player.isCreative()) stack.decrement(1);
            RotHelper.update(this.getWorld(), new SimpleInventory(this.getInventoryStack()), true);
            this.setDryingDeadline(DRYING_LENGTH + this.getWorld().getTime());
        } else {
            EntityHelper.dropItem(this.getWorld(), this.getPos(), this.getInventoryStack());
            this.setInventoryStack(ItemStack.EMPTY);
        }
        this.markDirty();
        this.getWorld().updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_LISTENERS);
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, this.inventory);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        Inventories.writeNbt(nbt, this.inventory);
        return nbt;
    }

    public long getDryingDeadline() {
        NbtCompound nbt = this.getInventoryStack().getOrCreateNbt();
        if (nbt.contains(DRYING_DEADLINE)) return nbt.getLong(DRYING_DEADLINE);
        return -1;
    }

    public void setDryingDeadline(long remain) {
        NbtCompound nbt = this.getInventoryStack().getOrCreateNbt();
        nbt.putLong(DRYING_DEADLINE, remain);
    }

}
