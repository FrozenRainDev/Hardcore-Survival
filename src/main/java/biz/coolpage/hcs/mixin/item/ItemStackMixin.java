package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.ArmorHelper;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ItemStack.class)
@SuppressWarnings("ConstantValue")
public abstract class ItemStackMixin {

    @Shadow
    public abstract Item getItem();

    @Unique
    private static String getProtectionAmount(ItemStack stack) {
        float protectionAmount = 0.0F;
        if (stack != null && stack.getItem() instanceof ArmorItem armor) {
            var material = armor.getMaterial();
            boolean b = ArmorHelper.CustomDecimalProtection.contains(material);
            protectionAmount = b ? ArmorHelper.CustomDecimalProtection.get(material, armor.getEquipmentSlot()) : armor.getDefense();
            MutableFloat delta = new MutableFloat(0.0F);
            ArmorHelper.eachArmorDeltaProcess(stack, delta);
            protectionAmount += delta.getValue();
        }
        return String.format("%.2f", protectionAmount);
    }

    @Inject(at = @At("HEAD"), method = "canCombine", cancellable = true)
    private static void canCombine(@NotNull ItemStack stack, ItemStack otherStack, CallbackInfoReturnable<Boolean> cir) {
        CompoundTag nbt1 = stack.getOrCreateTag().copy();
        if (nbt1.contains(RotHelper.HFE)) nbt1.remove(RotHelper.HFE);
        if (nbt1.contains(RotHelper.HFI)) nbt1.remove(RotHelper.HFI);
        CompoundTag nbt2 = otherStack.getOrCreateTag().copy();
        if (nbt2.contains(RotHelper.HFE)) nbt2.remove(RotHelper.HFE);
        if (nbt2.contains(RotHelper.HFI)) nbt2.remove(RotHelper.HFI);
        if (RotHelper.canRot(stack.getItem())) {
            if (stack.getItem() == otherStack.getItem() && (nbt1.toString()).equals(nbt2.toString())) {
                RotHelper.combineNBT(stack, otherStack);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "is(Lnet/minecraft/world/item/Item;)Z", cancellable = true)
    public void is(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem() == Reg.IMPROVISED_SHIELD && item == Items.SHIELD) cir.setReturnValue(true);
    }

    @Inject(at = @At("RETURN"), method = "getTooltipLines", cancellable = true)
    public void getTooltipLines(@Nullable Player player, TooltipFlag context, @NotNull CallbackInfoReturnable<List<Component>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        List<Component> texts = cir.getReturnValue();
        AtomicBoolean noArmorPlusModifier = new AtomicBoolean(true);
        boolean shouldAppendToolInfo = false;

        if (this.getItem() instanceof ArmorItem armor) {
            texts.replaceAll(text -> {
                if (text.getContents() instanceof TranslatableContents translatable) {
                    if (translatable.getKey().contains("attribute.modifier.plus.") && noArmorPlusModifier.get()) {
                        String protectionAmount = String.valueOf(getProtectionAmount(stack));
                        noArmorPlusModifier.set(false);
                        return Component.literal(text.getString().replaceAll("\\d+(\\.\\d+)?", protectionAmount)).withStyle(ChatFormatting.BLUE);
                    }
                }
                return text;
            });
            if (armor.getMaterial() == ArmorMaterials.LEATHER)
                texts.add(Component.translatable("hcs.tip.when_in_leather").withStyle(ChatFormatting.GRAY));
        }

        float reachRangeAddition = EntityHelper.getReachRangeAddition(stack);
        if (reachRangeAddition > 0.0F && !(stack.getItem() instanceof BlockItem)) shouldAppendToolInfo = true;

        if (noArmorPlusModifier.get() || shouldAppendToolInfo) {
            for (int i = 0; i < texts.size(); ++i) {
                Component text = texts.get(i);
                if (text.getContents() instanceof TranslatableContents translatable) {
                    String key = translatable.getKey();
                    if (key.contains("item.modifiers")) {
                        if (noArmorPlusModifier.get() && this.getItem() instanceof ArmorItem)
                            texts.add(i + 1, Component.translatable("attribute.modifier.plus.0", getProtectionAmount(stack), Component.translatable(Attributes.ARMOR.getDescriptionId())).withStyle(ChatFormatting.BLUE));
                        else if (shouldAppendToolInfo)
                            texts.add(i + 1, Component.translatable("hcs.tip.reach_range_addition", reachRangeAddition).withStyle(ChatFormatting.DARK_GREEN));
                    }
                }
            }
        }
        cir.setReturnValue(texts);
    }
}