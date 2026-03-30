package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Hcs;
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
            // 1.20.1 中 ArmorItem 使用 getType().getSlot() 获取设备槽位
            protectionAmount = b ? ArmorHelper.CustomDecimalProtection.get(material, armor.getType().getSlot()) : armor.getDefense();
            MutableFloat delta = new MutableFloat(0.0F);
            ArmorHelper.eachArmorDeltaProcess(stack, delta);
            protectionAmount += delta.getValue();

        }
        return String.format("%.2f", protectionAmount);
    }

    /* WTF??? Called consistently for EVERY same stack (perceived as different stacks combination)
    Fk u, mojang, infernal shit codes
    Case solved, the method that 1.19 had to be called only when different items were merged,
    to 1.20 will be called all the time for the same item pile when it doesn't need to be merged,
    which is equivalent to constantly merging with itself.
    In other words, it keeps calculating the freshness of the merge with itself, and since it uses floats,
     there is bound to be an error,
    and the error accumulates after a lot of calculations, resulting in a continuous and slow decrease of the expiration time NBT. */
    @Inject(at = @At("HEAD"), method = "isSameItemSameTags", cancellable = true)
    private static void canCombine(@NotNull ItemStack stack, ItemStack otherStack, CallbackInfoReturnable<Boolean> cir) {
        // Combine same kinds of food stacks with different freshness
        CompoundTag nbt1 = stack.getOrCreateTag().copy(); // DO NOT FORGET!
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
    public void isOf(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (Hcs.IMPROVISED_SHIELD.isPresent() && this.getItem() == Hcs.IMPROVISED_SHIELD.get() && item == Items.SHIELD)
            cir.setReturnValue(true);
    }

    //Modify stacks tooltips for armors and tools
    @Inject(at = @At("RETURN"), method = "getTooltipLines", cancellable = true)
    public void getTooltip1(@Nullable Player player, TooltipFlag context, @NotNull CallbackInfoReturnable<List<Component>> cir) {
        if ((Object) this instanceof ItemStack stack) {
            var texts = cir.getReturnValue();
            AtomicBoolean noArmorPlusModifier = new AtomicBoolean(true);
            boolean shouldAppendToolInfo = false;
            if (this.getItem() instanceof ArmorItem armor) {
                texts.replaceAll(text -> {
                    if (text.getContents() instanceof TranslatableContents translatable) {
                        // No such expression in non-protection armor such as wool boots, See noArmorPlusModifier
                        if (translatable.getKey().contains("attribute.modifier.plus.") && noArmorPlusModifier.get()
                            /*It does not indicate no modifier here, but means if the text is modified at the first time, as multiple modification will obfuscate armor protection value with armor toughness value, etc.*/) {
                            String protectionAmount = String.valueOf(getProtectionAmount(stack));
                            noArmorPlusModifier.set(false);
                            return MutableComponent.create(Component.literal(text.getString().replaceAll("\\d", protectionAmount)).getContents()).withStyle(ChatFormatting.BLUE);
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
                    //Do not onInteract for each -- Avoid ConcurrentModificationException
                    var text = texts.get(i);
                    if (text != null && text.getContents() instanceof TranslatableContents translatable) {
                        String key = translatable.getKey();
                        if (key.contains("item.modifiers")) {
                            if (noArmorPlusModifier.get() && this.getItem() instanceof ArmorItem)
                                texts.add(i + 1, Component.translatable("attribute.modifier.plus.0", getProtectionAmount(stack), Component.translatable(Attributes.ARMOR.getDescriptionId())).withStyle(ChatFormatting.BLUE));
                            else if (shouldAppendToolInfo) // Also see ItemMixin/appendTooltip()V
                                texts.add(i + 1, Component.translatable("hcs.tip.reach_range_addition", reachRangeAddition).withStyle(ChatFormatting.DARK_GREEN));
                        }
                    }
                }
            }
            cir.setReturnValue(texts);
        }
    }
}