package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin extends RangedWeaponItem {
    public CrossbowItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "appendTooltip", at = @At("HEAD"))
    public void appendTooltip(@NotNull ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        float reachRangeAddition = EntityHelper.getReachRangeAddition(stack);
        if (reachRangeAddition > 0.0F)
            tooltip.add(Text.translatable(Text.translatable("hcs.tip.reach_range_addition").getString() + reachRangeAddition).formatted(Formatting.DARK_GREEN));
    }
}
