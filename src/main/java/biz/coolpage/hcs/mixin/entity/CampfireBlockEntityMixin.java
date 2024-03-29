package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.item.HotWaterBottleItem;
import biz.coolpage.hcs.status.accessor.ICampfireBlockEntity;
import biz.coolpage.hcs.util.CombustionHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin extends BlockEntity implements ICampfireBlockEntity {
    @Unique
    private long extinguishTime;

    public CampfireBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Unique
    public long getBurnOutTime() {
        return this.extinguishTime;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Unique
    public void resetBurnOutTime() {
        if (this.world != null)
            this.extinguishTime = this.world.getTime() + CombustionHelper.CAMPFIRE_MAX_BURNING_LENGTH;
    }

    @Inject(method = "litServerTick", at = @At("HEAD"))
    private static void litServerTickInjected1(@NotNull World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        if (world.getRandom().nextFloat() < 0.001F) // Lit inflammable blocks nearby-
            Fluids.LAVA.onRandomTick(world, pos, Fluids.LAVA.getDefaultState(), world.getRandom());
        long time = world.getTime(), burnOutTime = ((ICampfireBlockEntity) campfire).getBurnOutTime();
        if (burnOutTime < time) {
            CampfireBlock.extinguish(null, world, pos, state);
            world.setBlockState(pos, state.with(CampfireBlock.LIT, false).with(CombustionHelper.COMBUST_STAGE, 0));
        } else if (time % 20 == 0)
            world.setBlockState(pos, CombustionHelper.updateCombustionState(state, (int) (burnOutTime - time)));
    }

    @Inject(method = "litServerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ItemScatterer;spawn(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void litServerTickInjected2(World world, BlockPos pos, @NotNull BlockState state, CampfireBlockEntity campfire, CallbackInfo ci, boolean bl, int i, ItemStack itemStack, Inventory inventory, ItemStack itemStack2) {
        if (state.isOf(Blocks.SOUL_CAMPFIRE) && itemStack2.isOf(Reg.HOT_WATER_BOTTLE))
            HotWaterBottleItem.setStatus(itemStack2, -1); // Soul campfire will cool temp down
    }
}
