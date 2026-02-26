package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.entity.DryingRackBlockEntity;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.SanityManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class RotHelper {
    public static final String HFE = "hcs_food_exp"; // food expiry (ticks)
    public static final String HFF = "hcs_food_fresh"; // percentage of food freshness
    public static final String HFI = "hcs_food_exp_icebox"; // food expiry when in icebox

    // See ItemStackMixin, really annoying :(
    public static void combineNBT(@NotNull ItemStack stackA, @NotNull ItemStack stackB) {
        if (WorldHelper.cannotGetServerWorld()) return;
        int countA = stackA.getCount();
        int countB = stackB.getCount();
        CompoundTag nbtA = stackA.getOrCreateTag();
        CompoundTag nbtB = stackB.getOrCreateTag();
        float freshA = getFresh(WorldHelper.getServerWorld(), stackA);
        float freshB = getFresh(WorldHelper.getServerWorld(), stackB);
        if (Float.compare(freshA, freshB) == 0) // Same freshness doesn't need further operations
            return;
//        System.out.println("further ops");
        if (countA <= 0 || countB <= 0) {
            Hcs.error("RotHelper/combineNBT(); {}", countA + ", " + countB);
            return;
        }
        if (WorldHelper.cannotGetServerWorld()) return;
        float avgFresh = (getFresh(WorldHelper.getServerWorld(), stackA) * countA + getFresh(WorldHelper.getServerWorld(), stackB) * countB) / (float) (countA + countB);
        if (nbtA.contains(HFE) || nbtA.contains(HFI))
            createExp(WorldHelper.getServerWorld(), stackA, avgFresh, nbtA.contains(HFI));
        if (nbtB.contains(HFE) || nbtB.contains(HFI))
            createExp(WorldHelper.getServerWorld(), stackB, avgFresh, nbtB.contains(HFI));
    }

    // To improve performance, do not use "onInteract" "stack.isOf"
    public static boolean canRot(Item item) {
        if (item == null) return false;
        // Check food spoil enabling config
        if (!Configs.isEnabled(Configs.FOOD_SPOIL)) return false;
        // Check special food types
        if (item == Hcs.ROT || item == Hcs.WORM || item == Items.ROTTEN_FLESH || item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE || item == Items.GOLDEN_CARROT || item == Items.GLISTERING_MELON_SLICE || Hcs.IS_BARK.test(item))
            return false;
        String name = item.getDescriptionId();
        if (item == Hcs.SELAGINELLA || item == Items.SUGAR || item == Items.EGG || item == Items.TURTLE_EGG || item == Items.MILK_BUCKET || item == Items.FERMENTED_SPIDER_EYE || item == Hcs.CACTUS_JUICE || item == Items.CAKE || item == Items.RABBIT_FOOT || item.getDescriptionId().contains("seeds") || name.contains("pumpkin") || name.contains("melon"))
            return true;
        return item.isEdible() && item.getFoodProperties() != null && !item.getDefaultInstance().is(ItemTags.FLOWERS);
    }

    public static int getPackageType(@NotNull Item item) {
        return getPackageType(item.getDescriptionId());
    }

    public static int getPackageType(@NotNull String name) {
        if ((name.contains("_can") && !name.contains("cane"))) return 2;//Canned Food
        if (name.contains("_stew") || name.contains("stew_") || name.contains("_salad") || name.contains("_soup") || name.contains("_bucket") || name.contains("bucket_") || name.contains("_bottle") || name.contains("bottle_") || name.contains("_cup") || name.contains("cup_") || name.contains("_juice"))
            return 1; // Food in container
        return 0; // Non-packaged
    }

    public static float getExpDate(Item item) {
        if (item == null) return 7;
        String name = item.getDescriptionId(); // item.getName().toString();
        int packageType = getPackageType(name);
        if (item == Hcs.POTHERB || item == Hcs.FEARLESSNESS_HERB || item == Hcs.ROASTED_SEEDS || item == Items.PUMPKIN_PIE || item == Items.KELP)
            return 10;
        if (item == Items.WHEAT || name.contains("seeds")) return 500;
        if (item == Items.MILK_BUCKET || item == Hcs.CACTUS_JUICE) return 2.5F;
        if (item == Items.MELON_SLICE) return 2;
        if (name.contains("_slice")) return 3;
        if (item == Hcs.ANIMAL_VISCERA || item.getDefaultInstance().is(ItemTags.FISHES)) return 4;
        if (item == Items.APPLE || item == Items.SUGAR_CANE || item == Items.POISONOUS_POTATO || name.contains("melon"))
            return 20;
        if (item == Items.CARROT || item == Items.BEETROOT) return 30;
        if (item == Items.EGG || item == Items.TURTLE_EGG || item == Hcs.CACTUS_FLESH || item == Hcs.ORANGE || item == Items.POTATO || item == Hcs.GINGER)
            return 45;
        if (name.contains("jerky") || item == Items.COOKIE || item == Items.DRIED_KELP || item == Items.FERMENTED_SPIDER_EYE)
            return 60;
        if (item == Items.HONEYCOMB) return 180;
        if (packageType == 2 || item == Items.HONEY_BOTTLE || item == Items.SUGAR)
            return 1000;
        if (packageType == 1 || name.contains("cooked_") || name.contains("baked_") || name.contains("roasted_") || name.contains("steamed_") || name.contains("fried_"))
            return 5;
        if (item == Items.COCOA_BEANS || item == Items.NETHER_WART || name.contains("pumpkin") || item == Hcs.SELAGINELLA)
            return 80;
        if (!item.isEdible() || item.getFoodProperties() == null) return 7;
        if (item.getFoodProperties().isMeat() || item == Items.RABBIT_FOOT || item == Hcs.BAT_WINGS) return 5;
        return 7;
    }

    public static long createExp(Level level, ItemStack stack) {
        return createExp(level, stack, false);
    }

    public static long createExp(Level level, ItemStack stack, boolean isInIcebox) {
        return createExp(level, stack, 1.0F, isInIcebox);
    }

    public static long createExp(Level level, ItemStack stack, float fresh, boolean isInIcebox) {
        if (level == null) {
            Hcs.error("RotHelper/createExp;level==null");
            return setExp(stack, 0, isInIcebox);
        }
        return setExp(stack, level.getGameTime() + (long) (getExpDate(stack.getItem()) * 24000 * fresh * (isInIcebox ? 3 : 1)), isInIcebox);
    }

    @Deprecated
    public static long getExp(@NotNull ItemStack stack) {
        if (stack.getOrCreateTag().contains(HFI)) return stack.getOrCreateTag().getLong(HFI);
        if (stack.getOrCreateTag().contains(HFE)) return stack.getOrCreateTag().getLong(HFE);
        if (WorldHelper.cannotGetServerWorld()) {
            Hcs.error("getExp client called");
            return 0;
        }
        return createExp(WorldHelper.getServerWorld(), stack);
    }

    public static long getExp(@NotNull ItemStack stack, boolean isInIcebox) {
        if (stack.getOrCreateTag().contains(isInIcebox ? HFI : HFE))
            return stack.getOrCreateTag().getLong(isInIcebox ? HFI : HFE);
        return Long.MAX_VALUE;
    }

    @Deprecated
    public static long setExp(@NotNull ItemStack stack, long expirationTime) {
        return setExp(stack, expirationTime, false);
    }

    public static long setExp(@NotNull ItemStack stack, long expirationTime, boolean isInIcebox) {
        stack.getOrCreateTag().putLong(isInIcebox ? HFI : HFE, expirationTime);
        return expirationTime;
    }

    public static float getFresh(Level level, ItemStack stack) {
        return getFresh(level, stack, stack.getOrCreateTag().contains(HFI));
    }

    public static float getFresh(Level level, ItemStack stack, boolean isInIcebox) {
        if (level == null) return 1.0F;
        float fresh = (float) (getExp(stack, isInIcebox) - level.getGameTime()) / (getExpDate(stack.getItem()) * 24000.0F * (isInIcebox ? 3.0F : 1.0F));
        if (fresh > 1.0F) fresh = 1.0F;
        else if (fresh < 0.0F) fresh = 0.0F;
        return fresh;
    }

    public static int getFreshLevel(float fresh) {
        if (fresh > 0.7F) return 3; // Fresh
        if (fresh > 0.35F) return 2; // Stale
        if (fresh > 0.0F) return 1; // Spoiled
        return 0; // Rotten
    }

    public static float getFreshCooked(float prevFresh) {
        if (prevFresh <= 0.0F) return 0.0F; //Cooked rotten food is still rotten
        return Math.min(1.0F, 0.4F + prevFresh / 1.5F);
    }

    public static void setFresh(Level level, ItemStack stack, float fresh) {
        setFresh(level, stack, fresh, false);
    }

    public static void setFresh(Level level, ItemStack stack, float fresh, boolean isInIcebox) {
        if (level == null) return;
        if (fresh > 1.0F) fresh = 1.0F;
        else if (fresh < 0.0F) fresh = 0.0F;
        setExp(stack, (long) (fresh * getExpDate(stack.getItem()) * 24000.0F + level.getGameTime()), isInIcebox);
    }

    public static void update(Level level, Container inv) {
        if (!(inv instanceof Inventory)) // Prevent duplicate call
            CombustionHelper.inventoryTick(false, inv, null);
        update(level, inv, false);
    }

    public static void update(Level level, Container inv, boolean isInIcebox) {
        if (level == null || inv == null) {
            Hcs.error("RotHelper/tick();level==null||inv==null");
            return;
        }
        WorldHelper.trySetServerWorld(level);
        if (WorldHelper.cannotGetServerWorld()) return;
        if (level instanceof ServerLevel) {
//        level = WorldHelper.getServerWorld();
            for (int i = 0; i < inv.getContainerSize(); ++i) {
                ItemStack stack = inv.getItem(i);
                if (stack == null) continue;
                Item item = stack.getItem();
                CompoundTag nbt = stack.getOrCreateTag();
                if (nbt.contains(DryingRackBlockEntity.DRYING_DEADLINE) && !isInIcebox)
                    nbt.remove(DryingRackBlockEntity.DRYING_DEADLINE);
                if (stack.isEmpty() || !canRot(item)) {
                    if (nbt.contains(HFE)) nbt.remove(HFE);
                    continue;
                }
                if (nbt.contains(HFF)) {
                    createExp(level, stack, nbt.getFloat(HFF), isInIcebox);
                    nbt.remove(HFF);
                }
                if (nbt.contains(isInIcebox ? HFI : HFE)) {
                    if (getExp(stack, isInIcebox) <= level.getGameTime() && getPackageType(item) != 1) {
                        inv.setItem(i, new ItemStack(Hcs.ROT, stack.getCount()));
                    }
                } else createExp(level, stack, isInIcebox);
                if (nbt.contains(isInIcebox ? HFE : HFI)) {
                    createExp(level, stack, getFresh(level, stack, !isInIcebox), isInIcebox);
                    nbt.remove(isInIcebox ? HFE : HFI);
                }
            }
        }
    }

    public static void onLeaveGame(Level level, Container inv) {
        // prevent left players' inventories from rotting
        if (inv == null) {
            Hcs.error("RotHelper/onLeaveGame;inv==null");
            return;
        }
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            Item item = stack.getItem();
            CompoundTag nbt = stack.getOrCreateTag();
            if (!canRot(item) || nbt.contains(HFF)) continue;
            if (nbt.contains(HFI)) update(level, inv, false);
            if (nbt.contains(HFE)) {
                nbt.putFloat(HFF, getFresh(level, stack, false));
                nbt.remove(HFE);
            }
        }
    }

    public static MutableComponent getModifierText(Level level, @NotNull ItemStack stack) {
        boolean isInIcebox = stack.getOrCreateTag().contains(HFI);
        if (level == null || (!stack.getOrCreateTag().contains(HFE) && !isInIcebox))
            return Component.translatable("hcs.food_info.fresh").withStyle(ChatFormatting.DARK_GREEN);
        float fresh = getFresh(level, stack, isInIcebox);
        int freshLv = getFreshLevel(fresh);
        MutableComponent modifier = Component.empty();
        if (freshLv >= 3)
            modifier.append(Component.translatable("hcs.food_info.fresh").withStyle(ChatFormatting.DARK_GREEN));
        else if (freshLv == 2) modifier.append(Component.translatable("hcs.food_info.stale").withStyle(ChatFormatting.YELLOW));
        else if (freshLv == 1) modifier.append(Component.translatable("hcs.food_info.spoiled").withStyle(ChatFormatting.RED));
        else {
            modifier.append(Component.translatable("hcs.food_info.rotten").withStyle(ChatFormatting.DARK_RED));
            if (getPackageType(stack.getItem()) == 1)
                modifier.append(Component.translatable("hcs.food_info.pour").withStyle(ChatFormatting.GRAY));
            return modifier;
        }
//        modifier.append(Component.translatable("hcs.food_info.expiry", (int) Math.ceil(Math.max(getExpDate(stack.getItem()) * fresh * (isInIcebox ? 3 : 1), 0.1F))).withStyle(ChatFormatting.GRAY));
        return modifier;
    }

    public static void appendInfo(Level level, ItemStack stack, List<Component> tooltip) {
        if (level == null) return;
        tooltip.add(getModifierText(level, stack));
    }

    public static int addDebuff(Level level, Player player, ItemStack stack) {
        int freshLevel = 3;
        if (player == null || stack == null) {
            Hcs.error("RotHelper/addDebuff;player==null||stack==null");
            return freshLevel;
        }
        if (level.isClientSide()) {
            Hcs.warn("RotHelper/addDebuff;level is client");
            return freshLevel;
        }
        FoodData foodData = player.getFoodData();
        SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
        Item item = stack.getItem();
        FoodProperties food = item.getFoodProperties();
        freshLevel = getFreshLevel(getFresh(level, stack));
        boolean isRot = item == Hcs.ROT;
        if (canRot(item) || isRot) {
            switch (isRot ? 0 : freshLevel) {
                case 0 -> {
                    foodData.setSaturation(0);
                    sanityManager.add(-0.1);
                    if (food != null) foodData.setFoodLevel(foodData.getFoodLevel() - food.getNutrition() + 1);
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200));
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
                    player.addEffect(new MobEffectInstance(HcsEffects.FOOD_POISONING, 1200));
                    player.addEffect(new MobEffectInstance(HcsEffects.DIARRHEA, 600));
                }
                case 1 -> {
                    sanityManager.add(-0.07);
                    if (food != null)
                        foodData.setFoodLevel(foodData.getFoodLevel() - (int) (Math.min(food.getNutrition() - 1, food.getNutrition() * 0.7)));
                    player.addEffect(new MobEffectInstance(HcsEffects.FOOD_POISONING, 600));
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200));
                    player.addEffect(new MobEffectInstance(HcsEffects.DIARRHEA, 600));
                }
                case 2 -> {
                    if (food != null)
                        foodData.setFoodLevel(foodData.getFoodLevel() - (int) (Math.min(food.getNutrition() - 1, food.getNutrition() * 0.3)));
                }
            }
        }
        return freshLevel;
    }

    public static boolean isMeat(ItemStack stack) {
        if (stack == null) return false;
        return CommUtil.applyNullable(stack.getItem().getFoodProperties(), FoodProperties::isMeat, false);
    }

}