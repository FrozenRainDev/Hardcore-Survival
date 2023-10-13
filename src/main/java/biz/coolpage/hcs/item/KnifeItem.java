package biz.coolpage.hcs.item;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class KnifeItem extends SwordItem {
    public KnifeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // Can strip bark by knives
        return EntityHelper.dropBark(context);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, @NotNull BlockState state, BlockPos pos, LivingEntity miner) {
        if (state.isIn(BlockTags.REPLACEABLE_PLANTS)) { //Grass-like blocks
            if (Math.random() < 0.15) stack.damage(1, miner, e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
            return true;
        }
        return super.postMine(stack, world, state, pos, miner);
    }

}
