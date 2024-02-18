package biz.coolpage.hcs.mixin.entity.player;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.block.torches.BurningCrudeTorchBlock;
import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.item.KnifeItem;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.IDamageSources;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.*;
import biz.coolpage.hcs.util.ArmorHelper;
import biz.coolpage.hcs.util.DigRestrictHelper;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
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

import static biz.coolpage.hcs.recipe.CustomDryingRackRecipe.HAS_COOKED;
import static biz.coolpage.hcs.status.manager.DiseaseManager.getParasitePossibilityAndCheckFoodPoisoning;
import static biz.coolpage.hcs.util.DigRestrictHelper.Predicates.IS_PLANT;
import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_AND_SERVER;
import static biz.coolpage.hcs.util.EntityHelper.toPlayer;


@Mixin(PlayerEntity.class)
@SuppressWarnings({"CanBeFinal", "AddedMixinMembersNamePattern"})
public abstract class PlayerEntityMixin extends LivingEntity implements StatAccessor {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract PlayerInventory getInventory();

    @Shadow
    public abstract HungerManager getHungerManager();

    @Shadow
    public abstract boolean isInvulnerableTo(DamageSource damageSource);

    @Shadow
    public abstract Text getName();

    @Shadow
    public abstract String getEntityName();

    @Shadow
    public abstract boolean damage(DamageSource source, float amount);

    @Shadow
    public abstract boolean isSpectator();

    @Shadow
    public abstract void remove(RemovalReason reason);

    @Shadow
    public abstract void increaseStat(Identifier stat, int amount);

    @Shadow
    public abstract void addExhaustion(float exhaustion);

    @Shadow
    public abstract void tick();

    @Shadow
    public abstract float getMovementSpeed();

    @Shadow
    public abstract boolean isSwimming();

    @Shadow
    public int experienceLevel;
    @Shadow
    protected HungerManager hungerManager;
    @Shadow
    protected boolean isSubmergedInWater;

    @Shadow
    public abstract void setFireTicks(int fireTicks);

    @Unique
    protected ThirstManager thirstManager = new ThirstManager();
    @Unique
    protected StaminaManager staminaManager = new StaminaManager();
    @Unique
    protected TemperatureManager temperatureManager = new TemperatureManager();
    @Unique
    protected StatusManager statusManager = new StatusManager();
    @Unique
    protected SanityManager sanityManager = new SanityManager();
    @Unique
    protected NutritionManager nutritionManager = new NutritionManager();
    @Unique
    protected WetnessManager wetnessManager = new WetnessManager();
    @Unique
    protected InjuryManager injuryManager = new InjuryManager();
    @Unique
    protected MoodManager moodManager = new MoodManager();
    @Unique
    protected DiseaseManager diseaseManager = new DiseaseManager();

    @Unique
    private static void quitReturnTeleport(@Nullable Entity entity) {
        if (toPlayer(entity) instanceof ServerPlayerEntity player && player.hasStatusEffect(HcsEffects.RETURN)) {
            StatusManager statusManager1 = ((StatAccessor) player).getStatusManager();
            if (statusManager1.getReturnEffectAwaitTicks() > 0) EntityHelper.msgById(player, "hcs.tip.return_failed");
            statusManager1.setReturnEffectAwaitTicks(0);
        }
    }

    @Unique
    @Override
    public ThirstManager getThirstManager() {
        return this.thirstManager;
    }

    @Unique
    @Override
    public StaminaManager getStaminaManager() {
        return this.staminaManager;
    }

    @Unique
    @Override
    public TemperatureManager getTemperatureManager() {
        return this.temperatureManager;
    }

    @Unique
    @Override
    public StatusManager getStatusManager() {
        return this.statusManager;
    }

    @Unique
    @Override
    public SanityManager getSanityManager() {
        return this.sanityManager;
    }

    @Unique
    @Override
    public NutritionManager getNutritionManager() {
        return this.nutritionManager;
    }

    @Unique
    @Override
    public WetnessManager getWetnessManager() {
        return this.wetnessManager;
    }

