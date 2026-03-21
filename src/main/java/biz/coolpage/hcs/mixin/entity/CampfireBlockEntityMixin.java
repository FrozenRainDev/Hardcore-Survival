package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.item.HotWaterBottleItem;
import biz.coolpage.hcs.status.accessor.ICampfireBlockEntity;
import biz.coolpage.hcs.util.CombustionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static biz.coolpage.hcs.util.CombustionHelper.EXTINGUISH_TIME_NBT;

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
        if (Configs.isEnabled(Configs.BURN)) return this.extinguishTime;
        return this.extinguishTime = Long.MAX_VALUE;
    }

    @Unique
    @Override
    public void resetBurnOutTime() {
        if (this.getLevel() != null) {
            this.getLevel().blockEntityChanged(this.worldPosition); // Mojang 中也是 worldPosition
            this.extinguishTime = this.getLevel().getGameTime() + CombustionHelper.MAX_CAMPFIRE_BURNING_LENGTH;
        }
    }

    @Unique
    @Override
    public boolean setBurnOutTime(long val) {
        if (val < 0L || this.getLevel() == null) return false;
        long maxExtinguish = this.getLevel().getGameTime() + CombustionHelper.MAX_CAMPFIRE_BURNING_LENGTH;
        if (this.extinguishTime != Long.MAX_VALUE && maxExtinguish - this.extinguishTime < 20)
            return false;
        if (val > maxExtinguish) val = maxExtinguish;
        this.extinguishTime = val;
        return true;
    }

    // Yarn serverTick -> Mojang cookTick (1.20.1 CampfireBlockEntity 静态 tick 方法)
    @Inject(method = "cookTick", at = @At("HEAD"))
    private static void litServerTickInjected1(@NotNull Level world, BlockPos pos, @NotNull BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        if (state.is(Blocks.SOUL_CAMPFIRE)) {
            world.setBlockAndUpdate(pos, state.setValue(CombustionHelper.COMBUST_LUMINANCE, 15));
        } else if (campfire instanceof ICampfireBlockEntity ic) {
            CombustionHelper.onServerTick(world, pos, state, ic);
        }
    }

    @Inject(method = "cookTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void litServerTickInjected2(Level world, BlockPos pos, @NotNull BlockState state, CampfireBlockEntity campfire, CallbackInfo ci, boolean bl, int i, ItemStack itemStack, Container inventory, ItemStack itemStack2) {
        if (state.is(Blocks.SOUL_CAMPFIRE) && itemStack2.is(Hcs.HOT_WATER_BOTTLE))
            HotWaterBottleItem.setStatus(itemStack2, -1);
    }

    @Inject(method = "load", at = @At("HEAD"))
    public void load(@NotNull CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains(EXTINGUISH_TIME_NBT, Tag.TAG_LONG))
            this.extinguishTime = nbt.getLong(EXTINGUISH_TIME_NBT);
    }

    @Inject(method = "saveAdditional", at = @At("HEAD"))
    protected void saveAdditional(@NotNull CompoundTag nbt, CallbackInfo ci) {
        nbt.putLong(EXTINGUISH_TIME_NBT, this.extinguishTime);
    }
}