package com.hcs.mixin.item;

import com.hcs.status.HcsEffects;
import com.hcs.util.EntityHelper;
import com.hcs.util.RotHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

@Mixin(Item.class)
public class ItemMixin {
    //The saturationModifier is invalid as saturation is added as same as food level
    //e.g. Cooked chicken increases 6 food levels,and 6 saturation levels,while its saturationModifier=0.6F
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
    private static final FoodComponent ROTTEN_FLESH = new FoodComponent.Builder().hunger(1).saturationModifier(1.0f).statusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200), 1).statusEffect(new StatusEffectInstance(StatusEffects.POISON, 300), 1).statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 600), 1).statusEffect(new StatusEffectInstance(HcsEffects.DIARRHEA, 600, 1), 1).build();
    @Unique
    private static final FoodComponent COOKED_BEEF = new FoodComponent.Builder().hunger(10).saturationModifier(0.8f).meat().build();
    @Unique
    private static final FoodComponent NETHER_WART = new FoodComponent.Builder().hunger(1).saturationModifier(1.0f).build();
    @Unique
    private static final FoodComponent SWEET_BERRIES = new FoodComponent.Builder().hunger(1).saturationModifier(1.0f).build();
    @Unique
    private static final FoodComponent RABBIT_FOOT = new FoodComponent.Builder().hunger(2).saturationModifier(1.0f).build();


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
    }


    @Inject(method = "isFood", at = @At("RETURN"), cancellable = true)
    public void isFood(@NotNull CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(getFoodComponent() != null);
    }


    @Inject(method = "appendTooltip", at = @At("TAIL"))
    public void appendTooltip(@NotNull ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        Item item = stack.getItem();
        if (RotHelper.canRot(stack.getItem()) && world != null) RotHelper.appendInfo(world, stack, tooltip);
        float reachRangeAddition = EntityHelper.getReachRangeAddition(stack);
        if (reachRangeAddition > 0.0F && !(item instanceof BlockItem))
            tooltip.add(Text.translatable(Text.translatable("hcs.tip.reach_range_addition").getString() + reachRangeAddition).formatted(Formatting.GRAY));
    }

}

