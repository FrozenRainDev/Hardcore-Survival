package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.entity.BurningCrudeTorchBlockEntity;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CrudeTorchBlock extends TorchBlock {
    // Extinguished crude torch block
    public CrudeTorchBlock(Settings settings) {
        super(settings, ParticleTypes.FLAME);
    }

    // A shared method for torches and campfires, which is used to handle initial logics.
    @SuppressWarnings("unused")
    public static boolean onLit(@NotNull World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand) {
        if (!player.getAbilities().allowModifyWorld) return false;
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();
        final boolean isFireCharge = item == Items.FIRE_CHARGE, isFlintAndSteel = item == Items.FLINT_AND_STEEL;
        final boolean isTorch = isFlammableTorch(item);
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

    public static @Nullable ActionResult preLitHoldingTorch(@NotNull ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) return null;
        ItemStack stack = context.getStack();
        BlockPos pos = context.getBlockPos();
        BlockState state = context.getWorld().getBlockState(pos);
        Block block = state.getBlock();
        World world = context.getWorld();
        if (state.isIn(BlockTags.FIRE) || state.isIn(BlockTags.CAMPFIRES)
                || CrudeTorchBlock.isFlammableTorch(block.asItem())
                || (block instanceof AbstractFurnaceBlock && state.get(Properties.LIT) && player.isSneaking())) {
            litHoldingTorch(player, world, stack);
            return ActionResult.success(world.isClient);
        }
        return null;
    }

    public static void litHoldingTorch(PlayerEntity player, @NotNull World world, @NotNull ItemStack stack) {
        stack.decrement(1);
        EntityHelper.dropItem(player, stack.isOf(Reg.CRUDE_TORCH_ITEM) ? Reg.BURNING_CRUDE_TORCH_ITEM : Items.TORCH);
        world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS);
    }


    public static boolean isFlammableTorch(Item item) {
        return item == Reg.BURNING_CRUDE_TORCH_ITEM || (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof TorchBlock && item != Items.REDSTONE_TORCH && item != Reg.CRUDE_TORCH_ITEM && item != Reg.UNLIT_TORCH_ITEM);
    }

    protected BlockState getLitBlockStateForUpdate(BlockState prevStat) {
        return Reg.BURNING_CRUDE_TORCH_BLOCK.getDefaultState();
    }

    @Override
    public ActionResult onUse(BlockState state, @NotNull World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (onLit(world, pos, player, hand)) {
            world.setBlockState(pos, this.getLitBlockStateForUpdate(state));
            if (world.getBlockEntity(pos) instanceof BurningCrudeTorchBlockEntity torch)
                torch.onLit(player);
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public Item asItem() {
        return Reg.CRUDE_TORCH_ITEM;
    }

    @Override
    protected Block asBlock() {
        return Reg.CRUDE_TORCH_BLOCK;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        // Just do nothing
    }
}
