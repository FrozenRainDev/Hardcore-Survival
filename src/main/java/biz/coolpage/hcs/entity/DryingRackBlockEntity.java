package biz.coolpage.hcs.entity;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.recipe.DryingRackRecipe;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;


public class DryingRackBlockEntity extends BlockEntity implements EntityBlock {
    public static final String DRYING_DEADLINE = "hcs_drying_deadline";
    public static final long DRYING_LENGTH = 24000 * 2; // 2 days

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(Hcs.DRYING_RACK_BLOCK_ENTITY, pos, state);
    }

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);

    public NonNullList<ItemStack> getInventory() {
        return this.inventory;
    }

    public ItemStack getInventoryStack() {
        this.setChanged();
        return this.inventory.get(0);
    }

    public void setInventoryStack(ItemStack stack) {
        stack = stack.copy();
        if (stack.getCount() > 1) stack.setCount(1);
        this.inventory.set(0, stack);
        this.setChanged();
    }

    public boolean onInteract(Player player) {
        if (player == null || this.getLevel() == null || player.isShiftKeyDown()) return false;
        ItemStack stack = player.getMainHandItem();
        if (getInventoryStack().isEmpty()) {
            if (DryingRackRecipe.getOutput(stack.getItem()) == Items.AIR) return false;
            this.setInventoryStack(stack);
            if (EntityHelper.IS_SURVIVAL_LIKE.test(player)) stack.shrink(1);
            RotHelper.update(this.getLevel(), new SimpleContainer(this.getInventoryStack()), true);
            this.setDryingDeadline(DRYING_LENGTH + this.getLevel().getGameTime());
        } else {
            EntityHelper.dropItem(this.getLevel(), this.getBlockPos(), this.getInventoryStack());
            this.setInventoryStack(ItemStack.EMPTY);
        }
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        ContainerHelper.loadAllItems(nbt, this.inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        ContainerHelper.saveAllItems(nbt, this.inventory);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        ContainerHelper.saveAllItems(nbt, this.inventory);
        return nbt;
    }

    public long getDryingDeadline() {
        CompoundTag nbt = this.getInventoryStack().getOrCreateTag();
        if (nbt.contains(DRYING_DEADLINE)) return nbt.getLong(DRYING_DEADLINE);
        return -1;
    }

    public void setDryingDeadline(long remain) {
        CompoundTag nbt = this.getInventoryStack().getOrCreateTag();
        nbt.putLong(DRYING_DEADLINE, remain);
    }

}