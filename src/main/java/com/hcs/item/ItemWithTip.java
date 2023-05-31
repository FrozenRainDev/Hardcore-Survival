package com.hcs.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemWithTip extends Item {

    public ItemWithTip(Settings settings, String tip) {
        super(settings);
        this.key = tip;
    }

    String key;

    @Override
    public void appendTooltip(@NotNull ItemStack stack, World world, @NotNull List<Text> tooltip, TooltipContext tooltipContext) {
        tooltip.add(Text.translatable(key).formatted(Formatting.GRAY));
        super.appendTooltip(stack, world, tooltip, tooltipContext);
//        if (RotHelper.canRot(itemStack.getItem())) RotHelper.appendInfo(world, itemStack, tooltip);
    }

}
