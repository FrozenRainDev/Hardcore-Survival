package biz.coolpage.hcs.mixin.entity.player;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.block.torches.BurningCrudeTorchBlock;
import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.item.KnifeItem;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.IDamageSources;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.*;
import biz.coolpage.hcs.status.manager.disabled.*;
import biz.coolpage.hcs.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static biz.coolpage.hcs.config.Configs.*;
import static biz.coolpage.hcs.recipe.DryingRackRecipe.HAS_COOKED;
import static biz.coolpage.hcs.util.CommUtil.rehabPlayerStats;
import static biz.coolpage.hcs.util.DigRestrictHelper.Predicates.IS_PLANT;
import static biz.coolpage.hcs.util.EntityHelper.*;

@Mixin(Player.class)
@SuppressWarnings({"CanBeFinal", "AddedMixinMembersNamePattern"})
// PlayerEntityMixin
public abstract class PlayerMixin extends LivingEntity implements StatAccessor {
    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract Inventory getInventory();

    @Shadow
    public abstract FoodData getFoodData();

    @Shadow
    public abstract boolean isInvulnerableTo(DamageSource damageSource);

    @Shadow
    public abstract Component getName();

    @Shadow
    public abstract String getScoreboardName();

    @Shadow
    public abstract boolean hurt(DamageSource source, float amount);

    @Shadow
    public abstract boolean isSpectator();

    @Shadow
    public abstract void awardStat(ResourceLocation stat, int amount);

    @Shadow
    public abstract void causeFoodExhaustion(float exhaustion);

    @Shadow
    public abstract float getSpeed();

    @Shadow
    public abstract boolean isSwimming();

    @Shadow
    public int experienceLevel;

    @Shadow
    protected FoodData foodData;

    @Shadow
    protected boolean wasUnderwater; // 修正自 wasInWater

    @Shadow
    public abstract void setRemainingFireTicks(int fireTicks);

    @Shadow
    public abstract Iterable<ItemStack> getArmorSlots();

    @Unique
    protected StatusManager statusManager = new StatusManager();
    @Unique
    protected ConfigManager configManager = new ConfigManager();

    @Unique
    protected ThirstManager thirstManager = new ThirstManager(), disabledThirstManager = new DisabledThirstManager();
    @Unique
    protected StaminaManager staminaManager = new StaminaManager(), disabledStaminaManager = new DisabledStaminaManager();
    @Unique
    protected TemperatureManager temperatureManager = new TemperatureManager(), disabledTemperatureManager = new DisabledTemperatureManager();
    @Unique
    protected SanityManager sanityManager = new SanityManager(), disabledSanityManager = new DisabledSanityManager();
    @Unique
    protected NutritionManager nutritionManager = new NutritionManager(), disabledNutritionManager = new DisabledNutritionManager();
    @Unique
    protected WetnessManager wetnessManager = new WetnessManager(), disabledWetnessManager = new DisabledWetnessManager();
    @Unique
    protected InjuryManager injuryManager = new InjuryManager(), disabledInjuryManager = new DisabledInjuryManager();
    @Unique
    protected MoodManager moodManager = new MoodManager(), disabledMoodManager = new DisabledMoodManager();
    @Unique
    protected DiseaseManager diseaseManager = new DiseaseManager(), disabledDiseaseManger = new DisabledDiseaseManager();
    @Unique
    protected OxygenManager oxygenManager = new OxygenManager(), disabledOxygenManager = new DisabledOxygenManager();

    @Unique
    private static void quitReturnTeleport(@Nullable Entity entity) {
        if (toPlayer(entity) instanceof ServerPlayer player && player.hasEffect(HcsEffects.RETURN.get())) {
            StatusManager statusManager1 = ((StatAccessor) player).getStatusManager();
            if (statusManager1.getReturnEffectAwaitTicks() > 0) EntityHelper.msgById(player, "hcs.tip.return_failed");
            statusManager1.setReturnEffectAwaitTicks(0);
        }
    }

    @Unique
    @Override
    public StatusManager getStatusManager() {
        return this.statusManager;
    }

    @Unique
    @Override
    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    @Unique
    @Override
    public ThirstManager getThirstManager() {
        if (configManager.get(THIRST)) return this.thirstManager;
        return this.disabledThirstManager;
    }

    @Unique
    @Override
    public StaminaManager getStaminaManager() {
        if (configManager.get(STAMINA)) return this.staminaManager;
        return this.disabledStaminaManager;
    }

    @Unique
    @Override
    public TemperatureManager getTemperatureManager() {
        if (configManager.get(TEMPERATURE)) return this.temperatureManager;
        return this.disabledTemperatureManager;
    }

