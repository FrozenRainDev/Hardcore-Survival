package biz.coolpage.hcs.item;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class KnifeItem extends SwordItem {
    public KnifeItem(Tier toolMaterial, int attackDamage, float attackSpeed, Properties settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Can strip bark by knives
        return EntityHelper.dropBark(context);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, @NotNull BlockState state, BlockPos pos, LivingEntity miner) {
        if (state.canBeReplaced()) { //Grass-like blocks
            if (Math.random() < 0.15) stack.hurtAndBreak(1, miner, e -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
            return true;
        }
        return super.mineBlock(stack, world, state, pos, miner);
    }
}