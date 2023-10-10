package com.hcs.mixin.block;

import com.hcs.Reg;
import com.hcs.item.HotWaterBottleItem;
import com.hcs.util.EntityHelper;
import com.hcs.util.RotHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.hcs.util.EntityHelper.IS_SURVIVAL_LIKE;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {
    @Unique
    private static final BooleanProperty NATURALLY_GENERATED = BooleanProperty.of("hcs_naturally_gen");

    /*
     NOTES:
     1.Could Throw error if the name of property contains upper case letters
     2.The default value of BooleanProperty is true
    */

    @Inject(method = "onPlaced", at = @At("HEAD"))
    private void onPlaced(@NotNull World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ChestBlockEntity) world.setBlockState(pos, state.with(NATURALLY_GENERATED, false));
    }

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void onUse(BlockState state, @NotNull World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        ItemStack mainHandStack = player.getMainHandStack();
        Item mainHand = mainHandStack.getItem();
        if (blockEntity instanceof ChestBlockEntity && state.get(NATURALLY_GENERATED) != null && IS_SURVIVAL_LIKE.test(player)) {
            if (state.get(NATURALLY_GENERATED)) {
                cir.setReturnValue(ActionResult.SUCCESS);
                if (mainHand instanceof PickaxeItem) {
                    EntityHelper.msgById(player, "hcs.tip.unlocked");
                    world.setBlockState(pos, state.with(NATURALLY_GENERATED, false));
                } else EntityHelper.msgById(player, "hcs.tip.need_unlock");
            }
        }
    }

    @Inject(method = "appendProperties", at = @At("HEAD"))
    protected void appendProperties(StateManager.@NotNull Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(NATURALLY_GENERATED);
    }

    @Inject(method = "scheduledTick", at = @At("HEAD"))
    void scheduledTick(BlockState state, @NotNull ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!world.isClient()) {
            ChestBlock chestBlock = (ChestBlock) state.getBlock();
            Inventory inv = ChestBlock.getInventory(chestBlock, state, world, pos, true);
            if (!state.isOf(Reg.ICEBOX)) {
                RotHelper.update(world, inv);
                HotWaterBottleItem.update(world, inv);
            }
        }
    }

}