    @Unique
    @Override
    public SanityManager getSanityManager() {
        if (configManager.get(SANITY)) return this.sanityManager;
        return this.disabledSanityManager;
    }

    @Unique
    @Override
    public NutritionManager getNutritionManager() {
        if (configManager.get(NUTRITION)) return this.nutritionManager;
        return this.disabledNutritionManager;
    }

    @Unique
    @Override
    public WetnessManager getWetnessManager() {
        if (configManager.get(WET)) return this.wetnessManager;
        return this.disabledWetnessManager;
    }

    @Unique
    @Override
    public InjuryManager getInjuryManager() {
        if (configManager.get(INJURY)) return this.injuryManager;
        return this.disabledInjuryManager;
    }

    @Unique
    @Override
    public MoodManager getMoodManager() {
        if (configManager.get(MOOD)) return this.moodManager;
        return this.disabledMoodManager;
    }

    @Unique
    @Override
    public DiseaseManager getDiseaseManager() {
        if (configManager.get(DISEASE)) return this.diseaseManager;
        return this.disabledDiseaseManger;
    }

    @Unique
    @Override
    public OxygenManager getOxygenManager() {
        if (configManager.get(OXYGEN)) return this.oxygenManager;
        return this.disabledOxygenManager;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(@NotNull CompoundTag nbt, CallbackInfo info) {
        if (this.level().isClientSide) return;
        this.thirstManager.set(nbt.contains(ThirstManager.THIRST_NBT, Tag.TAG_DOUBLE) ? nbt.getDouble(ThirstManager.THIRST_NBT) : 1.0);
        this.thirstManager.setSaturation(nbt.contains(ThirstManager.THIRST_SATURATION_NBT, Tag.TAG_FLOAT) ? nbt.getFloat(ThirstManager.THIRST_SATURATION_NBT) : 0.2F);
        this.staminaManager.set(nbt.contains(StaminaManager.STAMINA_NBT, Tag.TAG_DOUBLE) ? nbt.getDouble(StaminaManager.STAMINA_NBT) : 1.0);
        this.temperatureManager.set(nbt.contains(TemperatureManager.TEMPERATURE_NBT, Tag.TAG_DOUBLE) ? nbt.getDouble(TemperatureManager.TEMPERATURE_NBT) : 0.5);
        this.temperatureManager.setSaturation(nbt.contains(TemperatureManager.TEMPERATURE_SATURATION_NBT, Tag.TAG_FLOAT) ? nbt.getFloat(TemperatureManager.TEMPERATURE_SATURATION_NBT) : 0.0F);
        this.statusManager.setMaxExpLevelReached(nbt.contains(StatusManager.MAX_LVL_NBT, Tag.TAG_INT) ? nbt.getInt(StatusManager.MAX_LVL_NBT) : 0);
        this.sanityManager.set(nbt.contains(SanityManager.SANITY_NBT, Tag.TAG_DOUBLE) ? nbt.getDouble(SanityManager.SANITY_NBT) : 1.0);
        this.nutritionManager.setVegetable(nbt.contains(NutritionManager.NUTRITION_VEGETABLE_NBT, Tag.TAG_DOUBLE) ? nbt.getDouble(NutritionManager.NUTRITION_VEGETABLE_NBT) : 1.0);
        this.wetnessManager.set(nbt.contains(WetnessManager.WETNESS_NBT, Tag.TAG_DOUBLE) ? nbt.getDouble(WetnessManager.WETNESS_NBT) : 0.0);
        this.statusManager.setSoulImpairedStat(nbt.contains(StatusManager.IS_SOUL_IMPAIRED_NBT) ? nbt.getInt(StatusManager.IS_SOUL_IMPAIRED_NBT) : 0);
        this.statusManager.setEnterCurrWldTimes(nbt.contains(StatusManager.ENTER_TIMES_NBT) ? nbt.getInt(StatusManager.ENTER_TIMES_NBT) : 0);
        this.injuryManager.setRawPain(nbt.contains(InjuryManager.PAIN_NBT) ? nbt.getDouble(InjuryManager.PAIN_NBT) : 0.0);
        this.injuryManager.setPainkillerApplied(nbt.contains(InjuryManager.PAINKILLER_APPLIED_NBT) ? nbt.getInt(InjuryManager.PAINKILLER_APPLIED_NBT) : 0);
        this.injuryManager.setBleeding(nbt.contains(InjuryManager.BLEEDING_NBT) ? nbt.getDouble(InjuryManager.BLEEDING_NBT) : 0.0);
        this.injuryManager.setFracture(nbt.contains(InjuryManager.FRACTURE_NBT) ? nbt.getDouble(InjuryManager.FRACTURE_NBT) : 0.0);
        this.statusManager.setInDarknessTicks(nbt.contains(StatusManager.IN_DARKNESS_TICKS_NBT) ? nbt.getInt(StatusManager.IN_DARKNESS_TICKS_NBT) : 0);
        this.moodManager.setPanic(nbt.contains(MoodManager.PANIC_NBT) ? nbt.getDouble(MoodManager.PANIC_NBT) : 0.0);
        this.moodManager.setPanicKillerApplied(nbt.contains(MoodManager.PANIC_KILLER_APPLIED_NBT) ? nbt.getInt(MoodManager.PANIC_KILLER_APPLIED_NBT) : 0);
        this.moodManager.setHappiness(nbt.contains(MoodManager.HAPPINESS_NBT) ? nbt.getDouble(MoodManager.HAPPINESS_NBT) : 1.0);
        this.diseaseManager.setParasite(nbt.contains(DiseaseManager.PARASITE_NBT) ? nbt.getDouble(DiseaseManager.PARASITE_NBT) : 0.0);
        this.diseaseManager.setCold(nbt.contains(DiseaseManager.COLD_NBT) ? nbt.getDouble(DiseaseManager.COLD_NBT) : 0.0);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addAdditionalSaveData(@NotNull CompoundTag nbt, CallbackInfo info) {
        if (this.level().isClientSide) return;
        nbt.putDouble(ThirstManager.THIRST_NBT, this.thirstManager.get());
        nbt.putFloat(ThirstManager.THIRST_SATURATION_NBT, this.thirstManager.getSaturation());
        nbt.putDouble(StaminaManager.STAMINA_NBT, this.staminaManager.get());
        nbt.putDouble(TemperatureManager.TEMPERATURE_NBT, this.temperatureManager.get());
        nbt.putFloat(TemperatureManager.TEMPERATURE_SATURATION_NBT, this.temperatureManager.getSaturation());
        nbt.putInt(StatusManager.MAX_LVL_NBT, this.statusManager.getMaxExpLevelReached());
        nbt.putDouble(SanityManager.SANITY_NBT, this.sanityManager.get());
        nbt.putDouble(NutritionManager.NUTRITION_VEGETABLE_NBT, this.nutritionManager.getVegetable());
        nbt.putDouble(WetnessManager.WETNESS_NBT, this.wetnessManager.get());
        nbt.putInt(StatusManager.IS_SOUL_IMPAIRED_NBT, this.statusManager.getSoulImpairedStat());
        nbt.putDouble(InjuryManager.PAIN_NBT, this.injuryManager.getRawPain());
        nbt.putInt(InjuryManager.PAINKILLER_APPLIED_NBT, this.injuryManager.getPainkillerApplied());
        nbt.putDouble(InjuryManager.BLEEDING_NBT, this.injuryManager.getBleeding());
        nbt.putInt(StatusManager.IN_DARKNESS_TICKS_NBT, this.statusManager.getInDarknessTicks());
        nbt.putDouble(MoodManager.PANIC_NBT, this.moodManager.getRawPanic());
        nbt.putInt(MoodManager.PANIC_KILLER_APPLIED_NBT, this.moodManager.getPanicKillerApplied());
        nbt.putDouble(InjuryManager.FRACTURE_NBT, this.injuryManager.getFracture());
        nbt.putDouble(DiseaseManager.PARASITE_NBT, this.diseaseManager.getParasite());
        nbt.putDouble(DiseaseManager.COLD_NBT, this.diseaseManager.getCold());
        nbt.putDouble(MoodManager.HAPPINESS_NBT, this.moodManager.getHappiness());
        nbt.putInt(StatusManager.ENTER_TIMES_NBT, this.statusManager.getEnterCurrWldTimes() + 1);
    }

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    public void getDestroySpeed(@NotNull BlockState state, @NotNull CallbackInfoReturnable<Float> cir) {
        this.statusManager.setRecentMiningTicks(200);
        this.staminaManager.add(-0.00035, this);
        this.staminaManager.pauseRestoring();
        float speed = MobEffectUtil.hasDigSpeed(this) ? cir.getReturnValue() : cir.getReturnValue() / 3.0F;
        Item mainHand = this.getMainHandItem().getItem();
        final boolean isShovelMineable = state.is(BlockTags.MINEABLE_WITH_SHOVEL);
        final boolean isKnife = mainHand instanceof KnifeItem, isSword = mainHand instanceof SwordItem, isAxe = mainHand instanceof AxeItem;
        final boolean digRestrict = this.configManager.get(DIG_CONSTRAIN);
        Block block = state.getBlock();

        if (digRestrict) {
            if (!DigRestrictHelper.canBreakExceptShovel(mainHand, state)) {
                if (isShovelMineable) speed /= 30.0F;
                else speed = -1.0F;
            }
            if (isShovelMineable && mainHand instanceof ShovelItem && mainHand != Hcs.FLINT_CONE.get()) speed /= 2.0F;
            else if (state.is(BlockTags.MINEABLE_WITH_AXE) || isSword) {
                if (mainHand == Hcs.FLINT_HATCHET.get()) speed *= 6.0F;
                else speed /= 2.5F;
            }
            if ((mainHand instanceof HoeItem || isKnife) && (block instanceof CropBlock || block instanceof StemBlock || state.canBeReplaced()))
                speed *= 2.0F;
            else if (isKnife) {
                if (IS_PLANT.test(block) || block instanceof WebBlock) speed *= 3.0F;
                else speed /= 10.0F;
            }
            if (this.hasEffect(HcsEffects.DEHYDRATED.get())) speed /= 2.0F;
            if (this.hasEffect(HcsEffects.STARVING.get())) speed /= 2.0F;
            if (this.hasEffect(HcsEffects.EXHAUSTED.get())) speed /= 2.0F;
            if (this.hasEffect(HcsEffects.UNHAPPY.get())) speed /= 1.2F;
            if (EntityHelper.getEffectAmplifier(this, HcsEffects.PARASITE_INFECTION.get()) > 0) speed /= 1.5F;
            speed /= (float) Math.max(1.0, Math.pow(1.15, (EntityHelper.getEffectAmplifier(this, HcsEffects.PAIN.get()) + 1) + (EntityHelper.getEffectAmplifier(this, HcsEffects.INJURY.get()) + 1)));
            if (DigRestrictHelper.Predicates.IS_BREAKABLE_FUNCTIONAL.test(block))
                speed *= (block instanceof AbstractFurnaceBlock || block == Blocks.ENDER_CHEST) ? 16.0F : 4.0F;
            else if (block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN) speed *= 3.0F;
            else if ((block == Blocks.CLAY && !isShovelMineable)) speed /= 9.0F;
            else if (block instanceof LeavesBlock && !isSword && !isAxe) speed /= 10.0F;
        }
        boolean shouldBreakInstantly = block instanceof TorchBlock || block instanceof BurningCrudeTorchBlock || (state.is(BlockTags.FLOWERS) && !(block instanceof LeavesBlock));
        boolean shouldVanillaBreakInstantly = Float.compare(block.getExplosionResistance(), 0.060114F) == 0;
        if (shouldBreakInstantly || (!digRestrict && shouldVanillaBreakInstantly))
            speed = 9999999F;
        if (digRestrict || shouldVanillaBreakInstantly) cir.setReturnValue(speed);
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!level().isClientSide) this.staminaManager.pauseRestoring();
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "eat", at = @At("HEAD"))
    public void eat(Level world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if ((Object) this instanceof ServerPlayer player && !stack.isEmpty()) {
            Item item = stack.getItem();
            String name = item.getDescriptionId();
            FoodProperties food = item.getFoodProperties();
            EntityHelper.checkOvereaten(player, false);
            if (food != null) {
                DiseaseManager.updateRawFoodDetriment(item, this);
                if (food.isMeat() || name.contains("egg")) this.nutritionManager.addVegetable(-0.1);
                else if (name.contains("kelp") || name.contains("sugar_cane")) this.nutritionManager.addVegetable(0.19);
                else if (name.contains("berries") || name.contains("berry")) this.nutritionManager.addVegetable(0.21);
                else if (name.contains("apple") || name.contains("orange") || name.contains("carrot") || name.contains("cactus") || name.contains("melon") || name.contains("potherb") || name.contains("shoot") || name.contains("salad") || name.contains("fruit"))
                    this.nutritionManager.addVegetable(0.35);
                if (name.contains("ginger")) this.diseaseManager.setCold(-0.2);
                int freshLevel = RotHelper.addDebuff(world, player, stack);
                if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) {
                    rehabPlayerStats(this);
                } else if (item == Items.KELP || Hcs.IS_BARK.test(item)) {
                    if (item == Hcs.WILLOW_BARK.get()) this.injuryManager.applyPainkiller();
                    this.sanityManager.add(-0.02);
                } else if (item == Hcs.FEARLESSNESS_HERB.get()) this.moodManager.applyPanicKiller();
                else if (item == Items.POISONOUS_POTATO || item == Items.SPIDER_EYE || item == Items.CHORUS_FRUIT)
                    this.sanityManager.add(-0.07);
                else if (item == Items.ROTTEN_FLESH) this.sanityManager.add(-0.1);
                else if (item == Items.RED_MUSHROOM || item == Items.CRIMSON_FUNGUS || item == Items.WARPED_FUNGUS)
                    this.sanityManager.add(-1.0);
                else if (freshLevel > 2) {
                    if (item == Items.PUMPKIN_PIE || item == Items.RABBIT_STEW || item == Items.GOLDEN_CARROT || item == Items.GLISTERING_MELON_SLICE)
                        this.sanityManager.add(0.15);
                    else if (item == Items.MUSHROOM_STEW || item == Hcs.COOKED_CACTUS_FLESH.get() || item == Items.BEETROOT_SOUP)
                        this.sanityManager.add(0.07);
                    else if (item == Items.DRIED_KELP) this.sanityManager.add(0.05);
                    else if (item == Items.COOKIE || item == Items.APPLE || item == Hcs.ORANGE.get() || item == Items.SUGAR)
                        this.sanityManager.add(0.025);
                    else if ((HAS_COOKED.test(name) && item != Hcs.ROASTED_WORM.get()) || item == Items.BREAD)
                        this.sanityManager.add(food.isFastFood() ? 0.005 : 0.01);
                }
                if (item == Items.WHEAT || item == Items.SUGAR || item == Items.SUGAR_CANE || item == Hcs.POTHERB.get()) {
                    final int increasedFoodLevel = this.foodData.getFoodLevel() + 1;
                    if (increasedFoodLevel > 20) this.foodData.setExhaustion(0.0F);
                    else this.foodData.setFoodLevel(increasedFoodLevel);
                } else if (((name.contains("seeds") || Hcs.IS_BARK.test(item)) && food.getNutrition() == 0) || item == Hcs.COOKED_SWEET_BERRIES.get() || item == Hcs.ROT.get() || item == Items.KELP || item == Hcs.PETALS_SALAD.get()) {
                    EntityHelper.addDecimalFoodLevel(player, 0.4F, false);
                    if (item == Items.PUMPKIN_SEEDS) this.diseaseManager.addParasite(-1.0);
                }
                if (!name.contains("dried") && !name.contains("jerky") && !name.contains("seeds") && item != Items.COOKIE && item != Items.BREAD && item != Items.SUGAR) {
                    if (name.contains("stew") || name.contains("soup"))
                        this.thirstManager.add(item == Items.MUSHROOM_STEW ? 0.06 : 0.2);
                    else if (item == Items.MELON_SLICE || item == Items.APPLE || item == Hcs.ORANGE.get() || item == Items.SUGAR_CANE || item == Hcs.CACTUS_FLESH.get())
                        this.thirstManager.addDirectly(0.05);
                    else if (item == Items.CARROT || item == Items.POTATO || item == Hcs.PUMPKIN_SLICE.get() || item == Hcs.PETALS_SALAD.get())
                        this.thirstManager.addDirectly(0.03);
                    else if (food.isMeat() || name.contains("berries") || item == Hcs.COOKED_CACTUS_FLESH.get() || item == Hcs.COOKED_PUMPKIN_SLICE.get() || item == Hcs.COOKED_CARROT.get() || item == Hcs.COOKED_BAMBOO_SHOOT.get() || item == Hcs.COOKED_SWEET_BERRIES.get())
                        this.thirstManager.addDirectly(0.02);
                    else this.thirstManager.addDirectly(0.01);
                }
            }
        }
    }

