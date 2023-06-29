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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static com.hcs.status.network.ServerS2C.doubleToInt;
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

    ThirstManager thirstManager = new ThirstManager();
    StaminaManager staminaManager = new StaminaManager();
    TemperatureManager temperatureManager = new TemperatureManager();
    StatusManager statusManager = new StatusManager();
    SanityManager sanityManager = new SanityManager();
    NutritionManager nutritionManager = new NutritionManager();

    @Override
    public ThirstManager getThirstManager() {
        return this.thirstManager;
    }

    @Override
    public StaminaManager getStaminaManager() {
        return this.staminaManager;
    }

    @Override
    public TemperatureManager getTemperatureManager() {
        return this.temperatureManager;
    }

    @Override
    public StatusManager getStatusManager() {
        return this.statusManager;
    }

    @Override
    public SanityManager getSanityManager() {
        return this.sanityManager;
    }

    @Override
    public NutritionManager getNutritionManager() {
        return this.nutritionManager;
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
        }
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At("HEAD"), cancellable = true)
    public void getBlockBreakingSpeed(@NotNull BlockState state, CallbackInfoReturnable<Float> cir) {
        this.statusManager.setRecentMiningTicks(200);
        Block block = state.getBlock();
        float f = this.getInventory().getBlockBreakingSpeed(state);
        int slowDown = 0;
        ItemStack mainHandStack = this.getMainHandStack();
        Item mainHand = mainHandStack.getItem();
        if (mainHand.isEnchantable(new ItemStack(mainHand))) {
            //is by tool? f > 1.0F originally
            int i = EnchantmentHelper.getEfficiency(this);
            ItemStack itemStack = this.getMainHandStack();
            if (i > 0 && !itemStack.isEmpty()) {
                f += (float) (i * i + 1);
            }
        } else ++slowDown;
        if (StatusEffectUtil.hasHaste(this)) {
            f *= 1.0F + (float) (StatusEffectUtil.getHasteAmplifier(this) + 1) * 0.2F;
        } else ++slowDown;
        if (slowDown == 2) f /= 5.0f;
        else if (mainHand != Reg.FLINT_HATCHET) f /= 2.0f;
        if (this.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float g = switch (Objects.requireNonNull(this.getStatusEffect(StatusEffects.MINING_FATIGUE)).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };
            f *= g;
        }
        if (this.hasStatusEffect(HcsEffects.EXHAUSTED))
            f *= doubleToInt(this.staminaManager.get()) <= 10 ? 0.05F : 0.7F;
        if (this.isSubmergedIn(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(this)) f /= 5.0F;
        if (!this.onGround) f /= 5.0F;
        if (state.isOf(Blocks.SUGAR_CANE)) f /= 9.0F;
        if (this.hasStatusEffect(HcsEffects.DEHYDRATED) || this.hasStatusEffect(HcsEffects.STARVING)) f /= 2;
        if (DigRestrictHelper.isBreakableFunctionalBlock(block))
            f *= (block instanceof AbstractFurnaceBlock || block == Blocks.ENDER_CHEST) ? 16 : 4;
        if (!canBreak(mainHand, state)) {
            if (block.getSoundGroup(state) == BlockSoundGroup.GRAVEL || block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM || block == Blocks.DIRT_PATH || block == Blocks.MUD) {
                //Soil,Clay
                f /= 30.0F;
//                if((Object)this instanceof PlayerEntity)((PlayerEntity)(Object)this).damage(DamageSource.CACTUS,0.0001F);
            } else if (block == Reg.ICEBOX || block == Reg.DRYING_RACK) f = 0.3F;
            else f = -1.0F;
        }
        if ((block instanceof TorchBlock) || state.isIn(BlockTags.FLOWERS)) f = 999999.0F;
        this.staminaManager.add(-0.00015, this);
        this.staminaManager.pauseRestoring();
        cir.setReturnValue(f);
        cir.cancel();
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


    @SuppressWarnings("all")
    @Inject(method = "eatFood", at = @At("HEAD"))
    public void eatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if ((Object) this instanceof ServerPlayerEntity player && !stack.isEmpty()) {
            Item item = stack.getItem();
            String name = item.getTranslationKey();
            FoodComponent food = item.getFoodComponent();
            EntityHelper.checkOvereaten(player, false);
            if (food != null) {
                if (food.isMeat() || name.contains("egg"))
                    this.nutritionManager.addVegetable(-0.08);
                else if (name.contains("kelp")) this.nutritionManager.addVegetable(0.15);
                else if (name.contains("berries") || name.contains("berry")) this.nutritionManager.addVegetable(0.17);
                else if (name.contains("apple") || name.contains("orange") || name.contains("carrot") || name.contains("cactus") || name.contains("melon") || name.contains("potherb") || name.contains("shoot") || name.contains("salad") || name.contains("fruit"))
                    this.nutritionManager.addVegetable(0.3);
                int freshLevel = RotHelper.addDebuff(world, player, stack);
                if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) this.sanityManager.add(1.0);
                else if (item == Items.KELP) this.sanityManager.add(-0.04);
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
                        this.sanityManager.add(0.01);
                }
                if (item == Items.WHEAT || item == Items.SUGAR || item == Items.SUGAR_CANE || item == Reg.POTHERB || item == Reg.ROASTED_SEEDS)
                    this.hungerManager.setFoodLevel(Math.min(this.hungerManager.getFoodLevel() + 1, 20));
                else if ((name.contains("seeds") && food.getHunger() == 0) || item == Reg.COOKED_SWEET_BERRIES || item == Reg.ROT || item == Items.KELP)
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
            this.thirstManager.add(-exhaustion / 140);//70 originally
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

    @Inject(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;incrementStat(Lnet/minecraft/util/Identifier;)V"), cancellable = true)
    public void jump(@NotNull CallbackInfo ci) {
        float rate = this.isSprinting() ? 3.0F : 1.0F;
        this.staminaManager.pauseRestoring();
        this.addExhaustion(0.025F * rate);
        this.staminaManager.pauseRestoring();
        this.staminaManager.add(-0.002 * rate, this);
        ci.cancel();
    }

    @Inject(method = "attack", at = @At("TAIL"))
    public void attack(Entity target, CallbackInfo ci) {
        this.staminaManager.pauseRestoring(50);
        this.staminaManager.add(-0.01, this);
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
        if (this.sanityManager.getMonsterWitnessingTicks() > 0) {
            this.sanityManager.add(-0.00003);
            this.sanityManager.setMonsterWitnessingTicks(this.sanityManager.getMonsterWitnessingTicks() - 1);
        }
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
                        if (this.isSprinting()) this.staminaManager.add(-0.0007, this);
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
                    this.staminaManager.add(0.0045, this);
                else if (this.getThirstManager().get() > 0.3 && this.getHungerManager().getFoodLevel() > 6)
                    this.staminaManager.add(0.0025, this);
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

}
