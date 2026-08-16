package biz.coolpage.hcs.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

public class LeavesReinforcerItem extends Item {
    public LeavesReinforcerItem(Properties pProperties) {
        super(pProperties);
    }

    public static final BooleanProperty REINFORCED_LEAVES = BooleanProperty.create("hcs_reinforced_leaves");

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState state = level.getBlockState(pos);

        // Check if the block is leaves and has our custom property
        if (state.is(BlockTags.LEAVES) && state.hasProperty(REINFORCED_LEAVES)) {
            // Check if it is not already reinforced
            if (!state.getValue(REINFORCED_LEAVES)) {
                if (!level.isClientSide) {
                    // Update the blockstate to reinforced
                    level.setBlock(pos, state.setValue(REINFORCED_LEAVES, true), 3);
                    level.playSound(null, pos, SoundEvents.BIG_DRIPLEAF_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);

                    // Consume the item if the player is not in creative mode
                    Player player = pContext.getPlayer();
                    if (player != null && !player.isCreative()) {
                        pContext.getItemInHand().shrink(1);
                    }
                } else {
                    // Spawn particles on the client side to indicate successful reinforcement
                    for (int i = 0; i < 7; i++) {
                        double d0 = level.random.nextGaussian() * 0.02D;
                        double d1 = level.random.nextGaussian() * 0.02D;
                        double d2 = level.random.nextGaussian() * 0.02D;
                        level.addParticle(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                                pos.getX() + level.random.nextDouble(),
                                pos.getY() + level.random.nextDouble(),
                                pos.getZ() + level.random.nextDouble(),
                                d0, d1, d2);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return super.useOn(pContext);
    }
}