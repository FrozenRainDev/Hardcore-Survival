package com.hcs.main.helper;

import com.hcs.main.Reg;
import com.hcs.main.manager.StatusManager;
import com.hcs.misc.HcsEffects;
import com.hcs.misc.accessor.StatAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.*;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Optional;

public class EntityHelper {
    public static final double[][] FIND_NEAREST = {{0, -1, 0}, {0, 1, 0}, {0, 2, 0}, {-1, 0, 0}, {-1, 1, 0}, {1, 0, 0}, {1, 1, 0}, {0, 0, 1}, {0, 1, 1}, {0, 0, -1}, {0, 1, -1}};
    public static final double ZOMBIE_SENSING_RANGE = 64.0D;
    @Deprecated
    public static PlayerEntity thePlayer;
    @Deprecated
    public static String theNameOfClientPlayer = null;
    @Deprecated
    public static boolean canShowMoreStat = false;


    public static void dropItem(@NotNull Entity entity, double x, double y, double z, Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        ItemEntity itemEntity = new ItemEntity(entity.getWorld(), x, y, z, stack);
        entity.world.spawnEntity(itemEntity);
    }

    public static void dropItem(@NotNull Entity entity, Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        ItemEntity itemEntity = new ItemEntity(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ(), stack);
        entity.world.spawnEntity(itemEntity);
    }

    public static void dropItem(@NotNull Entity entity, ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ(), stack);
        entity.world.spawnEntity(itemEntity);
    }

    public static void dropItem(World world, BlockPos pos, ItemStack stack) {
        if (world instanceof ServerWorld) {
            ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            world.spawnEntity(itemEntity);
        }
    }

    public static void msg(@NotNull PlayerEntity player, String text, Boolean isTipMessage) {
        player.sendMessage(MutableText.of(new LiteralTextContent(text)), isTipMessage);
    }

    public static void msgById(@NotNull PlayerEntity player, String id, Boolean isTipMessage) {
        player.sendMessage(Text.translatable(id), isTipMessage);
    }

    public static String customNumberFormatter(String pattern, double value) {
        DecimalFormat decimalFormat = new DecimalFormat(pattern);
        return decimalFormat.format(value);
    }

    public static String customNumberFormatter(String pattern, float value) {
        return customNumberFormatter(pattern, (double) value);
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
                boolean force = false;//player.isSpawnForced(dim);
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

    @Deprecated
    public static void renderGuiQuad(@NotNull BufferBuilder buffer, int x, int y, int width, int height, int red, int green, int blue, int alpha, boolean needShader) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(x, y, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x, y + height, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y + height, 0.0).color(red, green, blue, alpha).next();
        buffer.vertex(x + width, y, 0.0).color(red, green, blue, alpha).next();
        if (needShader) BufferRenderer.drawWithGlobalProgram(buffer.end());
        else BufferRenderer.drawWithGlobalProgram(buffer.end());//never use it !!
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

    public static float getReachRangeAddition(ItemStack mainHandStack) {
        float dist = 0.0F;
        if (mainHandStack == null) return dist;
        Item item = mainHandStack.getItem();
        String name = item.getTranslationKey();
        if (name.contains("knife") || name.contains("hatchet") || name.contains("_cone") || (item instanceof ShearsItem) || (item instanceof FlintAndSteelItem))
            dist += 0.5F;
        else if ((name.contains("bone") && !mainHandStack.isOf(Items.BONE_MEAL)) || name.contains("rod") || item == Items.WOODEN_SWORD || item == Items.STICK)
            dist += 0.75F;
        else if (item instanceof RangedWeaponItem || ((item instanceof BlockItem && (!RotHelper.canRot(item) || (!(name.contains("seed") && (name.contains("pumpkin") || name.contains("melon"))))))))
            dist += 1.0F;
        else if (name.contains("spear") || (item instanceof TridentItem)) dist += 1.75F;
        else if ((item instanceof ShovelItem) || (item instanceof PickaxeItem) || (item instanceof AxeItem) || (item instanceof SwordItem) || (item instanceof HoeItem))
            dist += 1.5F;
        else if (mainHandStack.isEnchantable() && !(item instanceof ArmorItem)) dist += 1.0F;
        return dist;
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
        System.out.println(stack.getItem());
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
            if (statusManager.getRecentLittleOvereatenTicks() > 0)
                player.addStatusEffect(new StatusEffectInstance(HcsEffects.OVEREATEN, 600, 0, false, false, true));
            else statusManager.setRecentLittleOvereatenTicks(1200);
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
}
