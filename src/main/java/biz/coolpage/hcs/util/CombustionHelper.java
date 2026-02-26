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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombustionHelper {
    // A shared method for torches and campfires, which is used to handle initial logics.
    @SuppressWarnings("unused")
    public static boolean onLit(@NotNull Level level, BlockPos pos, @NotNull Player player, InteractionHand hand) {
        if (!player.getAbilities().mayBuild) return false;
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        final boolean isFireCharge = item == Items.FIRE_CHARGE;
        final boolean isFlintAndSteel = item == Items.FLINT_AND_STEEL;
        final boolean isTorch = isTorchWithFlame(item);

        if (isFlintAndSteel || isFireCharge || isTorch) {
            if (!player.isCreative()) {
                if (isFlintAndSteel) {
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                } else if (isFireCharge) {
                    stack.shrink(1);
                }
            }
            player.awardStat(Stats.ITEM_USED.get(item));
            level.playSound(null, pos,
                    isFlintAndSteel ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRECHARGE_USE,
                    SoundSource.BLOCKS);
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
        Level level = context.getLevel();

        if (state.is(BlockTags.FIRE) ||
                state.is(BlockTags.CAMPFIRES) ||
                isTorchWithFlame(block.asItem()) ||
                (block instanceof AbstractFurnaceBlock &&
                        state.getValue(BlockStateProperties.LIT) &&
                        player.isShiftKeyDown())) {
            litHoldingTorch(player, level, stack);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return null;
    }

    public static void litHoldingTorch(Player player, @NotNull Level level, @NotNull ItemStack stack) {
        EntityHelper.dropItem(player,
                stack.is(Hcs.CRUDE_TORCH_ITEM) ? Hcs.BURNING_CRUDE_TORCH_ITEM : Items.TORCH);
        stack.shrink(1); // This operation must perform after the dropping item process considering condition when players holding single torch
        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS);
    }

    public static boolean isTorchWithFlame(Item item) {
        return item == Hcs.BURNING_CRUDE_TORCH_ITEM ||
                (item instanceof BlockItem blockItem &&
                        blockItem.getBlock() instanceof TorchBlock &&
                        item != Items.REDSTONE_TORCH &&
                        item != Hcs.CRUDE_TORCH_ITEM &&
                        item != Hcs.UNLIT_TORCH_ITEM &&
                        item != Hcs.GLOWSTONE_TORCH_ITEM);
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

    public static void onServerTick(@NotNull Level level, BlockPos pos, @NotNull BlockState state, ICampfireBlockEntity campfire) {
        // Campfire extinguish: Normal -> Smoldering -> Burnt
        boolean hasFlame = !state.is(Hcs.SMOLDERING_CAMPFIRE_BLOCK);
        if (hasFlame && level.getRandom().nextFloat() < 0.001F) // Lit inflammable blocks nearby
            Fluids.LAVA.randomTick(level, pos, Fluids.LAVA.defaultFluidState(), level.getRandom());

        long time = level.getGameTime();
        long burnOutTime = campfire.getBurnOutTime();

        if (burnOutTime < time) {
            CampfireBlock.dowse(null, level, pos, state);
            // "setBlockState" causes automatic cooking items drop (See CampfireBlock::onRemove)
            level.setBlock(pos,
                    (hasFlame ? Hcs.SMOLDERING_CAMPFIRE_BLOCK : Hcs.BURNT_CAMPFIRE_BLOCK)
                            .defaultBlockState()
                            .setValue(CampfireBlock.FACING, state.getValue(CampfireBlock.FACING))
                            .setValue(CampfireBlock.WATERLOGGED, state.getValue(CampfireBlock.WATERLOGGED)));
            level.levelEvent(null, 1009, pos, 0); // FIRE_EXTINGUISH event
        } else {
            if (burnOutTime == Long.MAX_VALUE && Configs.isEnabled(Configs.BURN))
                campfire.resetBurnOutTime();
            else if (hasFlame) {
                level.setBlock(pos, CombustionHelper.updateCombustionState(state, burnOutTime - time));
            }
        }
    }

    public static int getFuelDuration(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        return ForgeHooks.getBurnTime(stack, null);
    }

    public static boolean checkAddFuel(Level level, BlockPos pos, BlockState state, ItemStack stack) {
        int fuelDur = getFuelDuration(stack) * 2;
        if (fuelDur == 0) return false;
        if (CommUtil.hasNull(level, pos, state)) return false;

        if (level.getBlockEntity(pos) instanceof ICampfireBlockEntity campfire) {
            if (state.is(Hcs.BURNT_CAMPFIRE_BLOCK) ||
                    !state.hasProperty(CampfireBlock.LIT) ||
                    !state.getValue(CampfireBlock.LIT))
                return false;

            if (addFuel(level, pos, state, campfire, stack, fuelDur)) {
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS);
                return true;
            }
        }
        return false;
    }

    private static boolean addFuel(Level level, BlockPos pos, @NotNull BlockState state,
                                   ICampfireBlockEntity campfire, @NotNull ItemStack fuel, int fuelDur) {
        if (state.is(Hcs.SMOLDERING_CAMPFIRE_BLOCK)) {
            fuelDur = (int) (fuelDur * 1.5); // Burning Duration↑
            level.setBlockState(pos,
                    Blocks.CAMPFIRE.defaultBlockState()
                            .setValue(CampfireBlock.FACING, state.getValue(CampfireBlock.FACING))
                            .setValue(CampfireBlock.WATERLOGGED, state.getValue(CampfireBlock.WATERLOGGED)));

            if (level.getBlockEntity(pos) instanceof ICampfireBlockEntity camp)
                if (camp.setBurnOutTime(level.getGameTime() + fuelDur)) {
                    fuel.shrink(1);
                    return true;
                }
        } else { // Hcsular campfires
            if (campfire.setBurnOutTime(campfire.getBurnOutTime() + fuelDur)) {
                fuel.shrink(1);
                return true;
            }
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

            boolean isTorch = item == Items.TORCH;
            boolean isBurningCrudeTorch = item == Hcs.BURNING_CRUDE_TORCH_ITEM;
            boolean isFuelableCampfire = CombustionHelper.isFuelableCampfire(item);

            if ((isSubmerged ||
                    ((isBurningCrudeTorch || isFuelableCampfire) && BurningCrudeTorchItem.shouldExtinguish(stack)))
                    && (isTorch || isBurningCrudeTorch || isFuelableCampfire)) {

                Item extinguishedItem = Items.AIR;
                int extinguishCount = stack.getCount();

                if (isTorch) {
                    extinguishedItem = Hcs.UNLIT_TORCH_ITEM;
                } else if (isFuelableCampfire) {
                    extinguishedItem = Hcs.ASHES;
                    extinguishCount = 6;
                }

                inv.setItem(i, new ItemStack(extinguishedItem, extinguishCount));
                if (player != null)
                    player.level().playSound(null, player.blockPosition(),
                            SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS);
            }
        }
    }
}
