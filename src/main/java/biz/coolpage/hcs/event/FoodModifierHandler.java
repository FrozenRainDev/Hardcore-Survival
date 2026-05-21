package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.entity.FlintProjectileEntity;
import biz.coolpage.hcs.item.RockItem;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.util.CombustionHelper;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_LIKE;

public class FoodModifierHandler {
    // The saturationModifier is invalid as saturation is added as same as food level
    // e.g. Cooked chicken increases 6 food levels, and 6 saturation levels, while its saturationModifier=0.6F
    // CANNOT USE `HashMap<Item, FoodProperties>` AS CLASS CAST MAKE THE MAP NOT CONTAIN THE SAME KEY (`Items`)
    private static final FoodProperties SEEDS = new FoodProperties.Builder().nutrition(0).saturationMod(1.0f).alwaysEat().build();
    private static final FoodProperties WHEAT = new FoodProperties.Builder().nutrition(0).saturationMod(1.0f).build();
    private static final FoodProperties SUGAR = new FoodProperties.Builder().nutrition(0).saturationMod(1.0f).build();
    private static final FoodProperties COCA_BEANS = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).build();
    private static final FoodProperties SUGAR_CANE = new FoodProperties.Builder().nutrition(0).saturationMod(1.0f).build();
    private static final FoodProperties HONEYCOMB = new FoodProperties.Builder().nutrition(4).saturationMod(2.0f).build();
    private static final FoodProperties BROWN_MUSHROOM = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).build();
    private static final FoodProperties RED_MUSHROOM = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(new MobEffectInstance(MobEffects.CONFUSION, 200), 1).effect(new MobEffectInstance(MobEffects.POISON, 600), 1).build();
    private static final FoodProperties CRIMSON_FUNGUS = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(new MobEffectInstance(MobEffects.CONFUSION, 200), 1).effect(new MobEffectInstance(MobEffects.POISON, 600), 1).build();
    private static final FoodProperties WARPED_FUNGUS = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(new MobEffectInstance(MobEffects.CONFUSION, 200), 1).effect(new MobEffectInstance(MobEffects.POISON, 600), 1).build();
    private static final FoodProperties KELP = new FoodProperties.Builder().nutrition(0).saturationMod(0.0f).build();
    private static final FoodProperties SEAGRASS = new FoodProperties.Builder().nutrition(0).saturationMod(1.0f).build();
    private static final FoodProperties SEA_PICKLE = new FoodProperties.Builder().nutrition(0).saturationMod(1.0f).build();
    private static FoodProperties rottenFlesh;
    private static final FoodProperties COOKED_BEEF = new FoodProperties.Builder().nutrition(10).saturationMod(0.8f).meat().build();
    private static final FoodProperties NETHER_WART = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).build();
    private static final FoodProperties SWEET_BERRIES = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).build();
    private static final FoodProperties RABBIT_FOOT = new FoodProperties.Builder().nutrition(2).saturationMod(2.0f).build();
    private static final FoodProperties CHICKEN = new FoodProperties.Builder().nutrition(2).saturationMod(0.3f).meat().build(); // Deleted hunger debuff; Use food poisoning instead
    private static final FoodProperties HONEY_BOTTLE = new FoodProperties.Builder().nutrition(3).saturationMod(1.5f).build();


    private static FoodProperties getRottenFlesh() {
        if (rottenFlesh == null) {
            rottenFlesh = new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(new MobEffectInstance(MobEffects.CONFUSION, 200), 1).effect(new MobEffectInstance(MobEffects.POISON, 300), 1).effect(new MobEffectInstance(MobEffects.HUNGER, 600), 1).effect(new MobEffectInstance(HcsEffects.DIARRHEA.get(), 600, 1), 1).effect(new MobEffectInstance(HcsEffects.FOOD_POISONING.get(), 1200), 1).build();
        }
        return rottenFlesh;
    }

    // Handles mod lifecycle setup (MOD Bus)
    @Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                try {
                    // Reflection fields: foodProperties & maxStackSize
                    Field foodField = ObfuscationReflectionHelper.findField(Item.class, "f_41380_");
                    Field maxStackField = ObfuscationReflectionHelper.findField(Item.class, "f_41370_");
                    foodField.setAccessible(true);
                    maxStackField.setAccessible(true);

                    // 1. Inject Food Properties
                    foodField.set(Items.BEETROOT_SEEDS, SEEDS);
                    foodField.set(Items.MELON_SEEDS, SEEDS);
                    foodField.set(Items.PUMPKIN_SEEDS, SEEDS);
                    foodField.set(Items.WHEAT_SEEDS, SEEDS);
                    foodField.set(Items.TORCHFLOWER_SEEDS, SEEDS);
                    foodField.set(Items.WHEAT, WHEAT);
                    foodField.set(Items.SUGAR, SUGAR);
                    foodField.set(Items.COCOA_BEANS, COCA_BEANS);
                    foodField.set(Items.SUGAR_CANE, SUGAR_CANE);
                    foodField.set(Items.HONEYCOMB, HONEYCOMB);
                    foodField.set(Items.BROWN_MUSHROOM, BROWN_MUSHROOM);
                    foodField.set(Items.RED_MUSHROOM, RED_MUSHROOM);
                    foodField.set(Items.CRIMSON_FUNGUS, CRIMSON_FUNGUS);
                    foodField.set(Items.WARPED_FUNGUS, WARPED_FUNGUS);
                    foodField.set(Items.KELP, KELP);
                    foodField.set(Items.SEAGRASS, SEAGRASS);
                    foodField.set(Items.SEA_PICKLE, SEA_PICKLE);
                    foodField.set(Items.ROTTEN_FLESH, getRottenFlesh());
                    foodField.set(Items.COOKED_BEEF, COOKED_BEEF);
                    foodField.set(Items.NETHER_WART, NETHER_WART);
                    foodField.set(Items.SWEET_BERRIES, SWEET_BERRIES);
                    foodField.set(Items.GLOW_BERRIES, SWEET_BERRIES);
                    foodField.set(Items.RABBIT_FOOT, RABBIT_FOOT);
                    foodField.set(Items.MELON_SLICE, BROWN_MUSHROOM);
                    foodField.set(Items.GLISTERING_MELON_SLICE, HONEYCOMB);
                    foodField.set(Items.CHICKEN, CHICKEN);
                    foodField.set(Items.HONEY_BOTTLE, HONEY_BOTTLE);

                    // 2. Adjust Max Stack Sizes for specific items
                    for (Item item : ForgeRegistries.ITEMS.getValues()) {
                        if (item instanceof PotionItem) {
                            maxStackField.set(item, 16);
                        }
                        if (CombustionHelper.isFuelableCampfire(item)) {
                            maxStackField.set(item, 1);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    // Handles in-game mechanics (FORGE Bus)
    @Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onRightClickItem(PlayerInteractEvent.@NotNull RightClickItem event) {
            Player user = event.getEntity();
            Level world = event.getLevel();
            ItemStack stack = event.getItemStack();
            Item item = stack.getItem();

            // Flint logic
            if (item == Items.FLINT) {
                InteractionResultHolder<ItemStack> result = RockItem.throwOut(world, user, event.getHand(), new FlintProjectileEntity(user, world));
                event.setCanceled(true);
                event.setCancellationResult(result.getResult());
                return;
            }

            // Eating interception logic
            if (item.isEdible() && IS_SURVIVAL_LIKE.test(user) /*Both S C sides needed*/ &&
                    (user.hasEffect(HcsEffects.FOOD_POISONING.get()) || EntityHelper.getEffectAmplifier(user, HcsEffects.OVEREATEN.get()) > 0)) {

                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
                user.getCooldowns().addCooldown(item, 60);

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }

        }
    }
}
