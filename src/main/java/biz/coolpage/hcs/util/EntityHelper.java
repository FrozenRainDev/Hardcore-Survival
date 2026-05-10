package biz.coolpage.hcs.util;

import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StatusManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class EntityHelper {
    public static final double[][] FIND_NEAREST_BLOCKS = {{0, -1, 0}, {0, 1, 0}, {0, 2, 0}, {-1, 0, 0}, {-1, 1, 0}, {1, 0, 0}, {1, 1, 0}, {0, 0, 1}, {0, 1, 1}, {0, 0, -1}, {0, 1, -1}};
    public static double ZOMBIE_SENSING_RANGE = 40.0;
    public static final float HOLDING_BLOCK_REACHING_RANGE_ADDITION = 1.0F;

    // Note: DamageTypes need to be adjusted according to the actual damage type tags
    public static final Predicate<DamageSource> IS_PHYSICAL_DAMAGE = damageSource ->
            !damageSource.is(net.minecraft.tags.DamageTypeTags.IS_DROWNING) &&
                    !damageSource.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR) &&
                    !damageSource.getMsgId().contains("starve") &&
                    !damageSource.getMsgId().contains("magic") &&
                    !damageSource.getMsgId().contains("wither");

    public static final Predicate<DamageSource> IS_BURNING_DAMAGE = damageSource ->
            damageSource.is(net.minecraft.tags.DamageTypeTags.IS_FIRE);

    public static final Predicate<DamageSource> IS_BLEEDING_CAUSING_DAMAGE = IS_PHYSICAL_DAMAGE
            .and(damageSource -> !damageSource.is(net.minecraft.tags.DamageTypeTags.IS_FALL))
            .and(IS_BURNING_DAMAGE.negate())
            .and(source -> !(source.getEntity() instanceof Cow));

    public static final Predicate<Player> IS_SURVIVAL_LIKE = player ->
            player != null && !player.isCreative() && !player.isSpectator();

    public static final Predicate<Player> IS_SURVIVAL_AND_SERVER = IS_SURVIVAL_LIKE
            .and(player -> player.level() != null && !player.level().isClientSide);

    public static final Predicate<Player> IS_BAREHANDED = player -> {
        if (player == null) return false;
        ItemStack stack = player.getMainHandItem();
        if (stack == null || stack.isEmpty()) return true;
        return !(stack.getItem() instanceof TieredItem) &&
                !stack.is(Items.STICK) &&
                !stack.is(Items.BONE) &&
                !stack.is(Items.FLINT);
    };

    public static final BiPredicate<ItemStack, ItemStack> IS_HOLDING_BLOCK = (stack1, stack2) -> {
        if (stack1 == null || stack2 == null) return false;
        boolean result = false;
        for (Item item : new Item[]{stack1.getItem(), stack2.getItem()}) {
            String name = item.getDescriptionId();
            result = result || (item instanceof BlockItem &&
                    (!RotHelper.canRot(item) || !(name.contains("seed") && (name.contains("pumpkin") || name.contains("melon")))));
        }
        return result;
    };

    public static final BiPredicate<Entity, DamageSource> SHOULD_DROP_AFTER_DEATH = (victim, source) -> {
        if (source == null || !(source.getEntity() instanceof Enemy)) return false;
        return victim instanceof Animal;
    };

    /* Calculate plasma concentration according to the formula in LaTeX:
        \begin{cases}
        y=-2.5\left(\frac{x-600}{600}\right)^{2}+2.5\left\{0\le x\le600\right\}
         \\y=\frac{-2.72}{1+e^{-\frac{x-2000}{600}}}+2.74\left\{600\le x\le4200\right\}
        \end{cases}
        */
    public static final Function<Integer, Double> PLASMA_CONCENTRATION = x ->
            x <= 600 ? (-2.5 * Math.pow((x - 600) / 600.0, 2) + 2.5) :
                    (-2.72 / (1 + Math.pow(Math.E, (2000 - x) / 600.0)) + 2.74);

    // todo EntityDataAccessor 需要在实体类中注册 而不是mixin
    public static final EntityDataAccessor<Long> MILKED_TIME = SynchedEntityData.defineId(Cow.class, EntityDataSerializers.LONG);

    public static void dropItem(@NotNull Entity entity, double x, double y, double z, Item item, int count) {
        if (entity.level() instanceof ServerLevel) {
            ItemStack stack = new ItemStack(item, count);
            ItemEntity itemEntity = new ItemEntity(entity.level(), x, y, z, stack);
            entity.level().addFreshEntity(itemEntity);
        }
    }

    public static void dropItem(@NotNull Entity entity, Item item, int count) {
        if (entity.level() instanceof ServerLevel) {
            ItemStack stack = new ItemStack(item, count);
            ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack);
            entity.level().addFreshEntity(itemEntity);
        }
    }

    public static void dropItem(@NotNull Entity entity, ItemStack stack) {
        if (entity.level() instanceof ServerLevel) {
            ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack);
            entity.level().addFreshEntity(itemEntity);
        }
    }

    public static void dropItem(Entity entity, Item item) {
        if (entity != null && item != null) dropItem(entity, new ItemStack(item));
    }

    public static void dropItem(Level level, BlockPos pos, ItemStack stack) {
        if (level instanceof ServerLevel) {
            ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            level.addFreshEntity(itemEntity);
        }
    }

    public static void dropItem(Level level, BlockPos pos, Item item) {
        dropItem(level, pos, new ItemStack(item));
    }

    @Deprecated
    public static void msg(@NotNull Player player, String text, Boolean isTipMessage) {
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(text), isTipMessage);
    }

    public static void msgById(@Nullable Entity entity, String id) {
        if (entity instanceof Player player) msgById(player, id, true);
    }

    public static void msgById(@Nullable Player player, String id, Boolean isTipMessage) {
        applyNullable(player, p -> p.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(id), isTipMessage));
    }

    public static @NotNull BlockHitResult rayCast(@NotNull Level level, @NotNull Entity entity,
                                                  ClipContext.Fluid fluidHandling, double maxDistance) {
        float pitch = entity.getXRot();
        float yaw = entity.getYRot();
        Vec3 eyePos = entity.getEyePosition();
        float f = (float) Math.cos(-yaw * ((float) Math.PI / 180F) - (float) Math.PI);
        float g = (float) Math.sin(-yaw * ((float) Math.PI / 180F) - (float) Math.PI);
        float h = (float) -Math.cos(-pitch * ((float) Math.PI / 180F));
        float i = (float) Math.sin(-pitch * ((float) Math.PI / 180F));
        float j = g * h;
        float k = f * h;
        Vec3 targetVec = eyePos.add((double) j * maxDistance, (double) i * maxDistance, (double) k * maxDistance);
        return level.clip(new ClipContext(eyePos, targetVec, ClipContext.Block.OUTLINE, fluidHandling, entity));
    }

    public static boolean canDoTeleport(@NotNull Level level, Player player, boolean allowInterdimensional) {
        if (level.isClientSide) return true;
        var spawnDim = ((ServerPlayer) player).getRespawnDimension();
        return level.dimension() == spawnDim || allowInterdimensional;
    }

    public static void teleportPlayerToSpawn(@NotNull Level level, Player player, boolean allowInterdimensional) {
        if (level.isClientSide) return;
        if (!canDoTeleport(level, player, allowInterdimensional)) {
            EntityHelper.msgById(player, "tip.hcsurvival.return_failed_interdimention");
            return;
        }
        player.removeEffect(HcsEffects.RETURN.get());
        var spawnPointDimension = ((ServerPlayer) player).getRespawnDimension();
        Level targetLevel = level;
        if (targetLevel.dimension() != spawnPointDimension) {
            targetLevel = Objects.requireNonNull(targetLevel.getServer()).getLevel(spawnPointDimension);
        }
        player.stopRiding();
        if (player.isSleeping()) {
            player.stopSleepInBed(true, true);
        }
        if (targetLevel != null) {
            BlockPos spawnPoint = ((ServerPlayer) player).getRespawnPosition();
            if (spawnPoint != null) {
                boolean force = false;
                Optional<Vec3> optional = Player.findRespawnPositionAndUseSpawnBlock(
                        (ServerLevel) targetLevel, spawnPoint,
                        ((ServerPlayer) player).getRespawnAngle(), force, true);
                if (optional.isPresent()) {
                    Vec3 pos = optional.get();
                    doTeleport(player, level, targetLevel, pos.x, pos.y, pos.z);
                    return;
                }
            }
            spawnPoint = targetLevel.getSharedSpawnPos();
            if (spawnPoint != null) {
                doTeleport(player, level, targetLevel, spawnPoint.getX() + 0.5,
                        spawnPoint.getY(), spawnPoint.getZ() + 0.5);
            }
        }
    }

    private static void doTeleport(@NotNull Player player, Level origin, @NotNull Level target,
                                   double x, double y, double z) {
        target.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 1f);
        if (origin != target) {
            ((ServerPlayer) player).teleportTo((ServerLevel) target, x, y, z,
                    player.getYRot(), player.getXRot());
        } else {
            player.teleportTo(x, y, z);
        }
        if (player.fallDistance > 0.0F) {
            player.fallDistance = 0.0F;
        }
        target.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 1f);
    }

    public static BlockPos getPosFacing(Entity entity, boolean isBackward) {
        if (entity == null) {
            // Reg.LOGGER.error("EntityHelper/getPosFacing;entity==null");
            return BlockPos.ZERO;
        }
        BlockPos entityPos = entity.blockPosition();
        Direction facing = entity.getDirection();
        return switch (isBackward ? facing.getOpposite() : facing) {
            case EAST -> entityPos.east();
            case SOUTH -> entityPos.south();
            case WEST -> entityPos.west();
            default -> entityPos.north();
        };
    }

    public static float getReachRangeAddition(LivingEntity entity) {
        if (entity == null) return 0.0F;
        ItemStack mainHandStack = entity.getMainHandItem();
        ItemStack offHandStack = entity.getOffhandItem();
        if (mainHandStack == null || offHandStack == null) return 0.0F;
        return getReachRangeAddition(mainHandStack, offHandStack);
    }

    public static float getReachRangeAddition(@NotNull ItemStack mainHandStack) {
        return getReachRangeAddition(mainHandStack, null);
    }

    public static float getReachRangeAddition(@NotNull ItemStack mainHandStack, ItemStack offHandStack) {
        float dist = 0.0F, distAddHoldingBlock = 0.0F;
        Item item = mainHandStack.getItem();
        String name = item.getDescriptionId();
        if (name.contains("knife") || name.contains("hatchet") || name.contains("_cone") ||
                (item instanceof ShearsItem) || (item instanceof FlintAndSteelItem))
            dist += 0.75F;
        else if ((name.contains("bone") && !mainHandStack.is(Items.BONE_MEAL)) ||
                name.contains("rod") || item == Items.STICK)
            dist += 1.0F;
        else if (item instanceof ProjectileWeaponItem ||
                (item instanceof SwordItem swordItem && swordItem.getTier() == Tiers.WOOD))
            dist += 1.5F;
        else if (item instanceof ShovelItem || item instanceof PickaxeItem ||
                item instanceof AxeItem || item instanceof HoeItem)
            dist += 2.0F;
        else if (name.contains("spear") || item instanceof TridentItem || item instanceof SwordItem)
            dist += 2.5F;
        else if (mainHandStack.isEnchantable() && item != Items.BOOK && !(item instanceof ArmorItem))
            dist += 1.5F;
        if (IS_HOLDING_BLOCK.test(mainHandStack, offHandStack))
            distAddHoldingBlock += HOLDING_BLOCK_REACHING_RANGE_ADDITION;
        return Math.max(dist, distAddHoldingBlock);
    }

    public static void addHcsDebuff(Object playerObj, MobEffect effect) {
        addHcsDebuff(playerObj, effect, 0);
    }

    public static void addHcsDebuff(Object playerObj, MobEffect effect, int amplifier) {
        if (playerObj instanceof ServerPlayer player) {
            // Set ambient to 'false' (4th parameter) so vanilla draws the default grey background box.
            // Duration is kept extremely short (2 ticks).
            // The flashing issue will be intercepted and fixed in HcsMobEffect client extensions.
            player.addEffect(new MobEffectInstance(effect, 2, amplifier, false, false, true));
        }
    }

    public static void addDecimalFoodLevel(ServerPlayer player, float foodLevel, boolean hasSaturation) {
        if (player == null) return;
        FoodData foodData = player.getFoodData();
        if (foodData == null) return;
        if (hasSaturation) foodData.setSaturation(foodData.getSaturationLevel() + foodLevel);
        int amountIntPart = (int) foodLevel;
        float amountDecPart = foodLevel - amountIntPart;
        foodData.setFoodLevel(foodData.getFoodLevel() + amountIntPart);
        if (amountDecPart > 0.0F) {
            StatusManager statusManager = ((StatAccessor) player).getStatusManager();
            float exhaustionIncrement = -amountDecPart * 4.0F;
            float exhaustion = foodData.getExhaustionLevel();
            float exhaustionSum = exhaustion + exhaustionIncrement;
            if (exhaustionSum < 0.0F) {
                int foodLvlAdded = foodData.getFoodLevel() + 1;
                if (foodLvlAdded >= 21)
                    foodData.setSaturation(foodData.getSaturationLevel() - exhaustionIncrement);
                else foodData.setFoodLevel(foodLvlAdded);
                foodData.setExhaustion(4.0F + exhaustionSum);
            } else if (exhaustionSum > 4.0F) {
                foodData.setFoodLevel(Math.max(0, foodData.getFoodLevel() - 1));
                foodData.setExhaustion(4.0F - (exhaustionSum - 4.0F));
            } else foodData.setExhaustion(exhaustionSum);
            statusManager.setHasDecimalFoodLevel(foodData.getFoodLevel() < 20);
        }
    }

    public static void checkOvereaten(@NotNull ServerPlayer player, boolean isDrink) {
        int hunger = player.getFoodData().getFoodLevel();
        double thirst = ((StatAccessor) player).getThirstManager().get();
        if ((isDrink && (hunger >= 20 || thirst > 0.99)) || (!isDrink && hunger >= 20)) {
            StatusManager statusManager = ((StatAccessor) player).getStatusManager();
            statusManager.setHasDecimalFoodLevel(false);
            if (statusManager.getRecentLittleOvereatenTicks() > 0) {
                boolean hasOvereatenEffect = player.hasEffect(HcsEffects.OVEREATEN.get());
                int duration = 600;
                MobEffectInstance overeatenEffect = player.getEffect(HcsEffects.OVEREATEN.get());
                if (overeatenEffect != null) {
                    int sumDur = duration + overeatenEffect.getDuration();
                    duration = Math.min(sumDur, 1200);
                }
                player.addEffect(new MobEffectInstance(HcsEffects.OVEREATEN.get(), duration,
                        hasOvereatenEffect ? 1 : 0, false, false, true));
            } else statusManager.setRecentLittleOvereatenTicks(1200);
        }
    }

    public static LivingEntity getHallucinationEntityForPlayer(Level level, LivingEntity originalEntity) {
        if (originalEntity instanceof Player player) {
            if (level != null && player.hasEffect(HcsEffects.INSANITY.get()) &&
                    ((StatAccessor) player).getSanityManager().get() < 0.05) {
                LivingEntity hallucinationEntity = EntityType.SKELETON.create(level);
                if (hallucinationEntity != null) {
                    hallucinationEntity.setItemInHand(InteractionHand.MAIN_HAND, player.getMainHandItem());
                    hallucinationEntity.setItemInHand(InteractionHand.OFF_HAND, player.getOffhandItem());
                    return hallucinationEntity;
                }
            }
        }
        return originalEntity;
    }

    public static InteractionResult dropBark(UseOnContext context) {
        if (context == null) return InteractionResult.PASS;
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockState state = level.getBlockState(pos);
        BlockPos userPos = context.getPlayer() == null ? pos : context.getPlayer().blockPosition();
        Optional<BlockState> strippedState;

        if (Items.IRON_AXE instanceof AxeItem axeItem) {
            strippedState = Optional.ofNullable(state.getToolModifiedState(context, net.minecraftforge.common.ToolActions.AXE_STRIP, false));
            if (strippedState.isPresent() && IS_SURVIVAL_LIKE.test(player)) {
                // 需要自定义 WorldHelper 和 Reg 的引用
                // if (WorldHelper.enhancedIsWaterNearby(level, pos.below()) && Math.random() < 0.5)
                //     EntityHelper.dropItem(level, userPos, Reg.WILLOW_BARK);
                // else EntityHelper.dropItem(level, userPos, Reg.BARK);

                // if (stack.getItem() instanceof KnifeItem) { // 需要自定义 KnifeItem
                //     if (player instanceof ServerPlayer serverPlayer)
                //         // Trigger advancement
                //     level.setBlock(pos, strippedState.get(), 11);
                //     level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, strippedState.get()));
                //     if (player != null) stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
                //     return InteractionResult.sidedSuccess(level.isClientSide);
                // }
            }
        }
        return InteractionResult.PASS;
    }

    public static int getEffectAmplifier(@Nullable LivingEntity entity, @Nullable MobEffect effect) {
        if (entity == null || effect == null) return -1;
        if (entity.hasEffect(effect) && entity.getEffect(effect) != null)
            return Objects.requireNonNull(entity.getEffect(effect)).getAmplifier();
        return -1;
    }

    public static boolean isExistent(@Nullable LivingEntity... entities) {
        if (entities == null || entities.length == 0) return false;
        boolean result = true;
        for (LivingEntity entity : entities) {
            if (entity == null) return false;
            result = result && entity.isAlive();
        }
        return result;
    }

    public static @Nullable Player toPlayer(@Nullable Entity entity) {
        if (entity instanceof Player player) return player;
        return null;
    }

    public static boolean isPlayerStaring(LivingEntity stared, Player player) {
        if (!isExistent(player, stared)) return false;
        ItemStack itemStack = player.getInventory().armor.get(3);
        if (itemStack.is(Blocks.CARVED_PUMPKIN.asItem())) return false;
        Vec3 lookVec = player.getViewVector(1.0f).normalize();
        Vec3 targetVec = new Vec3(stared.getX() - player.getX(),
                stared.getEyeY() - player.getEyeY(), stared.getZ() - player.getZ());
        double distance = targetVec.length();
        double dotProduct = lookVec.dot(targetVec.normalize());
        if (dotProduct > 1.0 - 0.025 / distance) return player.hasLineOfSight(stared);
        return false;
    }

    public static boolean isLuminousBlockWorking(LivingEntity entity) {
        if (entity == null) return false;
        for (Item item : new Item[]{entity.getMainHandItem().getItem(), entity.getOffhandItem().getItem()}) {
            if (item instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (block.defaultBlockState().getLightEmission() > 0)
                    return !(block instanceof TorchBlock && entity.isUnderWater());
            }
        }
        return false;
    }

    public static List<? extends Mob> getOthersEntitiesInRange(@NotNull LivingEntity entity,
                                                               Class<? extends Mob> targetEntClass,
                                                               double rangeMultiplier) {
        AABB box = AABB.ofSize(entity.position(),
                ZOMBIE_SENSING_RANGE * rangeMultiplier * 2,
                10.0 * rangeMultiplier * 2,
                ZOMBIE_SENSING_RANGE * rangeMultiplier * 2);
        return entity.level().getEntitiesOfClass(targetEntClass, box,
                EntitySelector.NO_SPECTATORS);
    }

    public static void letEnderDragonChargeAtTheClosestPlayer(LivingEntity entity) {
        if (entity instanceof EnderDragon dragon) {
            Player player = dragon.level().getNearestPlayer(dragon, 150);
            if (player != null) {
                dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                var phase = dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER);
                if (phase != null) {
                    phase.setTarget(new Vec3(player.getX(), player.getY(), player.getZ()));
                }
            }
        }
    }

    public static void lightningStrike(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            if (entity.level() == null) return;
            LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(player.level());
            if (lightningBolt != null) {
                lightningBolt.moveTo(player.position());
                lightningBolt.setCause(player);
                player.level().addFreshEntity(lightningBolt);
            }
        }
    }

    public static boolean isInLeather(LivingEntity entity) {
        if (entity == null) return false;
        AtomicBoolean flag = new AtomicBoolean(false);
        entity.getArmorSlots().forEach(stack -> {
            if (stack.getItem() instanceof ArmorItem armor &&
                    armor.getMaterial() == ArmorMaterials.LEATHER)
                flag.set(true);
        });
        return flag.get();
    }

    public static void flyOut(@NotNull Mob attacker, @NotNull LivingEntity victim, float damage) {
        Vec3 vec1 = victim.getDeltaMovement();
        Vec3 vec2 = new Vec3(victim.getX() - attacker.getX(), 0.0, victim.getZ() - attacker.getZ());
        if (vec2.lengthSqr() > 1.0E-7) vec2 = vec2.normalize().add(vec1.multiply(0.2, 0.2, 0.2));
        victim.hurt(victim.level().damageSources().mobAttack(attacker), damage);
        victim.setDeltaMovement(vec2.x, 0.5, vec2.z);
    }
}