package biz.coolpage.hcs.entity;

import biz.coolpage.hcs.Reg;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;


public class IceboxBlockEntity extends LootableContainerBlockEntity implements BlockEntityProvider {
    public IceboxBlockEntity(BlockPos pos, BlockState state) {
        super(Reg.ICEBOX_BLOCK_ENTITY, pos, state);
    }

    public static final int INV_SIZE = 18;
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INV_SIZE, ItemStack.EMPTY);


    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        this.markDirty();
        return this.inventory;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }


    @Override
    protected Text getContainerName() {
        return Text.translatable("block.hcs.icebox");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X2, syncId, playerInventory, this, 2);
    }

    @Override
    public int size() {
        return INV_SIZE;
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public int getMaxCountPerStack() {
        return super.getMaxCountPerStack();
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new IceboxBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> GameEventListener getGameEventListener(ServerWorld world, T blockEntity) {
        return BlockEntityProvider.super.getGameEventListener(world, blockEntity);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
    }

}
