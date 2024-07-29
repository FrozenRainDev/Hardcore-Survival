package biz.coolpage.hcs.mixin.item;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.entity.FlintProjectileEntity;
import biz.coolpage.hcs.item.RockItem;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.util.CombustionHelper;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
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
    //The saturationModifier is invalid as saturation is added as same as food level
    //e.g. Cooked chicken increases 6 food levels,and 6 saturation levels,while its saturationModifier=0.6F
    //CANNOT USE `HashMap<Item, FoodComponent>` AS CLASS CAST MAKE THE MAP NOT CONTAIN THE SAME KEY (`Items`)
    @Unique
    private static final FoodComponent SEEDS = new FoodComponent.Builder().hunger(0).saturationModifier(1.0f).alwaysEdible().build();
    @Unique
    private static final FoodComponent WHEAT = new FoodComponent.Builder().hunger(0).saturationModifier(1.0f).build();
    @Unique
    private static final FoodComponent SUGAR = new FoodComponent.Builder().hunger(0).saturationModifier(1.0f).build();
    @Unique
    private static final FoodComponent COCA_BEANS = new FoodComponent.Builder().hunger(1).saturationModifier(1.0f).build();
    @Unique
    private static final FoodComponent SUGAR_CANE = new FoodComponent.Builder().hunger(0).saturationModifier(1.0f).build();
    @Unique
    private static final FoodComponent HONEYCOMB = new FoodComponent.Builder().hunger(4).saturationModifier(2.0f).build();
    @Unique
    private static final FoodComponent BROWN_MUSHROOM = new FoodComponent.Builder().hunger(1).saturationModifier(1.0f).build();
    @Unique
    private static final FoodComponent RED_MUSHROOM = new FoodComponent.Builder().hunger(1).saturationModifier(1.0f).statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200), 1).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 600), 1).build();
    @Unique
    private static final FoodComponent CRIMSON_FUNGUS = new FoodComponent.Builder().hunger(1).saturationModifier(1.0f).statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200), 1).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 600), 1).build();
    @Unique
    private static final FoodComponent WARPED_FUNGUS = new FoodComponent.Builder().hunger(1).saturationModifier(1.0f).statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200), 1).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 600), 1).build();
    @Unique
    private static final FoodComponent KELP = new FoodComponent.Builder().hunger(0).saturationModifier(0.0f).build();
    @Unique
    private static final FoodComponent SEAGRASS = new FoodComponent.Builder().hunger(0).saturationModifier(1.0f).build();
    @Unique
    private static final FoodComponent SEA_PICKLE = new FoodComponent.Builder().hunger(0).saturationModifier(1.0f).build();
    @Unique
    private static final FoodComponent ROTTEN_FLESH = new FoodComponent.Builder().hunger(1).saturationModifier(1.0f).statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200), 1).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 300), 1).statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600), 1).statusEffect(new StatusEffectInstance(HcsEffects.DIARRHEA, 600, 1), 1).statusEffect(new StatusEffectInstance(HcsEffects.FOOD_POISONING, 1200), 1).build();
    @Unique
    private static final FoodComponent COOKED_BEEF = new FoodComponent.Builder().hunger(10).saturationModifier(0.8f).meat().build();
    @Unique
    private static final FoodComponent NETHER_WART = new FoodComponent.Builder().hunger(1).saturationModifier(1.0f).build();
    @Unique
    private static final FoodComponent SWEET_BERRIES = new FoodComponent.Builder().hunger(1).saturationModifier(1.0f).build();
    @Unique
    private static final FoodComponent RABBIT_FOOT = new FoodComponent.Builder().hunger(2).saturationModifier(2.0f).build();
    @Unique
    private static final FoodComponent CHICKEN = new FoodComponent.Builder().hunger(2).saturationModifier(0.3f).meat().build(); //Deleted hunger debuff; Use food poisoning instead (See `DiseaseManager/getParasitePossibilityAndCheckFoodPoisoning()D`)


    @SuppressWarnings("SameReturnValue")
    @Shadow
    public FoodComponent getFoodComponent() {
        return null;
    }

    @Inject(method = "getFoodComponent", at = @At("RETURN"), cancellable = true)
    public void getFoodComponent(CallbackInfoReturnable<FoodComponent> cir) {
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


    @Inject(method = "isFood", at = @At("RETURN"), cancellable = true)
    public void isFood(@NotNull CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(getFoodComponent() != null);
    }


    @Inject(method = "appendTooltip", at = @At("TAIL"))
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        if (stack == null) return;
        Item item = stack.getItem();
        float reachRangeAddition = EntityHelper.getReachRangeAddition(stack);
        if (reachRangeAddition > 0.0F && !(stack.getItem() instanceof BlockItem)
                && applyNullable(stack, s -> s.getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty(), false)) {
            tooltip.add(ScreenTexts.EMPTY);
            tooltip.add(Text.translatable("item.modifiers.mainhand").formatted(Formatting.GRAY));
            //See end of ItemStackMixin/getTooltip1()V
        }
        if (RotHelper.canRot(item) && world != null) RotHelper.appendInfo(world, stack, tooltip);
        String descriptionKey = item.getTranslationKey() + ".description";
        MutableText description = Text.translatable(descriptionKey);
        String descriptionContent = description.getString();
        if (!descriptionContent.equals(descriptionKey))
            tooltip.add(description.formatted((descriptionContent.contains("!") || descriptionContent.contains("！")) ? Formatting.RED : Formatting.GRAY));
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (((Object) this) instanceof Item item) {
            if (item.isFood() && IS_SURVIVAL_LIKE.test(user)/*Both S C sides needed*/ && (user.hasStatusEffect(HcsEffects.FOOD_POISONING) || EntityHelper.getEffectAmplifier(user, HcsEffects.OVEREATEN) > 0)) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
                user.getItemCooldownManager().set(item, 60);
                cir.setReturnValue(TypedActionResult.fail(user.getStackInHand(hand)));
            } else if (item == Items.FLINT)
                cir.setReturnValue(RockItem.throwOut(world, user, hand, new FlintProjectileEntity(user, world)));
        }
    }

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    public final void getMaxCount(CallbackInfoReturnable<Integer> cir) {
        if (((Object) this) instanceof Item item) {
            if (item instanceof PotionItem) cir.setReturnValue(16);
            if (CombustionHelper.isFuelableCampfire(item)) cir.setReturnValue(1);
        }
    }

    @Inject(method = "isItemBarVisible", at = @At("HEAD"), cancellable = true)
    public void isItemBarVisible(@NotNull ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (CombustionHelper.isFuelableCampfire(stack.getItem()))
            cir.setReturnValue(Reg.BURNING_CRUDE_TORCH_ITEM.isItemBarVisible(stack));
    }

    @Inject(method = "getItemBarStep", at = @At("HEAD"), cancellable = true)
    public void getItemBarStep(@NotNull ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (CombustionHelper.isFuelableCampfire(stack.getItem()))
            cir.setReturnValue(Reg.BURNING_CRUDE_TORCH_ITEM.getItemBarStep(stack));
    }

    @Inject(method = "getItemBarColor", at = @At("HEAD"), cancellable = true)
    public void getItemBarColor(@NotNull ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (CombustionHelper.isFuelableCampfire(stack.getItem()))
            cir.setReturnValue(Reg.BURNING_CRUDE_TORCH_ITEM.getItemBarColor(stack));
    }
}

