package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.item.HotWaterBottleItem;
import biz.coolpage.hcs.status.accessor.ICampfireBlockEntity;
import biz.coolpage.hcs.util.CombustionHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static biz.coolpage.hcs.util.CombustionHelper.EXTINGUISH_TIME_NBT;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin extends BlockEntity implements ICampfireBlockEntity {
    @Unique
    private long extinguishTime = Long.MAX_VALUE;

    public CampfireBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Unique
    @Override
    public long getBurnOutTime() {
        return this.extinguishTime;
    }

    @Unique
    @Override
    public void resetBurnOutTime() {
        if (this.world != null) {
            CampfireBlockEntity.markDirty(this.world, this.pos, this.world.getBlockState(pos));
            this.extinguishTime = this.world.getTime() + CombustionHelper.MAX_CAMPFIRE_BURNING_LENGTH;
        }
    }

    @Unique
    @Override
    public boolean setBurnOutTime(long val) {
        if (val < 0L || this.world == null) return false;
        long maxExtinguish = this.world.getTime() + CombustionHelper.MAX_CAMPFIRE_BURNING_LENGTH;
        if (this.extinguishTime != Long.MAX_VALUE && maxExtinguish - this.extinguishTime < 20)
            return false; // Cannot add fuel when just added fuel
        if (val > maxExtinguish) val = maxExtinguish;
        this.extinguishTime = val;
        return true;
    }

    @Inject(method = "litServerTick", at = @At("HEAD"))
    private static void litServerTickInjected1(@NotNull World world, BlockPos pos, @NotNull BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
//        if (state.isOf(Reg.BURNT_CAMPFIRE_BLOCK)) ci.cancel();
        if (state.isOf(Blocks.SOUL_CAMPFIRE)) {
            world.setBlockState(pos, state.with(CombustionHelper.COMBUST_LUMINANCE, 15));
        } else if (campfire instanceof ICampfireBlockEntity ic) {
            CombustionHelper.onServerTick(world, pos, state, ic);
        }
    }

    @Inject(method = "litServerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ItemScatterer;spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void litServerTickInjected2(World world, BlockPos pos, @NotNull BlockState state, CampfireBlockEntity campfire, CallbackInfo ci, boolean bl, int i, ItemStack itemStack, Inventory inventory, ItemStack itemStack2) {
        if (state.isOf(Blocks.SOUL_CAMPFIRE) && itemStack2.isOf(Reg.HOT_WATER_BOTTLE))
            HotWaterBottleItem.setStatus(itemStack2, -1); // Soul campfire will cool temp down for hot water bag
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    public void readNbt(@NotNull NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(EXTINGUISH_TIME_NBT, NbtElement.LONG_TYPE))
            this.extinguishTime = nbt.getLong(EXTINGUISH_TIME_NBT);
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    protected void writeNbt(@NotNull NbtCompound nbt, CallbackInfo ci) {
        nbt.putLong(EXTINGUISH_TIME_NBT, this.extinguishTime);
    }
}
