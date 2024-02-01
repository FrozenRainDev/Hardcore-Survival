package biz.coolpage.hcs.block;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.entity.CrudeTorchBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class AbstractCrudeTorchBlock extends BlockWithEntity implements BlockEntityProvider {
    public AbstractCrudeTorchBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState().with(Properties.LIT, false));
    }

    public static void ignite(@NotNull World world, @NotNull BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof AbstractCrudeTorchBlock && world.getBlockEntity(pos) instanceof CrudeTorchBlockEntity torch) {
            torch.igniteSync();
            world.setBlockState(pos, state.with(Properties.LIT, true));
        }
    }

    public static void extinguish(@NotNull World world, @NotNull BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof AbstractCrudeTorchBlock && world.getBlockEntity(pos) instanceof CrudeTorchBlockEntity torch) {
            torch.extinguishSync();
            world.setBlockState(pos, state.with(Properties.LIT, false));
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS);
            if (world instanceof ServerWorld serverWorld) serverWorld.getChunkManager().markForUpdate(pos);
        }
    }

    // A shared method for all campfires and torches blocks, which is used to handle ignite logics.
    public static boolean onIgnite(@NotNull World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand, @Nullable Consumer<BlockEntity> operation) {
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();
        //TODO fire bow lit
        //TODO use lava or fire to lit while holding crude torches
        final boolean isFireCharge = item == Items.FIRE_CHARGE;
        final boolean isFlintAndSteel = item == Items.FLINT_AND_STEEL;
        final boolean isTorch = item instanceof BlockItem blockItem && (blockItem.getBlock() instanceof TorchBlock && item != Items.REDSTONE_TORCH && item != Reg.CRUDE_TORCH_ITEM);
        if (isFlintAndSteel || isFireCharge || isTorch) {
            if (!player.isCreative()) {
                if (isFlintAndSteel) stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                else if (isFireCharge) stack.decrement(1);
            }
            if (operation != null) operation.accept(world.getBlockEntity(pos));
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS);
            return true;
        }
        return false;
    }

    @Override
    protected void appendProperties(StateManager.@NotNull Builder<Block, BlockState> builder) {
        builder.add(Properties.LIT);
    }

    @Override
    public Item asItem() {
        return Reg.BURNING_CRUDE_TORCH_ITEM;
    }

    @Override
    protected Block asBlock() {
        return Reg.CRUDE_TORCH_BLOCK;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CrudeTorchBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL; // CRUX: models invisible without this
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, Reg.CRUDE_TORCH_BLOCK_ENTITY, CrudeTorchBlockEntity::tick);
    }

    @Override
    public void randomDisplayTick(@NotNull BlockState state, World world, BlockPos pos, Random random) {
        if (!state.get(Properties.LIT)) return;
        for (int i = 0; i < 4; ++i)
            world.addParticle(ParticleTypes.SMOKE, pos.getX(), pos.getY(), pos.getZ(), 0.0, 5.0E-4, 0.0);
    }

    @Override
    public ActionResult onUse(BlockState state, @NotNull World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (onIgnite(world, pos, player, hand, null)) {
            world.setBlockState(pos, state.with(Properties.LIT, true));
            return ActionResult.success(world.isClient);
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }


    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        if (stack.isOf(Reg.BURNING_CRUDE_TORCH_ITEM))
            ignite(world, state, pos);
        else if (stack.isOf(Reg.CRUDE_TORCH_ITEM) && world.getBlockEntity(pos) instanceof CrudeTorchBlockEntity torch)
            torch.extinguishSync();
    }
}
