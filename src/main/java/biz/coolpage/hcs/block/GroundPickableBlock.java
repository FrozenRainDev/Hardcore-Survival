package biz.coolpage.hcs.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.tags.BlockTags;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

// 通常无法生成是mayPlaceOn的问题，而不是rock_patch.json的问题
public class GroundPickableBlock extends BushBlock {
    // Flat shape close to the ground
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D);
    private final Supplier<ItemStack> dropSupplier;

    public GroundPickableBlock(BlockBehaviour.@NotNull Properties properties, Supplier<ItemStack> dropSupplier) {
        // Apply XZ offset for irregular generation (random positioning)
        super(properties.offsetType(BlockBehaviour.OffsetType.XZ));
        this.dropSupplier = dropSupplier;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        // Shift the hitbox to match the random visual offset
        Vec3 offset = state.getOffset(level, pos);
        return SHAPE.move(offset.x, offset.y, offset.z);
    }

    @Override
    protected boolean mayPlaceOn(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        // Added back BASE_STONE_OVERWORLD so it can spawn in caves on stone/deepslate
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.SNOW)
                || state.is(BlockTags.SAND)
                || state.is(Blocks.TERRACOTTA)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.DEEPSLATE);
    }

    @Override
    public InteractionResult use(BlockState state, @NotNull Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // Pick up logic
        if (!level.isClientSide) {
            ItemStack drop = this.dropSupplier.get().copy();
            // Try adding to inventory, if full drop on ground
            if (!player.getInventory().add(drop)) {
                popResource(level, pos, drop);
            }

            // Remove the block upon pickup
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        // Inherit grass characteristics: can be directly replaced by other blocks or snow layers
        return true;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return this.dropSupplier.get();
    }
}