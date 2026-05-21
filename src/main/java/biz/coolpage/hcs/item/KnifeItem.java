package biz.coolpage.hcs.item;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

public class KnifeItem extends SwordItem {
    public KnifeItem(Tier toolMaterial, int attackDamage, float attackSpeed, Properties settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        // Attempt to strip the log like an axe (This will automatically trigger the BlockToolModificationEvent for drops)
        BlockState strippedState = state.getToolModifiedState(context, ToolActions.AXE_STRIP, false);

        if (strippedState != null) {
            // Play axe stripping sound
            level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);

            if (!level.isClientSide) {
                // Change block to stripped variant
                level.setBlock(pos, strippedState, 11);

                // Damage the knife
                if (player != null) {
                    context.getItemInHand().hurtAndBreak(1, player, e -> e.broadcastBreakEvent(context.getHand()));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useOn(context);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, @NotNull BlockState state, BlockPos pos, LivingEntity miner) {
        if (state.canBeReplaced()) { //Grass-like blocks
            if (Math.random() < 0.15) stack.hurtAndBreak(1, miner, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            return true;
        }
        return super.mineBlock(stack, world, state, pos, miner);
    }

    // Allow the knife to perform axe stripping action officially
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return toolAction == ToolActions.AXE_STRIP || super.canPerformAction(stack, toolAction);
    }
}