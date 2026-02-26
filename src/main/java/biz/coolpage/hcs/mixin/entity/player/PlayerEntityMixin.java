package biz.coolpage.hcs.mixin.entity.player;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.block.torches.BurningCrudeTorchBlock;
import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.item.KnifeItem;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.IDamageSources;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.*;
import biz.coolpage.hcs.status.manager.disabled.*;
import biz.coolpage.hcs.util.ArmorHelper;
import biz.coolpage.hcs.util.DigRestrictHelper;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_AND_SERVER;
import static biz.coolpage.hcs.util.EntityHelper.toPlayer;


@Mixin(Player.class)
@SuppressWarnings({"CanBeFinal", "AddedMixinMembersNamePattern", "CommentedOutCode"})
public abstract class PlayerEntityMixin extends LivingEntity implements StatAccessor {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Shadow public abstract Inventory getInventory();
    @Shadow public abstract FoodData getFoodData();
    @Shadow public abstract boolean isInvulnerableTo(DamageSource damageSource);
    @Shadow public abstract Component getName();
    @Shadow public abstract String getScoreboardName();
    @Shadow public abstract boolean hurt(DamageSource source, float amount);
    @Shadow public abstract boolean isSpectator();
    @Shadow public abstract void remove(RemovalReason reason);
    @Shadow public abstract void awardStat(ResourceLocation stat, int amount);
    @Shadow public abstract void causeFoodExhaustion(float exhaustion);
    @Shadow public abstract void tick();
    @Shadow public abstract float getSpeed();
    @Shadow public abstract boolean isSwimming();
    @Shadow public int experienceLevel;
    @Shadow protected FoodData foodData;
    @Shadow protected boolean wasEyeInWater;
    @Shadow public abstract void setRemainingFireTicks(int fireTicks);
    @Shadow public abstract Iterable<ItemStack> getArmorSlots();

    @Unique protected StatusManager statusManager = new StatusManager();
    @Unique protected ConfigManager configManager = new ConfigManager();
    @Unique protected ThirstManager thirstManager = new ThirstManager(), disabledThirstManager = new DisabledThirstManager();
    @Unique protected StaminaManager staminaManager = new StaminaManager(), disabledStaminaManager = new DisabledStaminaManager();
    @Unique protected TemperatureManager temperatureManager = new TemperatureManager(), disabledTemperatureManager = new DisabledTemperatureManager();
    @Unique protected SanityManager sanityManager = new SanityManager(), disabledSanityManager = new DisabledSanityManager();
    @Unique protected NutritionManager nutritionManager = new NutritionManager(), disabledNutritionManager = new DisabledNutritionManager();
    @Unique protected WetnessManager wetnessManager = new WetnessManager(), disabledWetnessManager = new DisabledWetnessManager();
    @Unique protected InjuryManager injuryManager = new InjuryManager(), disabledInjuryManager = new DisabledInjuryManager();
    @Unique protected MoodManager moodManager = new MoodManager(), disabledMoodManager = new DisabledMoodManager();
    @Unique protected DiseaseManager diseaseManager = new DiseaseManager(), disabledDiseaseManger = new DisabledDiseaseManager();
    @Unique protected OxygenManager oxygenManager = new OxygenManager(), disabledOxygenManager = new DisabledOxygenManager();