    @Inject(method = "causeFoodExhaustion", at = @At("TAIL"))
    public void causeFoodExhaustion(float exhaustion, CallbackInfo ci) {
        if (!this.level().isClientSide) {
            this.thirstManager.add(-exhaustion / 60.0);
            if (this.hasEffect(HcsEffects.MALNUTRITION.get())) this.foodData.addExhaustion(exhaustion * 0.5F);
        }
    }

    @Inject(method = "checkMovementStatistics", at = @At("HEAD"), cancellable = true)
    public void checkMovementStatistics(double dx, double dy, double dz, CallbackInfo ci) {
        if (this.onGround() && !this.isPassenger()) {
            int i = Math.round((float) Math.sqrt(dx * dx + dz * dz) * 100.0f);
            if (i > 0) {
                if (this.isSprinting()) {
                    this.awardStat(Stats.SPRINT_ONE_CM, i);
                    this.causeFoodExhaustion(0.01f * (float) i * 0.01f);
                } else if (this.isCrouching()) {
                    this.awardStat(Stats.CROUCH_ONE_CM, i);
                    this.causeFoodExhaustion(0.007f * (float) i * 0.01f);
                } else {
                    this.awardStat(Stats.WALK_ONE_CM, i);
                    this.causeFoodExhaustion(0.007f * (float) i * 0.01f);
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    public void jump1(@NotNull CallbackInfo ci) {
        if (this.hasEffect(HcsEffects.EXHAUSTED.get())) {
            MobEffectInstance exhaustedEffectInstance = this.getEffect(HcsEffects.EXHAUSTED.get());
            if (exhaustedEffectInstance != null && exhaustedEffectInstance.getAmplifier() > 0) ci.cancel();
        }
        if (this.hasEffect(HcsEffects.INJURY.get())) {
            MobEffectInstance exhaustedEffectInstance = this.getEffect(HcsEffects.INJURY.get());
            if (exhaustedEffectInstance != null && exhaustedEffectInstance.getAmplifier() > 2) ci.cancel();
        }
    }

    @Inject(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/resources/ResourceLocation;)V", shift = At.Shift.AFTER), cancellable = true)
    public void jump2(@NotNull CallbackInfo ci) {
        double currRealPain = this.injuryManager.getRealPain();
        float rate = (this.isSprinting() ? 3.0F : 1.0F) * (currRealPain > 2.0 ? (float) (currRealPain * 1.5) : 1.0F) * (this.hasEffect(HcsEffects.FRACTURE.get()) ? 1.5F : 1.0F);
        this.staminaManager.pauseRestoring();
        this.causeFoodExhaustion(0.035F * rate);
        this.staminaManager.pauseRestoring(40);
        this.staminaManager.add(-0.0025F * rate, this);
        ci.cancel();
    }

    @Inject(method = "attack", at = @At("HEAD"))
    public void attack(Entity target, CallbackInfo ci) {
        this.staminaManager.pauseRestoring(50);
        this.staminaManager.add(-0.025, this);
        this.statusManager.setRecentAttackTicks(200);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        @Nullable Player player = toPlayer(this);
        if (!IS_SURVIVAL_AND_SERVER.test(player)) return;
        ArmorHelper.getFinalProtection(player);
        final double currPain = ((StatAccessor) this).getInjuryManager().getRealPain();
        boolean outOfDarkness = true, hasDarkDebuff = false;
        if (currPain > 2.0 && this.hasEffect(HcsEffects.PAIN.get()) && this.level().getGameTime() % Math.max(1, 30 * (6 - (int) currPain)) == 0)
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, (float) (currPain / 14), level().random.nextFloat() * 0.1f + 0.9f);
        int oxyLackLvl = 0;
        final double y = this.getY();
        final boolean isRelaxingMode = HcsDifficulty.isOf(this.level(), HcsDifficulty.HcsDifficultyEnum.relaxing);
        final boolean isDarkSafe = isRelaxingMode || EntityHelper.isLuminousBlockWorking(this) || this.hasEffect(MobEffects.NIGHT_VISION);
        this.causeFoodExhaustion(0.001F);
        this.statusManager.setRecentAttackTicks(Math.max(0, this.statusManager.getRecentAttackTicks() - 1));
        this.statusManager.setRecentMiningTicks(Math.max(0, this.statusManager.getRecentMiningTicks() - 1));
        this.statusManager.setRecentHasColdWaterBagTicks(Math.max(0, this.statusManager.getRecentHasColdWaterBagTicks() - 1));
        this.statusManager.setRecentHasHotWaterBagTicks(Math.max(0, this.statusManager.getRecentHasHotWaterBagTicks() - 1));
        this.statusManager.setRecentLittleOvereatenTicks(this.foodData.getFoodLevel() < 20 ? 0 : Math.max(0, this.statusManager.getRecentLittleOvereatenTicks() - 1));
        this.statusManager.setRecentSleepTicks(Math.max(0, this.statusManager.getRecentSleepTicks() - 1));
        this.statusManager.setRecentHurtTicks(this.statusManager.getRecentHurtTicks() - 1);
        if (this.statusManager.getBandageWorkTicks() > 0) statusManager.addBandageWorkTicks(-1);
        if (this.isInWaterRainOrBubble()) this.statusManager.setRecentWetTicks(20);
        else this.statusManager.setRecentWetTicks(Math.max(0, this.statusManager.getRecentWetTicks() - 1));
        if (this.statusManager.getRecentMiningTicks() < 20) this.statusManager.setBareDiggingTicks(0);
        if (this.experienceLevel > this.statusManager.getMaxExpLevelReached())
            this.statusManager.setMaxExpLevelReached(this.experienceLevel);

        if (this.position().distanceTo(this.staminaManager.getLastVecPos()) > 0.0001) {
            quitReturnTeleport(this);
            if (!this.isPassenger()) {
                boolean shouldPauseRestoring = true;
                if (this.onGround()) {
                    if (this.isSprinting()) this.staminaManager.add(-0.0006, this);
                    else if (this.isCrouching()) this.staminaManager.add(-0.0001, this);
                    else if (this.staminaManager.get() < 0.7) {
                        shouldPauseRestoring = false;
                        this.staminaManager.add(this.staminaManager.get() < 0.3 ? 0.0001 : 0.001, this);
                    }
                }
                if (this.isSwimming() || this.onClimbable()) this.staminaManager.add(-0.001, this);
                else if (this.isInWater()) this.staminaManager.add(-0.0004, this);
                if (shouldPauseRestoring) this.staminaManager.pauseRestoring();
            }
        } else {
            if (this.getThirstManager().get() > 0.5 && this.getFoodData().getFoodLevel() > 10 && !this.hasEffect(HcsEffects.COLD.get()))
                this.staminaManager.add(0.007, this);
            else if (this.getThirstManager().get() > 0.3 && this.getFoodData().getFoodLevel() > 6)
                this.staminaManager.add(0.003, this);
            else this.staminaManager.add(0.001, this);
        }
        BlockPos headPos = this.blockPosition().above();
        int skyBrightness = this.level().getBrightness(LightLayer.SKY, headPos);
        if (this.level().isDay() && skyBrightness >= 14) {
            if ((this.getMainHandItem().is(ItemTags.FLOWERS) || this.getOffhandItem().is(ItemTags.FLOWERS)))
                this.sanityManager.add(0.000009);
            else for (var item : this.getArmorSlots()) {
                if (item.getItem() == Hcs.GARLAND.get()) {
                    this.sanityManager.add(0.000012);
                    break;
                }
            }
        }
        if (this.hasEffect(MobEffects.WITHER)) this.sanityManager.add(-0.00008);
        else if (this.hasEffect(MobEffects.POISON)) this.sanityManager.add(-0.00003);
        boolean isInCavelike = skyBrightness < 1 && this.level().dimensionType().hasSkyLight();
        boolean isInUnpleasantDimension = !this.level().dimensionType().bedWorks() || this.level().dimension() == Level.NETHER;
        if (((this.level().isNight() || isInCavelike) && !this.hasEffect(MobEffects.NIGHT_VISION)) || isInUnpleasantDimension) {
            double sanDecrement = 0.00001;
            int blockBrightness = this.level().getBrightness(LightLayer.BLOCK, headPos);
            if (!isInUnpleasantDimension && configManager.get(DARK_THREAT)) {
                if (blockBrightness < 1 && isInCavelike && !isDarkSafe) {
                    sanDecrement = 0.00006;
                    hasDarkDebuff = true;
                    final int currDarkTicks = this.statusManager.getInDarknessTicks();
                    if (currDarkTicks == 60) EntityHelper.msgById(this, "hcs.tip.dark.warn");
                    else if (currDarkTicks > 60) {
                        sanDecrement = 0.0002;
                        this.moodManager.setPanic(4.0);
                        if (currDarkTicks == 340) EntityHelper.msgById(this, "hcs.tip.dark.closer");
                        else if (currDarkTicks > 720) {
                            sanDecrement = 0.1;
                            if (currDarkTicks > 800 && currDarkTicks % 10 == 0)
                                this.hurt(((IDamageSources) this.level().damageSources()).darkness(), 2.0F);
                        }
                    }
                    this.statusManager.setInDarknessTicks(currDarkTicks + 1);
                    outOfDarkness = false;
                } else if (blockBrightness < 8) sanDecrement = 0.000008;
            }
            this.sanityManager.add(-sanDecrement * HcsDifficulty.chooseVal(player, 0.5, 1.0, 2.0));
        }
        if (isInCavelike) {
            if (y < 42) oxyLackLvl = 3;
            else if (y < 49) oxyLackLvl = 2;
            else if (y < 56) oxyLackLvl = 1;
        }
        if (outOfDarkness) {
            this.statusManager.setInDarknessTicks(0);
            if (this.statusManager.getLastInDarknessTicks() >= 60) EntityHelper.msgById(this, "hcs.tip.dark.fade");
        }
        if (this.hasEffect(MobEffects.DAMAGE_BOOST)) this.staminaManager.reset();
        this.staminaManager.setLastVecPos(this.position());
        this.sanityManager.updateDifference();
        this.oxygenManager.setOxygenLackLevel(oxyLackLvl);
        if (this.oxygenManager.getFinalOxygenLackLevel() >= 3 && (this.level().getGameTime() % 20 == 0 || this.getAirSupply() < 0))
            this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
        if (this.getAirSupply() < -20 && this.level().getGameTime() % 15 == 0)
            this.hurt(((IDamageSources) this.level().damageSources()).oxygenDeficiency(), 1.0F);

        boolean rainedOn = this.level().isRainingAt(this.blockPosition()), touchingWater = this.isInWater();
        if (this.wasUnderwater) this.wetnessManager.add(0.03);
        else if (touchingWater && this.wetnessManager.get() < 0.7) this.wetnessManager.add(0.01);
        else if (rainedOn) this.wetnessManager.add(0.0004);
        else if (!touchingWater) {
            if (this.getRemainingFireTicks() > 0) {
                this.wetnessManager.add(-0.3);
                if (this.wetnessManager.get() > 0.0) this.setRemainingFireTicks(0);
            } else if (this.temperatureManager.getAmbientCache() > 0.0F)
                this.wetnessManager.add(-Math.abs(this.temperatureManager.getAmbientCache() / 100.0));
            else {
                float rate = this.thirstManager.getThirstRateAffectedByTemp();
                this.wetnessManager.add(-Math.abs(0.00015 * rate * rate));
            }
        }
        this.diseaseManager.tick(!this.hasEffect(HcsEffects.WET.get()) && this.temperatureManager.get() > 0.3);
        if (this.temperatureManager.get() < 0.2) this.diseaseManager.addCold(0.0001);
        int maxSoulImpaired = StatusManager.getMaxSoulImpaired(this);
        if (this.statusManager.getSoulImpairedStat() > maxSoulImpaired)
            this.statusManager.setSoulImpairedStat(maxSoulImpaired);
        this.statusManager.setHasDarknessEnvelopedDebuff(hasDarkDebuff);
        if (this.hasEffect(HcsEffects.RETURN.get()))
            this.statusManager.setReturnEffectAwaitTicks(this.statusManager.getReturnEffectAwaitTicks() + 1);
        else this.statusManager.setReturnEffectAwaitTicks(0);
        if (isRelaxingMode) {
            this.injuryManager.setRawPain(0.0);
            this.injuryManager.setBleeding(0.0);
            this.injuryManager.setFracture(0.0);
        }
        if (rainedOn && this.getXRot() < -60.0F) this.thirstManager.addDirectly(0.0005);
    }

    @Inject(method = "getExperienceReward", at = @At("HEAD"), cancellable = true)
    public void getExperienceReward(CallbackInfoReturnable<Integer> cir) {
        if (!this.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && IS_SURVIVAL_AND_SERVER.test(toPlayer(this))) {
            int xpSum = 0;
            for (int i = this.experienceLevel; i >= 0; --i) {
                if (i >= 31) xpSum += 112 + (i - 31) * 9;
                else if (i >= 16) xpSum += 37 + (i - 16) * 5;
                else if (i >= 1) xpSum += 7 + (i - 1) * 2;
            }
            cir.setReturnValue(xpSum);
        }
    }

    @SuppressWarnings("SameReturnValue")
    @ModifyArg(method = "causeFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/resources/ResourceLocation;I)V"), index = 1)
    public int handleFallDamage(int amount) {
        return 0;
    }

    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    public void actuallyHurt(DamageSource source, float amount, CallbackInfo ci) {
        quitReturnTeleport(this);
        float feelingAmount = amount;
        boolean isBurningDamage = EntityHelper.IS_BURNING_DAMAGE.test(source);
        if (!source.is(DamageTypeTags.BYPASSES_ARMOR))
            feelingAmount = ArmorHelper.getDamageLeftWithReducedArmor(toPlayer(this), amount);

        // 修正为 Shadow 的 Mojang 映射方法
        feelingAmount = this.getDamageAfterMagicAbsorb(source, feelingAmount);

        if (isBurningDamage) feelingAmount *= 2;
        float hurtPercent = feelingAmount / 20.0F;
        this.statusManager.setRecentFeelingDamage(feelingAmount);
        this.statusManager.setRecentHurtTicks(20);
        if (EntityHelper.IS_PHYSICAL_DAMAGE.and(damageSource -> !damageSource.is(DamageTypes.FREEZE)).test(source) && this.getAbsorptionAmount() < 2.0F && !HcsDifficulty.isOf(toPlayer(this), HcsDifficulty.HcsDifficultyEnum.relaxing)) {
            if (!this.hasEffect(HcsEffects.PAIN_KILLING.get())) this.injuryManager.addRawPain(hurtPercent * 4.5);
            if (!isBurningDamage && EntityHelper.IS_BLEEDING_CAUSING_DAMAGE.test(source)) {
                this.injuryManager.addBleeding(hurtPercent * 7.5);
                this.statusManager.setBandageWorkTicks(0);
            }
        }
        if (this.getKillCredit() instanceof Spider && !(Objects.equals(source.getMsgId(), level().damageSources().magic().getMsgId()))) {
            this.addEffect(new MobEffectInstance(MobEffects.POISON, 3 * 20, 0));
        }
    }

    @Inject(method = "canEat", at = @At("RETURN"), cancellable = true)
    public void canEat(@NotNull CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() && !this.hasEffect(HcsEffects.BLEEDING.get()) && EntityHelper.getEffectAmplifier(this, HcsEffects.PARASITE_INFECTION.get()) <= 1);
    }
}