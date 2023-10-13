package biz.coolpage.hcs.util;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.item.KnifeItem;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StatusManager;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageEffects;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class EntityHelper {
    public static final double[][] FIND_NEAREST_BLOCKS = {{0, -1, 0}, {0, 1, 0}, {0, 2, 0}, {-1, 0, 0}, {-1, 1, 0}, {1, 0, 0}, {1, 1, 0}, {0, 0, 1}, {0, 1, 1}, {0, 0, -1}, {0, 1, -1}};
    public static final double ZOMBIE_SENSING_RANGE = 48.0;
    public static final float HOLDING_BLOCK_REACHING_RANGE_ADDITION = 1.0F;

    public static final Predicate<DamageSource> IS_PHYSICAL_DAMAGE = damageSource -> !damageSource.isOf(DamageTypes.DROWN) && !damageSource.isOf(DamageTypes.STARVE) && !damageSource.isOf(DamageTypes.OUT_OF_WORLD) && !damageSource.isOf(DamageTypes.MAGIC) && !damageSource.isOf(DamageTypes.INDIRECT_MAGIC) && !damageSource.isOf(DamageTypes.WITHER);
    public static final Predicate<DamageSource> IS_BURNING_DAMAGE = damageSource -> damageSource.getType().effects().equals(DamageEffects.BURNING);
    public static final Predicate<DamageSource> IS_BLEEDING_CAUSING_DAMAGE = IS_PHYSICAL_DAMAGE.and(damageSource -> !damageSource.isOf(DamageTypes.FALL)).and(IS_BURNING_DAMAGE.negate());
    public static final Predicate<PlayerEntity> IS_SURVIVAL_LIKE = player -> player != null && !player.isCreative() && !player.isSpectator();
    public static final Predicate<PlayerEntity> IS_SURVIVAL_AND_SERVER = IS_SURVIVAL_LIKE.and(player -> player.world != null && !player.world.isClient);
    public static final Predicate<PlayerEntity> IS_BAREHANDED = player -> {
        if (player == null) return false;
        ItemStack stack = player.getMainHandStack(); //Do not use getActiveItem
//        return stack.isEmpty();
        if (stack == null) return false;
        return !(stack.getItem() instanceof ToolItem) && !stack.isOf(Items.STICK) && !stack.isOf(Items.BONE) && !stack.isOf(Reg.ROCK);
    };
    public static final BiPredicate<ItemStack, ItemStack> IS_HOLDING_BLOCK = (stack1, stack2) -> {
        if (stack1 == null || stack2 == null) return false;
        boolean result = false;
        for (Item item : new Item[]{stack1.getItem(), stack2.getItem()}) {
            String name = item.getTranslationKey();
            result = result || (item instanceof BlockItem && (!RotHelper.canRot(item) || !(name.contains("seed") && (name.contains("pumpkin") || name.contains("melon")))));
        }
        return result;
    };


    /* Calculate plasma concentration according to the formula in LaTeX:
        \begin{cases}
        y=-2.5\left(\frac{x-600}{600}\right)^{2}+2.5\left\{0\le x\le600\right\}
         \\y=\frac{-2.72}{1+e^{-\frac{x-2000}{600}}}+2.74\left\{600\le x\le4200\right\}
        \end{cases}
        */
    public static final Function<Integer, Double> PLASMA_CONCENTRATION = x -> x <= 600 ? (-2.5 * Math.pow((x - 600) / 600.0, 2) + 2.5) : (-2.72 / (1 + Math.pow(Math.E, (2000 - x) / 600.0)) + 2.74);

    @Deprecated
    public static PlayerEntity thePlayer;
    @Deprecated
    public static String theNameOfClientPlayer = null;
    @Deprecated
    public static boolean canShowMoreStat = false;


    public static void dropItem(@NotNull Entity entity, double x, double y, double z, Item item, int count) {
        if (entity.world instanceof ServerWorld) {
            ItemStack stack = new ItemStack(item, count);
            ItemEntity itemEntity = new ItemEntity(entity.getWorld(), x, y, z, stack);
            entity.world.spawnEntity(itemEntity);
        }
    }

    public static void dropItem(@NotNull Entity entity, Item item, int count) {
        if (entity.world instanceof ServerWorld) {
            ItemStack stack = new ItemStack(item, count);
            ItemEntity itemEntity = new ItemEntity(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ(), stack);
            entity.world.spawnEntity(itemEntity);
        }
    }

    public static void dropItem(@NotNull Entity entity, ItemStack stack) {
        if (entity.world instanceof ServerWorld) {
            ItemEntity itemEntity = new ItemEntity(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ(), stack);
            entity.world.spawnEntity(itemEntity);
        }
    }

    public static void dropItem(@NotNull Entity entity, @NotNull Item item) {
        dropItem(entity, new ItemStack(item));
    }

    public static void dropItem(World world, BlockPos pos, ItemStack stack) {
        if (world instanceof ServerWorld) {
            ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            world.spawnEntity(itemEntity);
        }
    }

    public static void dropItem(World world, BlockPos pos, Item item) {
        dropItem(world, pos, new ItemStack(item));
    }

    @Deprecated
    public static void msg(@NotNull PlayerEntity player, String text, Boolean isTipMessage) {
        player.sendMessage(MutableText.of(new LiteralTextContent(text)), isTipMessage);
    }

    public static void msgById(@Nullable Entity entity, String id) {
        if (entity instanceof PlayerEntity player) msgById(player, id, true);
    }

    public static void msgById(@NotNull PlayerEntity player, String id, Boolean isTipMessage) {
        player.sendMessage(Text.translatable(id), isTipMessage);
    }

    public static BlockHitResult rayCast(@NotNull World world, @NotNull Entity entity, RaycastContext.FluidHandling fluidHandling, double maxDistance) {
        float f = entity.getPitch();
        float g = entity.getYaw();
        Vec3d vec3d = entity.getEyePos();
        float h = MathHelper.cos(-g * ((float) Math.PI / 180) - (float) Math.PI);
        float i = MathHelper.sin(-g * ((float) Math.PI / 180) - (float) Math.PI);
        float j = -MathHelper.cos(-f * ((float) Math.PI / 180));
        float k = MathHelper.sin(-f * ((float) Math.PI / 180));
        float l = i * j;
        float n = h * j;
        Vec3d vec3d2 = vec3d.add((double) l * maxDistance, (double) k * maxDistance, (double) n * maxDistance);
        return world.raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.OUTLINE, fluidHandling, entity));
    }

    /*
    Teleport methods come from Bountiful Baubles mod by CursedFlames
    https://github.com/CursedFlames/BountifulBaubles/blob/MC_1.16_rewrite/common/src/main/java/cursedflames/bountifulbaubles/common/util/Teleport.java
    I can't getOutput in touch with the author.
    In case of infringement, please contact me to delete: mc1mc@qq.com
    */

    public static boolean canDoTeleport(@NotNull World world, PlayerEntity player, boolean allowInterdimensional) {
        // We have no way to check client-side.
        if (world.isClient) return true;
        RegistryKey<World> spawnDim = ((ServerPlayerEntity) player).getSpawnPointDimension();
        return world.getRegistryKey() == spawnDim || allowInterdimensional;
    }

    public static void teleportPlayerToSpawn(@NotNull World currentWorld, PlayerEntity player, boolean allowInterdimensional) {
        if (currentWorld.isClient) return;
        if (!canDoTeleport(currentWorld, player, allowInterdimensional)) return;
        RegistryKey<World> spawnPointDimension = ((ServerPlayerEntity) player).getSpawnPointDimension();
        World targetWorld = currentWorld;
        if (targetWorld.getRegistryKey() != spawnPointDimension) {
            targetWorld = Objects.requireNonNull(targetWorld.getServer()).getWorld(spawnPointDimension);
        }
        player.stopRiding();
        if (player.isSleeping()) {
            player.wakeUp();
        }
        if (targetWorld != null) {
            BlockPos spawnPoint = ((ServerPlayerEntity) player).getSpawnPointPosition();
            if (spawnPoint != null) {
                boolean force = false; //player.isSpawnForced(dim);
                Optional<Vec3d> optional =
                        PlayerEntity.findRespawnPosition((ServerWorld) targetWorld, spawnPoint,
                                ((ServerPlayerEntity) player).getSpawnAngle(), force, true);
                if (optional.isPresent()) {
                    Vec3d pos = optional.get();
                    doTeleport(player, currentWorld, targetWorld, pos.getX(), pos.getY(), pos.getZ());
                    return;
                }
            }
            spawnPoint = targetWorld.getSpawnPos();

            if (spawnPoint != null) {
                doTeleport(player, currentWorld, targetWorld, spawnPoint.getX() + 0.5, spawnPoint.getY(),
                        spawnPoint.getZ() + 0.5);
            }
        }
    }

    private static void doTeleport(@NotNull PlayerEntity player, World origin, @NotNull World target, double x, double y, double z) {
        target.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f);
        if (origin != target) {
            ((ServerChunkManager) target.getChunkManager()).addTicket(
                    ChunkTicketType.POST_TELEPORT,
                    new ChunkPos(new BlockPos((int) x, (int) y, (int) z)),
                    1, player.getId());
            ((ServerPlayerEntity) player).teleport(
                    (ServerWorld) target, x, y, z, player.getYaw(), player.getPitch());
        } else {
            player.requestTeleport(x, y, z);
        }
        if (player.fallDistance > 0.0F) {
            player.fallDistance = 0.0F;
        }
        target.playSound(null, x, y, z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1f, 1f);
    }

    public static BlockPos getPosBackward(Entity entity) {
        if (entity == null) {
            Reg.LOGGER.error("EntityHelper/getPosBackward;entity==null");
            return BlockPos.ORIGIN;
        }
        BlockPos entityPos = entity.getBlockPos();
        return switch (entity.getHorizontalFacing().getOpposite()) {
            case EAST -> entityPos.east();
            case SOUTH -> entityPos.south();
            case WEST -> entityPos.west();
            default -> entityPos.north();
        };
    }

    public static float getReachRangeAddition(LivingEntity entity) {
        if (entity == null) return 0.0F;
        ItemStack mainHandStack = entity.getMainHandStack(), offHandStack = entity.getOffHandStack();
        if (mainHandStack == null || offHandStack == null) return 0.0F;
        return getReachRangeAddition(mainHandStack, offHandStack);
    }

    public static float getReachRangeAddition(@NotNull ItemStack mainHandStack) {
        return getReachRangeAddition(mainHandStack, null);
    }

    public static float getReachRangeAddition(@NotNull ItemStack mainHandStack, ItemStack offHandStack) {
        float dist = 0.0F;
        Item item = mainHandStack.getItem();
        String name = item.getTranslationKey();
        if (name.contains("knife") || name.contains("hatchet") || name.contains("_cone") || (item instanceof ShearsItem) || (item instanceof FlintAndSteelItem))
            dist += 0.75F;
        else if ((name.contains("bone") && !mainHandStack.isOf(Items.BONE_MEAL)) || name.contains("rod") || item == Items.STICK)
            dist += 1.0F;
        else if (item instanceof RangedWeaponItem || (item instanceof SwordItem swordItem && swordItem.getMaterial() == ToolMaterials.WOOD))
            dist += 1.5F;
        else if (IS_HOLDING_BLOCK.test(mainHandStack, offHandStack))
            return HOLDING_BLOCK_REACHING_RANGE_ADDITION;
        else if (name.contains("spear") || (item instanceof TridentItem)) dist += 2.0F;
        else if ((item instanceof ShovelItem) || (item instanceof PickaxeItem) || (item instanceof AxeItem) || (item instanceof SwordItem) || (item instanceof HoeItem))
            dist += 2.0F;
        else if (mainHandStack.isEnchantable() && !(item instanceof ArmorItem)) dist += 1.5F;
        return dist;
    }

    public static void addHcsDebuff(Object playerObj, StatusEffect effect) {
        addHcsDebuff(playerObj, effect, 0);
    }

    //Also see at StatusEffectUtilMixin, AbstractInventoryScreenMixin
    public static void addHcsDebuff(Object playerObj, StatusEffect effect, int amplifier) {
        if (playerObj instanceof ServerPlayerEntity player) {
            player.addStatusEffect(new StatusEffectInstance(effect, 5, amplifier, false, false, true) {
                @Override
                public int getDuration() {
                    //Disable icon twinkling for short duration
                    return 210;
                }
            });
        }
    }

    public static void mixinToolsPostMine(ItemStack stack, BlockState state, LivingEntity miner, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || state == null || miner == null || cir == null) return;
        Block block = state.getBlock();
        if (state.isIn(BlockTags.FLOWERS) || block instanceof TorchBlock || (stack.getItem() instanceof SwordItem swordItem && swordItem.getMaterial() == ToolMaterials.WOOD && (block instanceof FernBlock || block instanceof TallPlantBlock)))
            cir.setReturnValue(true);
    }

    public static void addDecimalFoodLevel(ServerPlayerEntity player, float foodLevel, boolean hasSaturation) {
        if (player == null) return;
        HungerManager hungerManager = player.getHungerManager();
        if (hungerManager == null) return;
        if (hasSaturation) hungerManager.setSaturationLevel(hungerManager.getSaturationLevel() + foodLevel);
        int amountIntPart = (int) foodLevel;
        float amountDecPart = foodLevel - amountIntPart;
        hungerManager.setFoodLevel(hungerManager.getFoodLevel() + amountIntPart);
        if (amountDecPart > 0.0F) {
            StatusManager statusManager = ((StatAccessor) player).getStatusManager();
            float exhaustionIncrement = -amountDecPart * 4.0F;
            float exhaustion = hungerManager.getExhaustion();
            float exhaustionSum = exhaustion + exhaustionIncrement;
            if (exhaustionSum < 0.0F) {
                int foodLvlAdded = hungerManager.getFoodLevel() + 1;
                if (foodLvlAdded >= 21)
                    hungerManager.setSaturationLevel(hungerManager.getSaturationLevel() - exhaustionIncrement);
                else hungerManager.setFoodLevel(foodLvlAdded);
                hungerManager.setExhaustion(4.0F + exhaustionSum);
            } else if (exhaustionSum > 4.0F) {
                hungerManager.setFoodLevel(Math.max(0, hungerManager.getFoodLevel() - 1));
                hungerManager.setExhaustion(4.0F - (exhaustionSum - 4.0F));
            } else hungerManager.setExhaustion(exhaustionSum);
            statusManager.setHasDecimalFoodLevel(hungerManager.getFoodLevel() < 20);
        }
    }

    public static void checkOvereaten(@NotNull ServerPlayerEntity player, boolean isDrink) {
        int hunger = player.getHungerManager().getFoodLevel();
        double thirst = ((StatAccessor) player).getThirstManager().get();
        if ((isDrink && (hunger >= 20 || thirst > 0.99)) || (!isDrink && hunger >= 20)) {
            StatusManager statusManager = ((StatAccessor) player).getStatusManager();
            statusManager.setHasDecimalFoodLevel(false);
            if (statusManager.getRecentLittleOvereatenTicks() > 0) {
                if (player.hasStatusEffect(HcsEffects.OVEREATEN))
                    ((StatAccessor) player).getInjuryManager().addRawPain(0.5);
                player.addStatusEffect(new StatusEffectInstance(HcsEffects.OVEREATEN, 600, 0, false, false, true));
            } else statusManager.setRecentLittleOvereatenTicks(1200);
        }
    }

    public static LivingEntity getHallucinationEntityForPlayer(World world, LivingEntity originalEntity) {
        if (originalEntity instanceof PlayerEntity player) {
            if (world != null && player.hasStatusEffect(HcsEffects.INSANITY) && ((StatAccessor) player).getSanityManager().get() < 0.05) {
                LivingEntity hallucinationEntity = new SkeletonEntity(EntityType.SKELETON, world);
                hallucinationEntity.setStackInHand(Hand.MAIN_HAND, player.getMainHandStack());
                hallucinationEntity.setStackInHand(Hand.OFF_HAND, player.getOffHandStack());
                return hallucinationEntity;
            }
        }
        return originalEntity;
    }

    public static ActionResult dropBark(ItemUsageContext context) {
        if (context == null) return ActionResult.PASS;
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        BlockPos userPos = context.getPlayer() == null ? pos : context.getPlayer().getBlockPos();
        Optional<BlockState> strippedState;
        if (Items.IRON_AXE instanceof AxeItem axeItem) {
            if ((strippedState = axeItem.getStrippedState(world.getBlockState(pos))).isPresent() && IS_SURVIVAL_LIKE.test(player)) {
                if (WorldHelper.enhancedIsWaterNearby(world, pos.down()) && Math.random() < 0.5)
                    EntityHelper.dropItem(world, userPos, Reg.WILLOW_BARK);
                else EntityHelper.dropItem(world, userPos, Reg.BARK);
                if (stack.getItem() instanceof KnifeItem) {
                    if (player instanceof ServerPlayerEntity serverPlayerEntity)
                        Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayerEntity, pos, stack);
                    world.setBlockState(pos, strippedState.get(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                    world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, strippedState.get()));
                    if (player != null) stack.damage(1, player, p -> p.sendToolBreakStatus(context.getHand()));
                    return ActionResult.success(world.isClient);
                }
            }
        } else
            Reg.LOGGER.error("EntityHelper/dropBark: Cannot call getStrippedState(), as IRON_AXE is not an instance of AxeItem!");
        return ActionResult.PASS;
    }

    public static int getEffectAmplifier(@Nullable LivingEntity entity, @Nullable StatusEffect effect) {
        // -1 indicates no such effect
        if (entity == null || effect == null) return -1;
        if (entity.hasStatusEffect(effect) && entity.getStatusEffect(effect) != null)
            return Objects.requireNonNull(entity.getStatusEffect(effect)).getAmplifier();
        return -1;
    }

    public static boolean isExistent(@Nullable LivingEntity... entities) {
        if (entities.length == 0) return false;
        boolean result = true;
        for (LivingEntity entity : entities) {
            if (entity == null) return false;
            result = result && entity.isAlive();
        }
        return result;
    }

    public static @Nullable PlayerEntity toPlayer(@Nullable Entity entity) {
        if (entity instanceof PlayerEntity player) return player;
        return null;
    }

    public static boolean isPlayerStaring(LivingEntity stared, PlayerEntity player) { //From EndermanEntity
        if (!isExistent(player, stared)) return false;
        ItemStack itemStack = player.getInventory().armor.get(3);
        if (itemStack.isOf(Blocks.CARVED_PUMPKIN.asItem())) return false;
        Vec3d vec3d = player.getRotationVec(1.0f).normalize();
        Vec3d vec3d2 = new Vec3d(stared.getX() - player.getX(), stared.getEyeY() - player.getEyeY(), stared.getZ() - player.getZ());
        double d = vec3d2.length();
        double e = vec3d.dotProduct(vec3d2.normalize());
        if (e > 1.0 - 0.025 / d) return player.canSee(stared);
        return false;
    }

}
