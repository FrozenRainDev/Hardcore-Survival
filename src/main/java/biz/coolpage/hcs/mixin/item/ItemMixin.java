package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.entity.FlintProjectileEntity;
import biz.coolpage.hcs.item.RockItem;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.util.CombustionHelper;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;
import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_LIKE;

@Mixin(Item.class)
@SuppressWarnings("ConstantValue")
public class ItemMixin {
    // The saturationModifier is invalid as saturation is added as same as food level
    // e.g. Cooked chicken increases 6 food levels, and 6 saturation levels, while its saturationModifier=0.6F
    // CANNOT USE `HashMap<Item, FoodProperties>` AS CLASS CAST MAKE THE MAP NOT CONTAIN THE SAME KEY (`Items`)
    @Unique
    private static final FoodProperties SEEDS = new FoodProperties.Builder().nutrition(0).saturationMod(1.0f).alwaysEat().build();
    @Unique
    private static final FoodProperties WHEAT = new FoodProperties.Builder().nutrition(0).saturationMod(1.0f).build();
    @Unique
    private static final FoodProperties SUGAR = new FoodProperties.Builder().nutrition(0).saturationMod(1.0f).build();
    @Unique
    private static final FoodProperties COCA_BEANS = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).build();
    @Unique
    private static final FoodProperties SUGAR_CANE = new FoodProperties.Builder().nutrition(0).saturationMod(1.0f).build();
    @Unique
    private static final FoodProperties HONEYCOMB = new FoodProperties.Builder().nutrition(4).saturationMod(2.0f).build();
    @Unique
    private static final FoodProperties BROWN_MUSHROOM = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).build();
    @Unique
    private static final FoodProperties RED_MUSHROOM = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(new MobEffectInstance(MobEffects.CONFUSION, 200), 1).effect(new MobEffectInstance(MobEffects.POISON, 600), 1).build();
    @Unique
    private static final FoodProperties CRIMSON_FUNGUS = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(new MobEffectInstance(MobEffects.CONFUSION, 200), 1).effect(new MobEffectInstance(MobEffects.POISON, 600), 1).build();
    @Unique
    private static final FoodProperties WARPED_FUNGUS = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(new MobEffectInstance(MobEffects.CONFUSION, 200), 1).effect(new MobEffectInstance(MobEffects.POISON, 600), 1).build();
    @Unique
    private static final FoodProperties KELP = new FoodProperties.Builder().nutrition(0).saturationMod(0.0f).build();
    @Unique
    private static final FoodProperties SEAGRASS = new FoodProperties.Builder().nutrition(0).saturationMod(1.0f).build();
    @Unique
    private static final FoodProperties SEA_PICKLE = new FoodProperties.Builder().nutrition(0).saturationMod(1.0f).build();
    @Unique
    private static final FoodProperties ROTTEN_FLESH = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(new MobEffectInstance(MobEffects.CONFUSION, 200), 1).effect(new MobEffectInstance(MobEffects.POISON, 300), 1).effect(new MobEffectInstance(MobEffects.HUNGER, 600), 1).effect(new MobEffectInstance(HcsEffects.DIARRHEA, 600, 1), 1).effect(new MobEffectInstance(HcsEffects.FOOD_POISONING, 1200), 1).build();
    @Unique
    private static final FoodProperties COOKED_BEEF = new FoodProperties.Builder().nutrition(10).saturationMod(0.8f).meat().build();
    @Unique
    private static final FoodProperties NETHER_WART = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).build();
    @Unique
    private static final FoodProperties SWEET_BERRIES = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).build();
    @Unique
    private static final FoodProperties RABBIT_FOOT = new FoodProperties.Builder().nutrition(2).saturationMod(2.0f).build();
    @Unique
    private static final FoodProperties CHICKEN = new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).meat().build(); // Deleted hunger debuff; Use food poisoning instead


    @SuppressWarnings("SameReturnValue")
    @Shadow
    public FoodProperties getFoodProperties() {
        return null;
    }

    @Inject(method = "getFoodProperties", at = @At("RETURN"), cancellable = true)
    public void getFoodProperties(CallbackInfoReturnable<FoodProperties> cir) {
        Item item = ((Item) (Object) this);
        if (item == Items.BEETROOT_SEEDS || item == Items.MELON_SEEDS || item == Items.PUMPKIN_SEEDS || item == Items.WHEAT_SEEDS || item == Items.TORCHFLOWER_SEEDS)
            cir.setReturnValue(SEEDS);
        else if (item == Items.WHEAT) cir.setReturnValue(WHEAT);
        else if (item == Items.SUGAR) cir.setReturnValue(SUGAR);
        else if (item == Items.COCOA_BEANS) cir.setReturnValue(COCA_BEANS);
        else if (item == Items.SUGAR_CANE) cir.setReturnValue(SUGAR_CANE);
        else if (item == Items.HONEYCOMB) cir.setReturnValue(HONEYCOMB);
        else if (item == Items.BROWN_MUSHROOM) cir.setReturnValue(BROWN_MUSHROOM);
        else if (item == Items.RED_MUSHROOM) cir.setReturnValue(RED_MUSHROOM);
        else if (item == Items.CRIMSON_FUNGUS) cir.setReturnValue(CRIMSON_FUNGUS);
        else if (item == Items.WARPED_FUNGUS) cir.setReturnValue(WARPED_FUNGUS);
        else if (item == Items.KELP) cir.setReturnValue(KELP);
        else if (item == Items.SEAGRASS) cir.setReturnValue(SEAGRASS);
        else if (item == Items.SEA_PICKLE) cir.setReturnValue(SEA_PICKLE);
        else if (item == Items.ROTTEN_FLESH) cir.setReturnValue(ROTTEN_FLESH);
        else if (item == Items.COOKED_BEEF) cir.setReturnValue(COOKED_BEEF);
        else if (item == Items.NETHER_WART) cir.setReturnValue(NETHER_WART);
        else if (item == Items.SWEET_BERRIES || item == Items.GLOW_BERRIES) cir.setReturnValue(SWEET_BERRIES);
        else if (item == Items.RABBIT_FOOT) cir.setReturnValue(RABBIT_FOOT);
        else if (item == Items.MELON_SLICE) cir.setReturnValue(BROWN_MUSHROOM);
        else if (item == Items.GLISTERING_MELON_SLICE) cir.setReturnValue(HONEYCOMB);
        else if (item == Items.CHICKEN) cir.setReturnValue(CHICKEN);
    }


    @Inject(method = "isEdible", at = @At("RETURN"), cancellable = true)
    public void isEdible(@NotNull CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(getFoodProperties() != null);
    }


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

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (((Object) this) instanceof Item item) {
            if (item.isEdible() && IS_SURVIVAL_LIKE.test(user)/*Both S C sides needed*/ && (user.hasEffect(HcsEffects.FOOD_POISONING) || EntityHelper.getEffectAmplifier(user, HcsEffects.OVEREATEN) > 0)) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
                user.getCooldowns().addCooldown(item, 60);
                cir.setReturnValue(InteractionResultHolder.fail(user.getItemInHand(hand)));
            } else if (item == Items.FLINT)
                cir.setReturnValue(RockItem.throwOut(world, user, hand, new FlintProjectileEntity(user, world)));
        }
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
            cir.setReturnValue(Hcs.BURNING_CRUDE_TORCH_ITEM.isBarVisible(stack));
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    public void getBarWidth(@NotNull ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (CombustionHelper.isFuelableCampfire(stack.getItem()))
            cir.setReturnValue(Hcs.BURNING_CRUDE_TORCH_ITEM.getBarWidth(stack));
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    public void getBarColor(@NotNull ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (CombustionHelper.isFuelableCampfire(stack.getItem()))
            cir.setReturnValue(Hcs.BURNING_CRUDE_TORCH_ITEM.getBarColor(stack));
    }
}