package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Reg;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombustionHelper {
    // A shared method for torches and campfires, which is used to handle initial logics.
    @SuppressWarnings("unused")
    public static boolean onLit(@NotNull World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand) {
        if (!player.getAbilities().allowModifyWorld) return false;
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();
        final boolean isFireCharge = item == Items.FIRE_CHARGE, isFlintAndSteel = item == Items.FLINT_AND_STEEL;
        final boolean isTorch = isTorchWithFlame(item);
        if (isFlintAndSteel || isFireCharge || isTorch) {
            if (!player.isCreative()) {
                if (isFlintAndSteel) stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                else if (isFireCharge) stack.decrement(1);
            }
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            world.playSound(null, pos, isFlintAndSteel ? SoundEvents.ITEM_FLINTANDSTEEL_USE : SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS);
            return true;
        }
        return false;
    }

    // ***** Torches *****

    public static @Nullable ActionResult preLitHoldingTorch(@NotNull ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) return null;
        ItemStack stack = context.getStack();
        BlockPos pos = context.getBlockPos();
        BlockState state = context.getWorld().getBlockState(pos);
        Block block = state.getBlock();
        World world = context.getWorld();
        if (state.isIn(BlockTags.FIRE) || state.isIn(BlockTags.CAMPFIRES)
                || isTorchWithFlame(block.asItem())
                || (block instanceof AbstractFurnaceBlock && state.get(Properties.LIT) && player.isSneaking())) {
            litHoldingTorch(player, world, stack);
            return ActionResult.success(world.isClient);
        }
        return null;
    }

    public static void litHoldingTorch(PlayerEntity player, @NotNull World world, @NotNull ItemStack stack) {
        EntityHelper.dropItem(player, stack.isOf(Reg.CRUDE_TORCH_ITEM) ? Reg.BURNING_CRUDE_TORCH_ITEM : Items.TORCH);
        stack.decrement(1); // This operation must behind the drop item process considering condition when players holding single torch
        world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS);
    }

    public static boolean isTorchWithFlame(Item item) {
        return item == Reg.BURNING_CRUDE_TORCH_ITEM || (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof TorchBlock && item != Items.REDSTONE_TORCH && item != Reg.CRUDE_TORCH_ITEM && item != Reg.UNLIT_TORCH_ITEM && item != Reg.GLOWSTONE_TORCH_ITEM);
    }


    // ***** Campfires *****
    public static final IntProperty CUSTOM_BRIGHTNESS = IntProperty.of("hcs_custom_brightness", 1, 15);
    public static final int CAMPFIRE_MAX_BURNING_LENGTH = 4800;

    public static BlockState setCustomBrightnessByRemainingBurningLength(@NotNull BlockState state, int remain) {
        return state.with(CUSTOM_BRIGHTNESS, MathHelper.clamp(remain / CAMPFIRE_MAX_BURNING_LENGTH, 0, 15));
    }

}
