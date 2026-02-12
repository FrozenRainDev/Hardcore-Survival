package biz.coolpage.hcs.util;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ToolActions;

public class HcsPredicates {
    public static boolean isTool(Object object) {
        ItemStack stack = null;
        if (object instanceof Item item) {
            stack = new ItemStack(item);
        }
        if (object instanceof ItemStack stack1) {
            stack = stack1;
        }
        if (stack == null) return false;
        return stack.is(ItemTags.TOOLS) || object instanceof TieredItem || stack.canPerformAction(ToolActions.AXE_DIG) || stack.canPerformAction(ToolActions.HOE_DIG) || stack.canPerformAction(ToolActions.PICKAXE_DIG) || stack.canPerformAction(ToolActions.SHOVEL_DIG);
    }
}
