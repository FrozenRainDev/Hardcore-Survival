package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.ArmorHelper;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
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
            protectionAmount = b ? ArmorHelper.CustomDecimalProtection.get(material, armor.getSlotType()) : armor.getProtection();
            MutableFloat delta = new MutableFloat(0.0F);
            ArmorHelper.eachArmorDeltaProcess(stack, delta);
            protectionAmount += delta.getValue();

        }
        return String.format("%.2f", protectionAmount);
    }

    @Inject(at = @At("HEAD"), method = "canCombine", cancellable = true)
    private static void canCombine(@NotNull ItemStack stack, ItemStack otherStack, CallbackInfoReturnable<Boolean> cir) {
        // Combine same kinds of food stacks with different freshness
        NbtCompound nbt1 = stack.getOrCreateNbt().copy(); // DO NOT FORGET!
        if (nbt1.contains(RotHelper.HFE)) nbt1.remove(RotHelper.HFE);
        if (nbt1.contains(RotHelper.HFI)) nbt1.remove(RotHelper.HFI);
        NbtCompound nbt2 = otherStack.getOrCreateNbt().copy();
        if (nbt2.contains(RotHelper.HFE)) nbt2.remove(RotHelper.HFE);
        if (nbt2.contains(RotHelper.HFI)) nbt2.remove(RotHelper.HFI);
        if (RotHelper.canRot(stack.getItem())) {
            if (stack.getItem() == otherStack.getItem() && (nbt1.toString()).equals(nbt2.toString())) {
                RotHelper.combineNBT(stack, otherStack);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "isOf", cancellable = true)
    public void isOf(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem() == Reg.IMPROVISED_SHIELD && item == Items.SHIELD) cir.setReturnValue(true);
    }

    //Modify stacks tooltips for armors and tools
    @Inject(at = @At("RETURN"), method = "getTooltip", cancellable = true)
    public void getTooltip1(@Nullable PlayerEntity player, TooltipContext context, @NotNull CallbackInfoReturnable<List<Text>> cir) {
        if ((Object) this instanceof ItemStack stack) {
            var texts = cir.getReturnValue();
            AtomicBoolean noArmorPlusModifier = new AtomicBoolean(true);
            boolean shouldAppendToolInfo = false;
            if (this.getItem() instanceof ArmorItem armor) {
                texts.replaceAll(text -> {
                    if (text.getContent() instanceof TranslatableTextContent translatable) {
                        // No such expression in non-protection armor such as wool boots, See noArmorPlusModifier
                        if (translatable.getKey().contains("attribute.modifier.plus.") && noArmorPlusModifier.get()
                            /*It does not indicate no modifier here, but means if the text is modified at the first time, as multiple modification will obfuscate armor protection value with armor toughness value, etc.*/) {
                            String protectionAmount = String.valueOf(getProtectionAmount(stack));
                            noArmorPlusModifier.set(false);
                            return MutableText.of(Text.of(text.getString().replaceAll("\\d", protectionAmount)).getContent()).formatted(Formatting.BLUE);
                        }
                    }
                    return text;
                });
                if (armor.getMaterial() == ArmorMaterials.LEATHER)
                    texts.add(Text.translatable("hcs.tip.when_in_leather").formatted(Formatting.GRAY));
            }
            float reachRangeAddition = EntityHelper.getReachRangeAddition(stack);
            if (reachRangeAddition > 0.0F && !(stack.getItem() instanceof BlockItem)) shouldAppendToolInfo = true;
            if (noArmorPlusModifier.get() || shouldAppendToolInfo) {
                for (int i = 0; i < texts.size(); ++i) {
                    //Do not onInteract for each -- Avoid ConcurrentModificationException
                    var text = texts.get(i);
                    if (text != null && text.getContent() instanceof TranslatableTextContent translatable) {
                        String key = translatable.getKey();
                        if (key.contains("item.modifiers")) {
                            if (noArmorPlusModifier.get() && this.getItem() instanceof ArmorItem)
                                texts.add(i + 1, Text.translatable("attribute.modifier.plus.0", getProtectionAmount(stack), Text.translatable(EntityAttributes.GENERIC_ARMOR.getTranslationKey())).formatted(Formatting.BLUE));
                            else if (shouldAppendToolInfo) // Also see ItemMixin/appendTooltip()V
                                texts.add(i + 1, Text.translatable("hcs.tip.reach_range_addition", reachRangeAddition).formatted(Formatting.DARK_GREEN));
                        }
                    }
                }
            }
            cir.setReturnValue(texts);
        }
    }
}
