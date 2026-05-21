package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.util.CombustionHelper;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

@Mixin(Item.class)
@SuppressWarnings("ConstantValue")
public abstract class ItemMixin {
    @Inject(method = "appendHoverText", at = @At("TAIL"))
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context, CallbackInfo ci) {
        if (stack == null) return;
        Item item = stack.getItem();
        float reachRangeAddition = EntityHelper.getReachRangeAddition(stack);
        if (reachRangeAddition > 0.0F && !(stack.getItem() instanceof BlockItem)
                && applyNullable(stack, s -> s.getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty(), false)) {
            tooltip.add(CommonComponents.EMPTY);
            tooltip.add(Component.translatable("item.modifiers.mainhand").withStyle(ChatFormatting.GRAY));
        }
        if (RotHelper.canRot(item) && world != null) RotHelper.appendInfo(world, stack, tooltip);
        String descriptionKey = item.getDescriptionId() + ".description";
        MutableComponent description = Component.translatable(descriptionKey);
        String descriptionContent = description.getString();
        if (!descriptionContent.equals(descriptionKey))
            tooltip.add(description.withStyle((descriptionContent.contains("!") || descriptionContent.contains("！")) ? ChatFormatting.RED : ChatFormatting.GRAY));
    }

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    public final void getMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        if (((Object) this) instanceof Item item) {
            if (item instanceof PotionItem) cir.setReturnValue(16);
            if (CombustionHelper.isFuelableCampfire(item)) cir.setReturnValue(1);
        }
    }

    @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
    public void isBarVisible(@NotNull ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (CombustionHelper.isFuelableCampfire(stack.getItem()))
            cir.setReturnValue(Hcs.BURNING_CRUDE_TORCH_ITEM.get().isBarVisible(stack));
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    public void getBarWidth(@NotNull ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (CombustionHelper.isFuelableCampfire(stack.getItem()))
            cir.setReturnValue(Hcs.BURNING_CRUDE_TORCH_ITEM.get().getBarWidth(stack));
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    public void getBarColor(@NotNull ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (CombustionHelper.isFuelableCampfire(stack.getItem()))
            cir.setReturnValue(Hcs.BURNING_CRUDE_TORCH_ITEM.get().getBarColor(stack));
    }
}