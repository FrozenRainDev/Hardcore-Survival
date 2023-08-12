package com.hcs.mixin.entity.player;

import com.hcs.Reg;
import com.hcs.status.HcsEffects;
import com.hcs.status.accessor.DamageSourcesAccessor;
import com.hcs.status.accessor.StatAccessor;
import com.hcs.status.manager.*;
import com.hcs.util.DigRestrictHelper;
import com.hcs.util.EntityHelper;
import com.hcs.util.RotHelper;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static com.hcs.util.DigRestrictHelper.canBreak;

@Mixin(PlayerEntity.class)
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
    public abstract boolean isCreative();

    @Shadow
    protected boolean isSubmergedInWater;

    @Shadow
    public abstract void setFireTicks(int fireTicks);

    @SuppressWarnings("CanBeFinal")
    @Unique
    ThirstManager thirstManager = new ThirstManager();
    @SuppressWarnings("CanBeFinal")
    @Unique
    StaminaManager staminaManager = new StaminaManager();
    @SuppressWarnings("CanBeFinal")
    @Unique
    TemperatureManager temperatureManager = new TemperatureManager();
    @SuppressWarnings("CanBeFinal")
    @Unique
    StatusManager statusManager = new StatusManager();
    @SuppressWarnings("CanBeFinal")
    @Unique
    SanityManager sanityManager = new SanityManager();
    @SuppressWarnings("CanBeFinal")
    @Unique
    NutritionManager nutritionManager = new NutritionManager();
    @SuppressWarnings("CanBeFinal")
    @Unique
    WetnessManager wetnessManager = new WetnessManager();

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public ThirstManager getThirstManager() {
        return this.thirstManager;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public StaminaManager getStaminaManager() {
        return this.staminaManager;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public TemperatureManager getTemperatureManager() {
        return this.temperatureManager;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public StatusManager getStatusManager() {
        return this.statusManager;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public SanityManager getSanityManager() {
        return this.sanityManager;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public NutritionManager getNutritionManager() {
        return this.nutritionManager;
    }


    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public WetnessManager getWetnessManager() {
        return this.wetnessManager;
    }


    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo info) {
        if (!this.world.isClient()) {
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
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo info) {
        if (!this.world.isClient()) {
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
        }
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    public void getBlockBreakingSpeed(@NotNull BlockState state, @NotNull CallbackInfoReturnable<Float> cir) {
        this.statusManager.setRecentMiningTicks(200);
        this.staminaManager.add(-0.00035, this);
        this.staminaManager.pauseRestoring();
        float speed = cir.getReturnValue() / 3.5F;
        boolean shovelMineable = state.isIn(BlockTags.SHOVEL_MINEABLE);
        ItemStack mainHandStack = this.getMainHandStack();
        Item mainHand = mainHandStack.getItem();
        Block block = state.getBlock();
        if (!canBreak(mainHand, state)) {
            if (shovelMineable)
                speed /= 30.0F;//if((Object)this instanceof PlayerEntity)((PlayerEntity)(Object)this).damage(DamageSource.CACTUS,0.0001F);
            else speed = -1.0F;
        }
        if (mainHand != Reg.FLINT_HATCHET && (state.isIn(BlockTags.AXE_MINEABLE)) || mainHand instanceof SwordItem)
            speed /= 2.0F;
        if (this.hasStatusEffect(HcsEffects.DEHYDRATED) || this.hasStatusEffect(HcsEffects.STARVING) || this.hasStatusEffect(HcsEffects.EXHAUSTED))
            speed /= 2.0F;
        if (DigRestrictHelper.isBreakableFunctionalBlock(block))
            speed *= (block instanceof AbstractFurnaceBlock || block == Blocks.ENDER_CHEST) ? 16.0F : 4.0F;
        if (block == Blocks.SUGAR_CANE || block == Blocks.CLAY) speed /= 9.0F;
        else if (block instanceof LeavesBlock && !(mainHand instanceof SwordItem) && !(mainHand instanceof AxeItem))
            speed /= 25.0F;
        if (block instanceof TorchBlock || state.isIn(BlockTags.FLOWERS)) speed = 999999.0F;
        cir.setReturnValue(speed);
    }

    @Inject(method = "damage", at = @At("HEAD"))
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!world.isClient()) {
            this.staminaManager.pauseRestoring();
            if (this.getAttacker() instanceof SpiderEntity && !(Objects.equals(source.getName(), world.getDamageSources().magic().getName()))) {
                //trigger when being attacked by (cave)spider and not by poison
                BlockState state = this.world.getBlockState(this.getBlockPos());
                Block block = state.getBlock();
                Material material = state.getMaterial();
                if (material == Material.REPLACEABLE_PLANT || block == Blocks.AIR)
                    this.world.setBlockState(this.getBlockPos(), Blocks.COBWEB.getDefaultState());
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 3 * 20, 0));
            }
        }
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
                if (food.isMeat() || name.contains("egg"))
                    this.nutritionManager.addVegetable(-0.1);
                else if (name.contains("kelp") || name.contains("sugar_cane")) this.nutritionManager.addVegetable(0.19);
                else if (name.contains("berries") || name.contains("berry")) this.nutritionManager.addVegetable(0.21);
                else if (name.contains("apple") || name.contains("orange") || name.contains("carrot") || name.contains("cactus") || name.contains("melon") || name.contains("potherb") || name.contains("shoot") || name.contains("salad") || name.contains("fruit"))
                    this.nutritionManager.addVegetable(0.35);
                int freshLevel = RotHelper.addDebuff(world, player, stack);
                if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) {
                    this.sanityManager.add(1.0);
                    this.statusManager.setSoulImpairedStat(0);
                } else if (item == Items.KELP) this.sanityManager.add(-0.04);
                else if (item == Items.POISONOUS_POTATO || item == Items.SPIDER_EYE || item == Items.CHORUS_FRUIT)
                    this.sanityManager.add(-0.07);
                else if (item == Items.ROTTEN_FLESH) this.sanityManager.add(-0.1);
                else if (item == Items.RED_MUSHROOM || item == Items.CRIMSON_FUNGUS || item == Items.WARPED_FUNGUS)
                    this.sanityManager.add(-1.0);
                else if (freshLevel > 2) {
                    if (item == Items.PUMPKIN_PIE || item == Items.RABBIT_STEW || item == Items.GOLDEN_CARROT)
                        this.sanityManager.add(0.15);
                    else if (item == Items.MUSHROOM_STEW || item == Reg.COOKED_CACTUS_FLESH || item == Items.BEETROOT_SOUP)
                        this.sanityManager.add(0.07);
                    else if (item == Items.DRIED_KELP) this.sanityManager.add(0.05);
                    else if (item == Items.COOKIE || item == Items.APPLE || item == Reg.ORANGE)
                        this.sanityManager.add(0.03);
                    else if (name.contains("cooked_") || name.contains("roasted_") || name.contains("baked_") || item == Items.BREAD || item == Items.SUGAR)
                        this.sanityManager.add(0.02);
                }
                if (item == Items.WHEAT || item == Items.SUGAR || item == Items.SUGAR_CANE || item == Reg.POTHERB || item == Reg.ROASTED_SEEDS)
                    this.hungerManager.setFoodLevel(Math.min(this.hungerManager.getFoodLevel() + 1, 20));
                else if ((name.contains("seeds") && food.getHunger() == 0) || item == Reg.COOKED_SWEET_BERRIES || item == Reg.ROT || item == Items.KELP || item == Reg.PETALS_SALAD)
                    EntityHelper.addDecimalFoodLevel(player, 0.4F, false);
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
            this.thirstManager.add(-exhaustion / 55.0); //70 originally
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
    }

    @Inject(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;incrementStat(Lnet/minecraft/util/Identifier;)V", shift = At.Shift.AFTER), cancellable = true)
    public void jump2(@NotNull CallbackInfo ci) {
        float rate = this.isSprinting() ? 3.0F : 1.0F;
        this.staminaManager.pauseRestoring();
        this.addExhaustion(0.025F * rate);
        this.staminaManager.pauseRestoring(40);
        this.staminaManager.add(-0.005F * rate, this);
        ci.cancel();
    }

    @Inject(method = "attack", at = @At("TAIL"))
    public void attack(Entity target, CallbackInfo ci) {
        this.staminaManager.pauseRestoring(50);
        this.staminaManager.add(-0.025, this);
        this.statusManager.setRecentAttackTicks(200);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (this.world.isClient) return;
        int oxyLackLvl = 0;
        double y = this.getY();
        this.addExhaustion(0.001F);
        this.statusManager.setRecentAttackTicks(Math.max(0, this.statusManager.getRecentAttackTicks() - 1));
        this.statusManager.setRecentMiningTicks(Math.max(0, this.statusManager.getRecentMiningTicks() - 1));
        this.statusManager.setRecentHasColdWaterBagTicks(Math.max(0, this.statusManager.getRecentHasColdWaterBagTicks() - 1));
        this.statusManager.setRecentHasHotWaterBagTicks(Math.max(0, this.statusManager.getRecentHasHotWaterBagTicks() - 1));
        this.statusManager.setRecentLittleOvereatenTicks(this.hungerManager.getFoodLevel() < 20 ? 0 : Math.max(0, this.statusManager.getRecentLittleOvereatenTicks() - 1));
        this.statusManager.setRecentSleepTicks(Math.max(0, this.statusManager.getRecentSleepTicks() - 1));
        if (this.sanityManager.getMonsterWitnessingTicks() > 0) {
            this.sanityManager.add(-0.00004);
            this.sanityManager.setMonsterWitnessingTicks(this.sanityManager.getMonsterWitnessingTicks() - 1);
        }
        if (this.isWet()) this.statusManager.setRecentWetTicks(20);
        else this.statusManager.setRecentWetTicks(Math.max(0, this.statusManager.getRecentWetTicks() - 1));
        //Set max health according to max exp level reached
        if (this.experienceLevel > this.statusManager.getMaxExpLevelReached())
            this.statusManager.setMaxExpLevelReached(this.experienceLevel);
        int maxLvlReached = this.statusManager.getMaxExpLevelReached();
        EntityAttributeInstance instance = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (instance != null) {
            int limitedMaxHealth = Math.min(20, (int) Math.floor(maxLvlReached / 3.0) + 8);//Cut down max health when in low exp lvl
            double currentMaxHealth = instance.getBaseValue();
            if (limitedMaxHealth > currentMaxHealth || (limitedMaxHealth < currentMaxHealth && maxLvlReached < 36)) {
                instance.setBaseValue(limitedMaxHealth);
                if (limitedMaxHealth < this.getHealth()) this.setHealth((float) limitedMaxHealth);
            } else if (maxLvlReached >= 36 && currentMaxHealth < 20) instance.setBaseValue(20);
        }
        if (!this.isCreative() && !this.isSpectator()) {
            if (this.getPos().distanceTo(this.staminaManager.getLastVecPos()) > 0.0001) {
                //Player is moving this.getVelocity() and this.speed are useless
                if (!this.hasVehicle()) {
                    boolean shouldPauseRestoring = true;
                    if (this.onGround) {
                        if (this.isSprinting()) this.staminaManager.add(-0.001, this);
                        else if (this.isInSneakingPose()) this.staminaManager.add(-0.0001, this);
                        else if (this.staminaManager.get() < 0.7) {//walking while stamina < 0.7 will get a recovery
                            shouldPauseRestoring = false;
                            this.staminaManager.add(this.staminaManager.get() < 0.3 ? 0.0001 : 0.001, this);
                        }
                    }
                    if (this.isSwimming() || this.isClimbing()) this.staminaManager.add(-0.001, this);
                    else if (this.isTouchingWater()) this.staminaManager.add(-0.0004, this);
                    if (shouldPauseRestoring) this.staminaManager.pauseRestoring();
                }
            } else {
                if (this.getThirstManager().get() > 0.6 && this.getHungerManager().getFoodLevel() > 12)
                    this.staminaManager.add(0.006, this);
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
            boolean isInUnpleasantDimension = !this.world.getDimension().bedWorks() || this.world.getRegistryKey() == World.NETHER;//Avoid mods conflict since sleeping in the nether is set to permissive
            if (((this.world.isNight() || isInCavelike) && !this.hasStatusEffect(StatusEffects.NIGHT_VISION)) || isInUnpleasantDimension) {
                double sanDecrement = 0.00001;
                int blockBrightness = this.world.getLightLevel(LightType.BLOCK, headPos);
                if (!isInUnpleasantDimension) {
                    if (blockBrightness < 2 && isInCavelike) sanDecrement = 0.00004;
                    else if (blockBrightness < 8) sanDecrement = 0.000015;
                }
                this.sanityManager.add(-sanDecrement);
            }
            if (isInCavelike) {
                //Lose oxygen in deep cave
                if (y < 42) oxyLackLvl = 3;
                else if (y < 49) oxyLackLvl = 2;
                else if (y < 56) oxyLackLvl = 1;
            }
        }
        if (this.hasStatusEffect(StatusEffects.STRENGTH)) this.staminaManager.reset();
        this.staminaManager.setLastVecPos(this.getPos());
        this.sanityManager.updateDifference();
        this.statusManager.setOxygenLackLevel(oxyLackLvl);
        if (this.statusManager.getFinalOxygenLackLevel() >= 3 && (this.world.getTime() % 20 == 0 || this.getAir() < 0))
            this.setAir(this.getNextAirUnderwater(this.getAir()));
        if (this.getAir() < -20 && this.world.getTime() % 15 == 0)
            this.damage(((DamageSourcesAccessor) this.world.getDamageSources()).oxygenDeficiency(), 1.0F);
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
    }

    @Inject(method = "getXpToDrop", at = @At("HEAD"), cancellable = true)
    public void getXpToDrop(CallbackInfoReturnable<Integer> cir) {
        //Drop nearly all xp after death
        if (!(this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || this.isSpectator())) {
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

}
