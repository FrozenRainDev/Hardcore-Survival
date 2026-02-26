package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin extends ProjectileWeaponItem {
    public CrossbowItemMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "appendHoverText", at = @At("HEAD"))
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag context, CallbackInfo ci) {
        float reachRangeAddition = EntityHelper.getReachRangeAddition(stack);
        if (reachRangeAddition > 0.0F) {
            // Text.translatable -> Component.translatable
            // Formatting -> ChatFormatting
            tooltip.add(Component.translatable(Component.translatable("hcs.tip.reach_range_addition").getString() + reachRangeAddition).withStyle(ChatFormatting.DARK_GREEN));
        }
    }
}