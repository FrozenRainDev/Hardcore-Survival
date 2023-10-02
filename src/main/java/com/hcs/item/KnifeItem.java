package com.hcs.item;

import com.hcs.util.EntityHelper;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.ActionResult;

public class KnifeItem extends SwordItem {
    public KnifeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // Can strip bark by knives
        return EntityHelper.dropBark(context);
    }

}
