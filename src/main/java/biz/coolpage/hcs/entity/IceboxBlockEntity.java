package biz.coolpage.hcs.entity;

import biz.coolpage.hcs.Hcs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import org.jetbrains.annotations.Nullable;


public class IceboxBlockEntity extends RandomizableContainerBlockEntity implements EntityBlock {
    public IceboxBlockEntity(BlockPos pos, BlockState state) {
        super(Hcs.ICEBOX_BLOCK_ENTITY.get(), pos, state);
    }

    public static final int INV_SIZE = 18;
    private NonNullList<ItemStack> inventory = NonNullList.withSize(INV_SIZE, ItemStack.EMPTY);


    @Override
    protected NonNullList<ItemStack> getItems() {
        this.setChanged();
        return this.inventory;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> list) {
        this.inventory = list;
    }


    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.hcsurvival.icebox");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new ChestMenu(MenuType.GENERIC_9x2, containerId, playerInventory, this, 2);
    }

    @Override
    public int getContainerSize() {
        return INV_SIZE;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public int getMaxStackSize() {
        return super.getMaxStackSize();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IceboxBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel world, T blockEntity) {
        return EntityBlock.super.getListener(world, blockEntity);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        ContainerHelper.loadAllItems(nbt, inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        ContainerHelper.saveAllItems(nbt, inventory);
    }
}