    @Unique
    @Override
    public InjuryManager getInjuryManager() {
        return this.injuryManager;
    }

    @Unique
    @Override
    public MoodManager getMoodManager() {
        return this.moodManager;
    }

    @Unique
    @Override
    public DiseaseManager getDiseaseManager() {
        return this.diseaseManager;
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(@NotNull NbtCompound nbt, CallbackInfo info) {
        if (this.world.isClient) return;
        this.thirstManager.set(nbt.contains(ThirstManager.THIRST_NBT, NbtElement.DOUBLE_TYPE) ? nbt.getDouble(ThirstManager.THIRST_NBT) : 1.0);
        this.thirstManager.setSaturation(nbt.contains(ThirstManager.THIRST_SATURATION_NBT, NbtElement.FLOAT_TYPE) ? nbt.getFloat(ThirstManager.THIRST_SATURATION_NBT) : 0.2F);
        this.staminaManager.set(nbt.contains(StaminaManager.STAMINA_NBT, NbtElement.DOUBLE_TYPE) ? nbt.getDouble(StaminaManager.STAMINA_NBT) : 1.0);
        this.temperatureManager.set(nbt.contains(TemperatureManager.TEMPERATURE_NBT, NbtElement.DOUBLE_TYPE) ? nbt.getDouble(TemperatureManager.TEMPERATURE_NBT) : 0.5);
        this.temperatureManager.setSaturation(nbt.contains(TemperatureManager.TEMPERATURE_SATURATION_NBT, NbtElement.FLOAT_TYPE) ? nbt.getFloat(TemperatureManager.TEMPERATURE_SATURATION_NBT) : 0.0F);
        this.statusManager.setMaxExpLevelReached(nbt.contains(StatusManager.MAX_LVL_NBT, NbtElement.INT_TYPE) ? nbt.getInt(StatusManager.MAX_LVL_NBT) : 0);
        this.sanityManager.set(nbt.contains(SanityManager.SANITY_NBT, NbtElement.DOUBLE_TYPE) ? nbt.getDouble(SanityManager.SANITY_NBT) : 1.0);
        this.nutritionManager.setVegetable(nbt.contains(NutritionManager.NUTRITION_VEGETABLE_NBT, NbtElement.DOUBLE_TYPE) ? nbt.getDouble(NutritionManager.NUTRITION_VEGETABLE_NBT) : 1.0);
        this.wetnessManager.set(nbt.contains(WetnessManager.WETNESS_NBT, NbtElement.DOUBLE_TYPE) ? nbt.getDouble(WetnessManager.WETNESS_NBT) : 0.0);
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

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(@NotNull NbtCompound nbt, CallbackInfo info) {
        if (this.world.isClient) return;
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

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    public void getBlockBreakingSpeed(@NotNull BlockState state, @NotNull CallbackInfoReturnable<Float> cir) {
        //See abstractBlockMixin/calcBlockBreakingDelta(), which adds compulsory calling when the block hardness is -1.0F
        this.statusManager.setRecentMiningTicks(200);
        this.staminaManager.add(-0.00035, this);
        this.staminaManager.pauseRestoring();
        float speed = StatusEffectUtil.hasHaste(this) ? cir.getReturnValueF() : cir.getReturnValueF() / 3.0F;
        Item mainHand = this.getMainHandStack().getItem();
        final boolean isShovelMineable = state.isIn(BlockTags.SHOVEL_MINEABLE);
        final boolean isKnife = mainHand instanceof KnifeItem, isSword = mainHand instanceof SwordItem, isAxe = mainHand instanceof AxeItem;
        Block block = state.getBlock();
        if (!DigRestrictHelper.canBreak(mainHand, state)) {
            if (isShovelMineable) speed /= 30.0F;
            else speed = -1.0F;
            /*
            if (IS_BAREHANDED.and(IS_SURVIVAL_AND_SERVER).test(player)) {
                this.statusManager.addBareDiggingTicks();
                if (this.statusManager.getBareDiggingTicks() == 30)
                    EntityHelper.msgById(this, "hcs.tip.hurt_hand_dig");
                if (this.statusManager.getBareDiggingTicks() > 50) {
                    if (this.injuryManager.getRawPain() < 3.0) this.injuryManager.addRawPain(0.002);
                    if (this.injuryManager.getBleeding() < 3.5) this.injuryManager.addBleeding(0.004);
                    if (this.statusManager.getBareDiggingTicks() > 80 && this.world.getTime() % 40 == 0)
                        this.damage(this.world.getDamageSources().cactus(), 1.0F);
                }
            }
            */
        }
        if (isShovelMineable && mainHand instanceof ShovelItem && mainHand != Reg.FLINT_CONE) speed /= 2.0F;
        else if (state.isIn(BlockTags.AXE_MINEABLE) || isSword) {
//            System.out.println(speed);//0.00952381
            if (mainHand == Reg.FLINT_HATCHET) speed *= 6.0F;
            else speed /= 2.5F;
        }
        if ((mainHand instanceof HoeItem || isKnife) && (block instanceof CropBlock || block instanceof StemBlock || state.isIn(BlockTags.REPLACEABLE_PLANTS)))
            speed *= 2.0F;
        else if (isKnife) {
            if (IS_PLANT.test(block) || block instanceof CobwebBlock) speed *= 3.0F;
            else speed /= 10.0F;
        }
        if (this.hasStatusEffect(HcsEffects.DEHYDRATED)) speed /= 2.0F;
        if (this.hasStatusEffect(HcsEffects.STARVING)) speed /= 2.0F;
        if (this.hasStatusEffect(HcsEffects.EXHAUSTED)) speed /= 2.0F;
        if (this.hasStatusEffect(HcsEffects.UNHAPPY)) speed /= 1.2F;
        if (EntityHelper.getEffectAmplifier(this, HcsEffects.PARASITE_INFECTION) > 0) speed /= 1.5F;
        speed /= (float) Math.max(1.0, Math.pow(1.15, (EntityHelper.getEffectAmplifier(this, HcsEffects.PAIN) + 1) + (EntityHelper.getEffectAmplifier(this, HcsEffects.INJURY) + 1)));
        if (DigRestrictHelper.Predicates.IS_BREAKABLE_FUNCTIONAL.test(block))
            speed *= (block instanceof AbstractFurnaceBlock || block == Blocks.ENDER_CHEST) ? 16.0F : 4.0F;
        else if (block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN) speed *= 3.0F;
        else if ((block == Blocks.CLAY && !isShovelMineable)) speed /= 9.0F;
        else if (block instanceof LeavesBlock && !isSword && !isAxe) speed /= 10.0F;
        if (block instanceof TorchBlock || block instanceof BurningCrudeTorchBlock || state.isIn(BlockTags.FLOWERS))
            speed = 999999.0F;
        cir.setReturnValue(speed);
    }

    @Inject(method = "damage", at = @At("HEAD"))
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient) this.staminaManager.pauseRestoring();
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "eatFood", at = @At("HEAD"))
    public void eatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if ((Object) this instanceof ServerPlayerEntity player && !stack.isEmpty()) {
            Item item = stack.getItem();
            String name = item.getTranslationKey();
            FoodComponent food = item.getFoodComponent();
            EntityHelper.checkOvereaten(player, false);
            if (food != null) {
                if (Math.random() < getParasitePossibilityAndCheckFoodPoisoning(item, this))
                    this.diseaseManager.addParasite(0.12);
                if (food.isMeat() || name.contains("egg")) this.nutritionManager.addVegetable(-0.1);
                else if (name.contains("kelp") || name.contains("sugar_cane")) this.nutritionManager.addVegetable(0.19);
                else if (name.contains("berries") || name.contains("berry")) this.nutritionManager.addVegetable(0.21);
                else if (name.contains("apple") || name.contains("orange") || name.contains("carrot") || name.contains("cactus") || name.contains("melon") || name.contains("potherb") || name.contains("shoot") || name.contains("salad") || name.contains("fruit"))
                    this.nutritionManager.addVegetable(0.35);
                if (name.contains("ginger")) this.diseaseManager.setCold(-0.2);
                int freshLevel = RotHelper.addDebuff(world, player, stack);
                if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) {
                    this.sanityManager.add(1.0);
                    this.statusManager.setSoulImpairedStat(0);
                    this.injuryManager.applyPainkiller();
                    this.injuryManager.setBleeding(0.0);
                    this.injuryManager.setFracture(0.0);
                    this.diseaseManager.reset();
                    this.moodManager.setHappiness(1.0);
                } else if (item == Items.KELP || Reg.IS_BARK.test(item)) {
                    if (item == Reg.WILLOW_BARK) this.injuryManager.applyPainkiller();
                    this.sanityManager.add(-0.02);
                } else if (item == Reg.FEARLESSNESS_HERB) this.moodManager.applyPanicKiller();
                else if (item == Items.POISONOUS_POTATO || item == Items.SPIDER_EYE || item == Items.CHORUS_FRUIT)
                    this.sanityManager.add(-0.07);
                else if (item == Items.ROTTEN_FLESH) this.sanityManager.add(-0.1);
                else if (item == Items.RED_MUSHROOM || item == Items.CRIMSON_FUNGUS || item == Items.WARPED_FUNGUS)
                    this.sanityManager.add(-1.0);
                else if (freshLevel > 2) {
                    if (item == Items.PUMPKIN_PIE || item == Items.RABBIT_STEW || item == Items.GOLDEN_CARROT || item == Items.GLISTERING_MELON_SLICE)
                        this.sanityManager.add(0.15);
                    else if (item == Items.MUSHROOM_STEW || item == Reg.COOKED_CACTUS_FLESH || item == Items.BEETROOT_SOUP)
                        this.sanityManager.add(0.07);
                    else if (item == Items.DRIED_KELP) this.sanityManager.add(0.05);
                    else if (item == Items.COOKIE || item == Items.APPLE || item == Reg.ORANGE || item == Items.SUGAR)
                        this.sanityManager.add(0.025);
                    else if ((HAS_COOKED.test(name) && item != Reg.ROASTED_WORM) || item == Items.BREAD)
                        this.sanityManager.add(food.isSnack() ? 0.005 : 0.01);
                }
                if (item == Items.WHEAT || item == Items.SUGAR || item == Items.SUGAR_CANE || item == Reg.POTHERB) {
                    final int increasedFoodLevel = this.hungerManager.getFoodLevel() + 1;
                    if (increasedFoodLevel > 20) this.hungerManager.setExhaustion(0.0F);
                    else this.hungerManager.setFoodLevel(increasedFoodLevel);

                } else if (((name.contains("seeds") || Reg.IS_BARK.test(item)) && food.getHunger() == 0) || item == Reg.COOKED_SWEET_BERRIES || item == Reg.ROT || item == Items.KELP || item == Reg.PETALS_SALAD) {
                    EntityHelper.addDecimalFoodLevel(player, 0.4F, false);
                    if (item == Items.PUMPKIN_SEEDS) this.diseaseManager.addParasite(-1.0);
                }
                if (!name.contains("dried") && !name.contains("jerky") && !name.contains("seeds") && item != Items.COOKIE && item != Items.BREAD && item != Items.SUGAR) {
                    if (name.contains("stew") || name.contains("soup"))
                        this.thirstManager.add(item == Items.MUSHROOM_STEW ? 0.06 : 0.2);
                    else if (item == Items.MELON_SLICE || item == Items.APPLE || item == Reg.ORANGE || item == Items.SUGAR_CANE || item == Reg.CACTUS_FLESH)
                        this.thirstManager.addDirectly(0.05);
                    else if (item == Items.CARROT || item == Items.POTATO || item == Reg.PUMPKIN_SLICE || item == Reg.PETALS_SALAD)
                        this.thirstManager.addDirectly(0.03);
                    else if (food.isMeat() || name.contains("berries") || item == Reg.COOKED_CACTUS_FLESH || item == Reg.COOKED_PUMPKIN_SLICE || item == Reg.COOKED_CARROT || item == Reg.COOKED_BAMBOO_SHOOT || item == Reg.COOKED_SWEET_BERRIES)
                        this.thirstManager.addDirectly(0.02);
                    else this.thirstManager.addDirectly(0.01);
                }
            }
        }
    }


    @Inject(method = "addExhaustion", at = @At("TAIL"))
    public void addExhaustion(float exhaustion, CallbackInfo ci) {
        if (!this.world.isClient) {
            this.thirstManager.add(-exhaustion / 60.0); //70 originally
            if (this.hasStatusEffect(HcsEffects.MALNUTRITION)) this.hungerManager.addExhaustion(exhaustion * 0.5F);
        }
    }

    @Inject(method = "increaseTravelMotionStats", at = @At("HEAD"), cancellable = true)
    public void increaseTravelMotionStats(double dx, double dy, double dz, CallbackInfo ci) {
        if (this.onGround && !this.hasVehicle()) {
            int i = Math.round((float) Math.sqrt(dx * dx + dz * dz) * 100.0f);
            if (i > 0) {
                if (this.isSprinting()) {
                    this.increaseStat(Stats.SPRINT_ONE_CM, i);
                    this.addExhaustion(0.01f * (float) i * 0.01f);
                } else if (this.isInSneakingPose()) {
                    this.increaseStat(Stats.CROUCH_ONE_CM, i);
                    this.addExhaustion(0.007f * (float) i * 0.01f);
                } else {
                    this.increaseStat(Stats.WALK_ONE_CM, i);
                    this.addExhaustion(0.007f * (float) i * 0.01f);
                }
            }
            ci.cancel();
        }
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    public void jump1(@NotNull CallbackInfo ci) {
        if (this.hasStatusEffect(HcsEffects.EXHAUSTED)) {
            StatusEffectInstance exhaustedEffectInstance = this.getStatusEffect(HcsEffects.EXHAUSTED);
            if (exhaustedEffectInstance != null && exhaustedEffectInstance.getAmplifier() > 0) ci.cancel();
        }
        if (this.hasStatusEffect(HcsEffects.INJURY)) {
            StatusEffectInstance exhaustedEffectInstance = this.getStatusEffect(HcsEffects.INJURY);
            if (exhaustedEffectInstance != null && exhaustedEffectInstance.getAmplifier() > 2) ci.cancel();
        }
    }

    @Inject(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;incrementStat(Lnet/minecraft/util/Identifier;)V", shift = At.Shift.AFTER), cancellable = true)
    public void jump2(@NotNull CallbackInfo ci) {
        double currRealPain = this.injuryManager.getRealPain();
        float rate = (this.isSprinting() ? 3.0F : 1.0F) * (currRealPain > 2.0 ? (float) (currRealPain * 1.5) : 1.0F) * (this.hasStatusEffect(HcsEffects.FRACTURE) ? 1.5F : 1.0F);
        this.staminaManager.pauseRestoring();
        this.addExhaustion(0.035F * rate);
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
        @Nullable PlayerEntity player = toPlayer(this);
        if (!IS_SURVIVAL_AND_SERVER.test(player)) return;
        //noinspection ResultOfMethodCallIgnored
        ArmorHelper.getFinalProtection(player); //Refresh player armor protection
        // Painful sound effect
        final double currPain = ((StatAccessor) this).getInjuryManager().getRealPain();
        boolean outOfDarkness = true, hasDarkDebuff = false;
        if (currPain > 2.0 && this.hasStatusEffect(HcsEffects.PAIN) && this.world.getTime() % Math.max(1, 30 * (6 - (int) currPain)) == 0)
            this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_BREATH, SoundCategory.PLAYERS, (float) (currPain / 14), world.random.nextFloat() * 0.1f + 0.9f, false); // using expect ver seems avoids subtitles
        int oxyLackLvl = 0;
        final double y = this.getY();
        final boolean isRelaxingMode = HcsDifficulty.isOf(this.world, HcsDifficulty.HcsDifficultyEnum.relaxing);
        final boolean isDarkSafe = isRelaxingMode || EntityHelper.isLuminousBlockWorking(this) || this.hasStatusEffect(StatusEffects.NIGHT_VISION);
        this.addExhaustion(0.001F);
        this.statusManager.setRecentAttackTicks(Math.max(0, this.statusManager.getRecentAttackTicks() - 1));
        this.statusManager.setRecentMiningTicks(Math.max(0, this.statusManager.getRecentMiningTicks() - 1));
        this.statusManager.setRecentHasColdWaterBagTicks(Math.max(0, this.statusManager.getRecentHasColdWaterBagTicks() - 1));
        this.statusManager.setRecentHasHotWaterBagTicks(Math.max(0, this.statusManager.getRecentHasHotWaterBagTicks() - 1));
        this.statusManager.setRecentLittleOvereatenTicks(this.hungerManager.getFoodLevel() < 20 ? 0 : Math.max(0, this.statusManager.getRecentLittleOvereatenTicks() - 1));
        this.statusManager.setRecentSleepTicks(Math.max(0, this.statusManager.getRecentSleepTicks() - 1));
        this.statusManager.setRecentHurtTicks(this.statusManager.getRecentHurtTicks() - 1);
        if (this.statusManager.getBandageWorkTicks() > 0) statusManager.addBandageWorkTicks(-1);
        if (this.isWet()) this.statusManager.setRecentWetTicks(20);
        else this.statusManager.setRecentWetTicks(Math.max(0, this.statusManager.getRecentWetTicks() - 1));
        if (this.statusManager.getRecentMiningTicks() < 20) this.statusManager.setBareDiggingTicks(0);
        //Set max health according to max exp level reached
        if (this.experienceLevel > this.statusManager.getMaxExpLevelReached())
            this.statusManager.setMaxExpLevelReached(this.experienceLevel);
        /*
        int maxLvlReached = this.statusManager.getMaxExpLevelReached();
        EntityAttributeInstance instance = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (instance != null) {
            final int limitedMaxHealth = HcsDifficulty.isOf(player, HcsDifficulty.HcsDifficultyEnum.relaxing) ? 20 : Math.min(20, (int) Math.floor(maxLvlReached / 4.0) + 12);//Cut down max health when in low exp lvl
            //HcsDifficulty.chooseVal(player, Math.min(30, (int) Math.floor(maxLvlReached / 3.0) + 20), Math.min(30, (int) Math.floor(maxLvlReached / 2.0) + 12), Math.min(20, (int) Math.floor(maxLvlReached / 3.0) + 12));
            final double currMaxHealth = instance.getBaseValue();
            if (limitedMaxHealth > currMaxHealth || (limitedMaxHealth < currMaxHealth && maxLvlReached < 36)) {
                instance.setBaseValue(limitedMaxHealth);
                if (limitedMaxHealth < this.getHealth()) this.setHealth((float) limitedMaxHealth);
            } else if (maxLvlReached >= 36 && currMaxHealth < 20) instance.setBaseValue(20);
        }
        */
        if (this.getPos().distanceTo(this.staminaManager.getLastVecPos()) > 0.0001) {
            //Player is moving; `this.getVelocity()` and `this.speed` are useless
            quitReturnTeleport(this);
            if (!this.hasVehicle()) {
                boolean shouldPauseRestoring = true;
                if (this.onGround) {
                    if (this.isSprinting()) this.staminaManager.add(-0.0006, this);
                    else if (this.isInSneakingPose()) this.staminaManager.add(-0.0001, this);
                    else if (this.staminaManager.get() < 0.7) { //walking while stamina < 0.7 will getRealPain a recovery
                        shouldPauseRestoring = false;
                        this.staminaManager.add(this.staminaManager.get() < 0.3 ? 0.0001 : 0.001, this);
                    }
                }
                if (this.isSwimming() || this.isClimbing()) this.staminaManager.add(-0.001, this);
                else if (this.isTouchingWater()) this.staminaManager.add(-0.0004, this);
                if (shouldPauseRestoring) this.staminaManager.pauseRestoring();
            }
        } else {
            if (this.getThirstManager().get() > 0.5 && this.getHungerManager().getFoodLevel() > 10 && !this.hasStatusEffect(HcsEffects.COLD))
                this.staminaManager.add(0.007, this);
            else if (this.getThirstManager().get() > 0.3 && this.getHungerManager().getFoodLevel() > 6)
                this.staminaManager.add(0.003, this);
            else this.staminaManager.add(0.001, this);
        }
        //Gain sanity when being exposed in the sun with flower in hand
        BlockPos headPos = this.getBlockPos().up();
        int skyBrightness = this.world.getLightLevel(LightType.SKY, headPos);
        if ((this.getMainHandStack().isIn(ItemTags.FLOWERS) || this.getOffHandStack().isIn(ItemTags.FLOWERS)) && this.world.isDay() && skyBrightness >= LightType.SKY.value)
            this.sanityManager.add(0.000009);
        //Lose sanity
        if (this.hasStatusEffect(StatusEffects.WITHER)) this.sanityManager.add(-0.00008);
        else if (this.hasStatusEffect(StatusEffects.POISON)) this.sanityManager.add(-0.00003);
        boolean isInCavelike = skyBrightness < 1 && this.world.getDimension().hasSkyLight();
        boolean isInUnpleasantDimension = !this.world.getDimension().bedWorks() || this.world.getRegistryKey() == World.NETHER; //Avoid mods conflicts since sleeping in the nether is setRealPain to permissive
        if (((this.world.isNight() || isInCavelike) && !this.hasStatusEffect(StatusEffects.NIGHT_VISION)) || isInUnpleasantDimension) {
            double sanDecrement = 0.00001;
            int blockBrightness = this.world.getLightLevel(LightType.BLOCK, headPos);
            if (!isInUnpleasantDimension) {
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
                                this.damage(((IDamageSources) this.world.getDamageSources()).darkness(), 2.0F);
                        }
                    }
                    this.statusManager.setInDarknessTicks(currDarkTicks + 1);
                    outOfDarkness = false;
                } else if (blockBrightness < 8) sanDecrement = 0.000008;
            }
            this.sanityManager.add(-sanDecrement * HcsDifficulty.chooseVal(player, 0.5, 1.0, 2.0));
        }
        if (isInCavelike) {
            //Lose oxygen in deep cave
            if (y < 42) oxyLackLvl = 3;
            else if (y < 49) oxyLackLvl = 2;
            else if (y < 56) oxyLackLvl = 1;
        }
        if (outOfDarkness) {
            this.statusManager.setInDarknessTicks(0);
            if (this.statusManager.getLastInDarknessTicks() >= 60) EntityHelper.msgById(this, "hcs.tip.dark.fade");
        }
        if (this.hasStatusEffect(StatusEffects.STRENGTH)) this.staminaManager.reset();
        this.staminaManager.setLastVecPos(this.getPos());
        this.sanityManager.updateDifference();
        this.statusManager.setOxygenLackLevel(oxyLackLvl);
        if (this.statusManager.getFinalOxygenLackLevel() >= 3 && (this.world.getTime() % 20 == 0 || this.getAir() < 0))
            this.setAir(this.getNextAirUnderwater(this.getAir()));
        if (this.getAir() < -20 && this.world.getTime() % 15 == 0)
            this.damage(((IDamageSources) this.world.getDamageSources()).oxygenDeficiency(), 1.0F);
        //Wetness
        if (this.isSubmergedInWater) this.wetnessManager.add(0.03);
        else if (this.isTouchingWater() && this.wetnessManager.get() < 0.7) this.wetnessManager.add(0.01);
        else if (this.isBeingRainedOn()) this.wetnessManager.add(0.0004);
        else if (!this.isTouchingWater()) {
            if (this.getFireTicks() > 0) {
                this.wetnessManager.add(-0.3);
                if (this.wetnessManager.get() > 0.0) this.setFireTicks(0);
            } else if (this.temperatureManager.getAmbientCache() > 0.0F)
                this.wetnessManager.add(-Math.abs(this.temperatureManager.getAmbientCache() / 100.0));
            else {
                float rate = this.thirstManager.getThirstRateAffectedByTemp();
                this.wetnessManager.add(-Math.abs(0.00015 * rate * rate));
            }
        }
        //Disease
        this.diseaseManager.tick(!this.hasStatusEffect(HcsEffects.WET));
        //Controls the max percentage that soul impaired effect can deduct depending on the HcsDifficulty
        int maxSoulImpaired = StatusManager.getMaxSoulImpaired(this);
        if (this.statusManager.getSoulImpairedStat() > maxSoulImpaired)
            this.statusManager.setSoulImpairedStat(maxSoulImpaired);
        this.statusManager.setHasDarknessEnvelopedDebuff(hasDarkDebuff);
        //Handle buff of return
        if (this.hasStatusEffect(HcsEffects.RETURN))
            this.statusManager.setReturnEffectAwaitTicks(this.statusManager.getReturnEffectAwaitTicks() + 1);
        else this.statusManager.setReturnEffectAwaitTicks(0);
        //Player will not pain in relaxing mode (Also see `applyDamage()`)
        if (isRelaxingMode) {
            this.injuryManager.setRawPain(0.0);
            this.injuryManager.setBleeding(0.0);
            this.injuryManager.setFracture(0.0);
        }
    }

    @Inject(method = "getXpToDrop", at = @At("HEAD"), cancellable = true)
    public void getXpToDrop(CallbackInfoReturnable<Integer> cir) {
        //Drop nearly all xp after death
        if (!this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) && IS_SURVIVAL_AND_SERVER.test(toPlayer(this))) {
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
    @ModifyArg(method = "handleFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;increaseStat(Lnet/minecraft/util/Identifier;I)V"), index = 1)
    public int handleFallDamage(int amount) {
        return 0; //See at BlockMixin/onLandedUpon()
    }

    @Inject(method = "applyDamage", at = @At("TAIL"))
    public void applyDamage(DamageSource source, float amount, CallbackInfo ci) { //This method won't be called when the damage is taken by a shield
        //Do not add pain in ServerPlayerEntity as it adds with redundant repeat
        quitReturnTeleport(this);
        //Calc "feeling" damage amount
        float feelingAmount = amount;
        boolean isBurningDamage = EntityHelper.IS_BURNING_DAMAGE.test(source);
        if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR))
            feelingAmount = ArmorHelper.getDamageLeftWithReducedArmor(toPlayer(this), amount);
        feelingAmount = this.modifyAppliedDamage(source, feelingAmount);
        if (isBurningDamage) feelingAmount *= 2;
        float hurtPercent = feelingAmount / 20.0F;
        //Update status manager data
        this.statusManager.setRecentFeelingDamage(feelingAmount);
        this.statusManager.setRecentHurtTicks(20);
        //Add damage-leading debuffs
        if (EntityHelper.IS_PHYSICAL_DAMAGE.test(source) && this.getAbsorptionAmount() < 2.0F && !HcsDifficulty.isOf(toPlayer(this), HcsDifficulty.HcsDifficultyEnum.relaxing)) {
            if (!this.hasStatusEffect(HcsEffects.PAIN_KILLING)) this.injuryManager.addRawPain(hurtPercent * 4.5);
            if (!isBurningDamage && EntityHelper.IS_BLEEDING_CAUSING_DAMAGE.test(source)) {
                this.injuryManager.addBleeding(hurtPercent * 7.5);
                this.statusManager.setBandageWorkTicks(0);
            }
        }
        //Enable poisoning abilities for vanilla spiders
        if (this.getAttacker() instanceof SpiderEntity && !(Objects.equals(source.getName(), world.getDamageSources().magic().getName()))) {
            //trigger when being attacked by (cave)spider and not by poison
            //Also see SpiderEntityMixin for spiders' webbing
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 3 * 20, 0));
        }
    }

    @Inject(method = "canFoodHeal", at = @At("RETURN"), cancellable = true)
    public void canFoodHeal(@NotNull CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValueZ() && !this.hasStatusEffect(HcsEffects.BLEEDING) && EntityHelper.getEffectAmplifier(this, HcsEffects.PARASITE_INFECTION) <= 1);
    }
}
