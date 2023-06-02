package com.hcs.mixin.entity.player;

import com.hcs.main.Reg;
import com.hcs.main.helper.DigRestrictHelper;
import com.hcs.main.helper.EntityHelper;
import com.hcs.main.helper.RotHelper;
import com.hcs.main.manager.*;
import com.hcs.misc.HcsEffects;
import com.hcs.misc.accessor.StatAccessor;
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
import net.minecraft.entity.player.PlayerAbilities;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static com.hcs.main.helper.DigRestrictHelper.canBreak;
import static com.hcs.misc.network.ServerS2C.floatToInt;

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
    @Final
    private PlayerAbilities abilities;

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
    ThirstManager thirstManager = new ThirstManager();
    StaminaManager staminaManager = new StaminaManager();
    TemperatureManager temperatureManager = new TemperatureManager();
    StatusManager statusManager = new StatusManager();
    SanityManager sanityManager = new SanityManager();

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


    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo info) {
        if (!this.world.isClient()) {
            this.thirstManager.set(nbt.contains(ThirstManager.THIRST_NBT, NbtElement.FLOAT_TYPE) ? nbt.getFloat(ThirstManager.THIRST_NBT) : 1.0F);
            this.thirstManager.setSaturation(nbt.contains(ThirstManager.THIRST_SATURATION_NBT, NbtElement.FLOAT_TYPE) ? nbt.getFloat(ThirstManager.THIRST_SATURATION_NBT) : 0.2F);
            this.staminaManager.set(nbt.contains(StaminaManager.STAMINA_NBT, NbtElement.FLOAT_TYPE) ? nbt.getFloat(StaminaManager.STAMINA_NBT) : 1.0F);
            this.temperatureManager.set(nbt.contains(TemperatureManager.TEMPERATURE_NBT, NbtElement.FLOAT_TYPE) ? nbt.getFloat(TemperatureManager.TEMPERATURE_NBT) : 0.5F);
            this.temperatureManager.setSaturation(nbt.contains(TemperatureManager.TEMPERATURE_SATURATION_NBT, NbtElement.FLOAT_TYPE) ? nbt.getFloat(TemperatureManager.TEMPERATURE_SATURATION_NBT) : 0.0F);
            this.statusManager.setMaxExpLevelReached(nbt.contains(StatusManager.MAX_LVL_NBT, NbtElement.INT_TYPE) ? nbt.getInt(StatusManager.MAX_LVL_NBT) : 0);
            this.sanityManager.set(nbt.contains(SanityManager.SANITY_NBT, NbtElement.FLOAT_TYPE) ? nbt.getFloat(SanityManager.SANITY_NBT) : 1.0F);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo info) {
        if (!this.world.isClient()) {
            nbt.putFloat(ThirstManager.THIRST_NBT, this.thirstManager.get());
            nbt.putFloat(ThirstManager.THIRST_SATURATION_NBT, this.thirstManager.getSaturation());
            nbt.putFloat(StaminaManager.STAMINA_NBT, this.staminaManager.get());
            nbt.putFloat(TemperatureManager.TEMPERATURE_NBT, this.temperatureManager.get());
            nbt.putFloat(TemperatureManager.TEMPERATURE_SATURATION_NBT, this.temperatureManager.getSaturation());
            nbt.putInt(StatusManager.MAX_LVL_NBT, this.statusManager.getMaxExpLevelReached());
            nbt.putFloat(SanityManager.SANITY_NBT, this.sanityManager.get());
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
        if (this.hasStatusEffect(HcsEffects.EXHAUSTED)) f *= floatToInt(this.staminaManager.get()) <= 10 ? 0.05F : 0.7F;
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
                /*
                if((Object)this instanceof PlayerEntity){
                    ((PlayerEntity)(Object)this).damage(DamageSource.CACTUS,0.0001F);
                }
                */
            } else if (block == Reg.ICEBOX || block == Reg.DRYING_RACK) f = 0.3F;
            else f = -1.0F;
        }
        if ((block instanceof TorchBlock) || state.isIn(BlockTags.FLOWERS)) f = 999999.0F;
        this.staminaManager.add(-0.00015F, this);
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


    @Inject(method = "eatFood", at = @At("HEAD"))
    public void eatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if ((Object) this instanceof ServerPlayerEntity player && !stack.isEmpty()) {
            Item item = stack.getItem();
            String name = item.getTranslationKey();
            FoodComponent food = item.getFoodComponent();
            EntityHelper.checkOvereaten(player, false);
            if (food != null) {
                RotHelper.addDebuff(world, player, stack);
                if (item == Items.WHEAT || item == Items.SUGAR || item == Items.SUGAR_CANE || item == Reg.POTHERB || item == Reg.ROASTED_SEEDS)
                    this.hungerManager.setFoodLevel(Math.min(this.hungerManager.getFoodLevel() + 1, 20));
                else if ((name.contains("seeds") && food.getHunger() == 0) || item == Reg.COOKED_SWEET_BERRIES || item == Reg.ROT)
                    EntityHelper.addDecimalFoodLevel(player, 0.4F, false);
                if (!name.contains("dried") && !name.contains("jerky") && !name.contains("seeds") && item != Items.COOKIE && item != Items.BREAD && item != Items.SUGAR) {
                    if (name.contains("stew") || name.contains("soup"))
                        this.thirstManager.add(item == Items.MUSHROOM_STEW ? 0.06F : 0.2F);
                    else if (item == Items.MELON_SLICE || item == Items.APPLE || item == Reg.ORANGE || item == Items.SUGAR_CANE || item == Reg.CACTUS_FLESH)
                        this.thirstManager.addDirectly(0.05F);
                    else if (item == Items.CARROT || item == Items.POTATO || item == Reg.PUMPKIN_SLICE || item == Reg.PETALS_SALAD)
                        this.thirstManager.addDirectly(0.03F);
                    else if (food.isMeat() || name.contains("berries") || item == Reg.COOKED_CACTUS_FLESH || item == Reg.COOKED_PUMPKIN_SLICE || item == Reg.COOKED_CARROT || item == Reg.COOKED_BAMBOO_SHOOT || item == Reg.COOKED_SWEET_BERRIES)
                        this.thirstManager.addDirectly(0.02F);
                    else this.thirstManager.addDirectly(0.01F);
                }
            }
        }
    }


    @Inject(method = "addExhaustion", at = @At("TAIL"))
    public void addExhaustion(float exhaustion, CallbackInfo ci) {
        if (!this.world.isClient) {
            this.thirstManager.add(-exhaustion / 70);
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
        this.staminaManager.add(-0.002F * rate, this);
        ci.cancel();
    }

    @Inject(method = "attack", at = @At("TAIL"))
    public void attack(Entity target, CallbackInfo ci) {
        this.staminaManager.pauseRestoring(50);
        this.staminaManager.add(-0.01F, this);
        this.statusManager.setRecentAttackTicks(200);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (this.world.isClient) return;
        this.addExhaustion(0.001F);
        this.statusManager.setRecentAttackTicks(Math.max(0, this.statusManager.getRecentAttackTicks() - 1));
        this.statusManager.setRecentMiningTicks(Math.max(0, this.statusManager.getRecentMiningTicks() - 1));
        this.statusManager.setRecentHasColdWaterBagTicks(Math.max(0, this.statusManager.getRecentHasColdWaterBagTicks() - 1));
        this.statusManager.setRecentHasHotWaterBagTicks(Math.max(0, this.statusManager.getRecentHasHotWaterBagTicks() - 1));
        this.statusManager.setRecentLittleOvereatenTicks(this.hungerManager.getFoodLevel() < 20 ? 0 : Math.max(0, this.statusManager.getRecentLittleOvereatenTicks() - 1));
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
        if (!this.abilities.invulnerable) {
            if (this.getPos().distanceTo(this.staminaManager.getLastVecPos()) > 0.0001D) {
                //Player is moving this.getVelocity() and this.speed are useless
                if (!this.hasVehicle()) {
                    boolean shouldPauseRestoring = true;
                    if (this.onGround) {
                        if (this.isSprinting()) this.staminaManager.add(-0.0007F, this);
                        else if (this.isInSneakingPose()) this.staminaManager.add(-0.0001F, this);
                        else if (this.staminaManager.get() < 0.7F) {//walking while stamina < 0.7 will get a recovery
                            shouldPauseRestoring = false;
                            this.staminaManager.add(this.staminaManager.get() < 0.3F ? 0.0001F : 0.001F, this);
                        }
                    }
                    if (this.isSwimming() || this.isClimbing()) this.staminaManager.add(-0.001F, this);
                    else if (this.isTouchingWater()) this.staminaManager.add(-0.0004F, this);
                    if (shouldPauseRestoring) this.staminaManager.pauseRestoring();
                }
            } else {
                if (this.getThirstManager().get() > 0.6F && this.getHungerManager().getFoodLevel() > 12)
                    this.staminaManager.add(0.0045F, this);
                else if (this.getThirstManager().get() > 0.3F && this.getHungerManager().getFoodLevel() > 6)
                    this.staminaManager.add(0.0025F, this);
                else this.staminaManager.add(0.001F, this);
            }
            //Lose sanity in darkness
            boolean isInCavelike = this.world.getLightLevel(LightType.SKY, this.getBlockPos()) < 5 && this.world.getDimension().hasSkyLight();
            boolean isInUnpleasantDimension = !this.world.getDimension().bedWorks() || this.world.getRegistryKey() == World.NETHER;//Avoid mods conflict as sleeping in the nether is set to permissive
            if (this.world.isNight() || isInCavelike || isInUnpleasantDimension) {
                float sanDecrement = 0.00001F;
                int blockBrightness = this.world.getLightLevel(LightType.BLOCK, this.getBlockPos());
                if (blockBrightness < 2) sanDecrement = 0.00003F;
                else if (blockBrightness < 8) sanDecrement = 0.00002F;
                else if (isInCavelike) sanDecrement = 0.000014F;
                this.sanityManager.add(-sanDecrement);
            }
        }
        if (this.hasStatusEffect(StatusEffects.STRENGTH)) this.staminaManager.reset();
        this.staminaManager.setLastVecPos(this.getPos());
        this.sanityManager.updateDifference();
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
