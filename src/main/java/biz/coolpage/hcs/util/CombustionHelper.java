package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.item.BurningCrudeTorchItem;
import biz.coolpage.hcs.status.accessor.ICampfireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombustionHelper {
    // A shared method for torches and campfires, which is used to handle initial logics.
    @SuppressWarnings("unused")
    public static boolean onLit(@NotNull Level world, BlockPos pos, @NotNull Player player, InteractionHand hand) {
        if (!player.getAbilities().mayBuild) return false;
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        final boolean isFireCharge = item == Items.FIRE_CHARGE, isFlintAndSteel = item == Items.FLINT_AND_STEEL;
        final boolean isTorch = isTorchWithFlame(item);
        if (isFlintAndSteel || isFireCharge || isTorch) {
            if (!player.isCreative()) {
                if (isFlintAndSteel) stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                else if (isFireCharge) stack.shrink(1);
            }
            player.awardStat(Stats.ITEM_USED.get(item));
            world.playSound(null, pos, isFlintAndSteel ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS);
            return true;
        }
        return false;
    }

    // ***** Torches *****

    public static final long MAX_TORCH_BURNING_LENGTH = 24000L;

    public static @Nullable InteractionResult preLitHoldingTorch(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return null;
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(pos);
        Block block = state.getBlock();
        Level world = context.getLevel();
        if (state.is(BlockTags.FIRE) || state.is(BlockTags.CAMPFIRES) || isTorchWithFlame(block.asItem()) || (block instanceof AbstractFurnaceBlock && state.getValue(BlockStateProperties.LIT) && player.isShiftKeyDown())) {
            litHoldingTorch(player, world, stack);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return null;
    }

    public static void litHoldingTorch(Player player, @NotNull Level world, @NotNull ItemStack stack) {
        EntityHelper.dropItem(player, stack.is(Hcs.CRUDE_TORCH_ITEM.get()) ? Hcs.BURNING_CRUDE_TORCH_ITEM.get() : Items.TORCH);
        stack.shrink(1); // This operation must preform after the dropping item process considering condition when players holding single torch
        world.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS);
    }

    public static boolean isTorchWithFlame(Item item) {
        return item == Hcs.BURNING_CRUDE_TORCH_ITEM.get() || (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof TorchBlock && item != Items.REDSTONE_TORCH && item != Hcs.CRUDE_TORCH_ITEM.get() && item != Hcs.UNLIT_TORCH_ITEM.get() && item != Hcs.GLOWSTONE_TORCH_ITEM.get());
    }

    // ***** Campfires *****
    private static int getLuminance(long remainTicks) {
        if (remainTicks < 266) return 6;
        if (remainTicks < 416) return 7;
        if (remainTicks < 615) return 8;
        if (remainTicks < 874) return 9;
        if (remainTicks < 1200) return 10;
        if (remainTicks < 1604) return 11;
        if (remainTicks < 2094) return 12;
        if (remainTicks < 2681) return 13;
        if (remainTicks < 3374) return 14;
        return 15;
    }

    public static final IntegerProperty COMBUST_LUMINANCE = IntegerProperty.create("hcs_campfire_luminance", 0, 15);
    // Input: remain ticks; output: brightness; Hash table for less computing
    public static final int MAX_CAMPFIRE_BURNING_LENGTH = 4000;
    public static final String EXTINGUISH_TIME_NBT = "hcs_extinguish_nbt";

    public static @NotNull BlockState updateCombustionState(@NotNull BlockState state, long remain) {
        if (remain > MAX_CAMPFIRE_BURNING_LENGTH) remain = MAX_CAMPFIRE_BURNING_LENGTH;
        else if (remain < 0L) remain = 0L;
        return state.setValue(COMBUST_LUMINANCE, getLuminance(remain));
    }

    public static void onServerTick(@NotNull Level world, BlockPos pos, @NotNull BlockState state, ICampfireBlockEntity campfire) {
        // Campfire extinguish: Normal -> Smoldering -> Burnt
        boolean hasFlame = !state.is(Hcs.SMOLDERING_CAMPFIRE_BLOCK.get());
        if (hasFlame && world.random.nextFloat() < 0.001F) // Lit inflammable blocks nearby
            Fluids.LAVA.tick(world, pos, Fluids.LAVA.defaultFluidState());
        long time = world.getGameTime(), burnOutTime = campfire.getBurnOutTime();
        if (burnOutTime < time) {
            CampfireBlock.dowse(null, world, pos, state);
            // "setBlock" causes automatic cooking items drop (See CampfireBlock::onStateReplaced)
            // Modified: setBlockState -> setBlock (Mojang Mapping)
            world.setBlock(pos, (hasFlame ? Hcs.SMOLDERING_CAMPFIRE_BLOCK : Hcs.BURNT_CAMPFIRE_BLOCK).get().defaultBlockState().setValue(CampfireBlock.FACING, state.getValue(CampfireBlock.FACING)).setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED)), 3);
            world.levelEvent(null, 1009, pos, 0);
        } else {
            if (burnOutTime == Long.MAX_VALUE && Configs.isEnabled(Configs.BURN))
                campfire.resetBurnOutTime();
            else if (hasFlame) {
                world.setBlock(pos, CombustionHelper.updateCombustionState(state, burnOutTime - time), 3);
            }
        }
    }

    public static int getFuelDuration(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        return ForgeHooks.getBurnTime(stack, null);
    }

    public static boolean checkAddFuel(Level world, BlockPos pos, BlockState state, ItemStack stack) {
        int fuelDur = getFuelDuration(stack) * 2;
        if (fuelDur == 0) return false;
        if (CommUtil.hasNull(world, pos, state)) return false;
        if (world.getBlockEntity(pos) instanceof ICampfireBlockEntity campfire) {
            if (state.is(Hcs.BURNT_CAMPFIRE_BLOCK.get()) || !state.hasProperty(BlockStateProperties.LIT) || !state.getValue(BlockStateProperties.LIT))
                return false;
            if (addFuel(world, pos, state, campfire, stack, fuelDur)) {
                world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS);
                return true;
            }
        }
        return false;
    }

    private static boolean addFuel(Level world, BlockPos pos, @NotNull BlockState state, ICampfireBlockEntity campfire, @NotNull ItemStack fuel, int fuelDur) {
        if (state.is(Hcs.SMOLDERING_CAMPFIRE_BLOCK.get())) {
            fuelDur = (int) (fuelDur * 1.5); // Burning Duration↑
            // Modified: setBlockState -> setBlock (Mojang Mapping)
            world.setBlock(pos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.FACING, state.getValue(CampfireBlock.FACING)).setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED)), 3);
            if (world.getBlockEntity(pos) instanceof ICampfireBlockEntity camp)
                if (camp.setBurnOutTime(world.getGameTime() + fuelDur)) {
                    fuel.shrink(1);
                    return true;
                }
        } else  // Regular campfires
            if (campfire.setBurnOutTime(campfire.getBurnOutTime() + (int)(fuelDur * 1.5))) {
                fuel.shrink(1);
                return true;
            }
        return false;
    }

    public static boolean isFuelableCampfire(@Nullable Item item) {
        if (item == null) return false;
        return item == Items.CAMPFIRE;
    }

    public static void inventoryTick(boolean isSubmerged, Container inv, Player player) {
        if (inv == null) return;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            Item item = stack.getItem();

            boolean isTorch = item == Items.TORCH, isBurningCrudeTorch = item == Hcs.BURNING_CRUDE_TORCH_ITEM.get(), isFuelableCampfire = CombustionHelper.isFuelableCampfire(item);
            if ((isSubmerged || ((isBurningCrudeTorch || isFuelableCampfire) && BurningCrudeTorchItem.shouldExtinguish(stack)))
                    && (isTorch || isBurningCrudeTorch || isFuelableCampfire)) {
                Item extinguishedItem = Items.AIR;
                int extinguishCount = stack.getCount();
                if (isTorch) extinguishedItem = Hcs.UNLIT_TORCH_ITEM.get();
                else if (isFuelableCampfire) {
                    extinguishedItem = Hcs.ASHES.get();
                    extinguishCount = 6;
                }
                inv.setItem(i, new ItemStack(extinguishedItem, extinguishCount));
                if (player != null)
                    player.level().playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS);
            }
        }
    }
}