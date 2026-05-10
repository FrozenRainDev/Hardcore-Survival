package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.SanityManager;
import biz.coolpage.hcs.status.manager.StatusManager;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.HcsFactory;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minecraft.sounds.SoundEvents.*;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HallucinationEvents {

    private static Entity hallucinationEntity = null;
    // Added: Record the next game tick allowed to spawn a hallucination entity
    private static int nextSpawnTick = 0;

    @SubscribeEvent
    public static void onRenderLevel(@NotNull RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        if (minecraft.player.hasEffect(HcsEffects.INSANITY.get()) && ((StatAccessor) minecraft.player).getSanityManager().get() < 0.1) {
            double dist;
            Vec3 playerPos = minecraft.player.position();
            double san = ((StatAccessor) minecraft.player).getSanityManager().get();
            // Note: entityPos is the old position obtained BEFORE the spawning logic
            Vec3 entityPos = hallucinationEntity == null ? Vec3.ZERO : hallucinationEntity.position();

            // Strictly preserve original effects: entity disappears if distance > 16, or too close to player (< 4)
            if (hallucinationEntity != null && ((dist = playerPos.distanceTo(entityPos)) > 16 || dist < 4 || Double.isNaN(dist))) {
                hallucinationEntity = null;
                // Add a brief random cooldown: 20 ~ 80 ticks (approx. 1s ~ 4s)
                nextSpawnTick = minecraft.player.tickCount + Mth.nextInt(RandomSource.create(), 20 + (int) (san * 150), 60 + (int) (san * 150));
            }

            // If entity is null and current time hasn't reached cooldown end, skip rendering
            if (hallucinationEntity == null && minecraft.player.tickCount < nextSpawnTick) {
                return;
            }

            // Re-spawn randomly at a distance when cooldown ends and entity is null
            if (hallucinationEntity == null) {
                // Dynamically fetch all entities categorized as monsters from registry, including vanilla and mods
                List<EntityType<?>> monsters = ForgeRegistries.ENTITY_TYPES.getValues().stream()
                        .filter(type -> type.getCategory() == MobCategory.MONSTER)
                        .filter(type -> type != EntityType.SHULKER && type != EntityType.ENDER_DRAGON) // Exclude Shulker and Ender Dragon
                        .toList();

                if (!monsters.isEmpty()) {
                    EntityType<?> randomType = monsters.get(Mth.nextInt(RandomSource.create(), 0, monsters.size() - 1));
                    hallucinationEntity = randomType.create(minecraft.level);
                }

                // Fallback: If registry is abnormal or fails to create, default to Zombie
                if (hallucinationEntity == null) {
                    hallucinationEntity = new Zombie(EntityType.ZOMBIE, minecraft.level);
                }

                double newX = playerPos.x();
                double newY = playerPos.y(); // Height y consistent with player
                double newZ = playerPos.z();
                boolean validPos = false;

                // Try to find a spawn point within player's FOV (front) with no obstruction, max 15 attempts
                for (int i = 0; i < 15; i++) {
                    // Randomize within ±60 degrees of player's yaw (ensures it's in front)
                    float randomYaw = minecraft.player.getYRot() + Mth.nextFloat(RandomSource.create(), -60F, 60F);
                    float rad = randomYaw * ((float) Math.PI / 180F);
                    double tryDist = Mth.nextDouble(RandomSource.create(), 8D, 16D);

                    // Calculate target coordinates based on yaw and distance
                    double tryX = playerPos.x() - Mth.sin(rad) * tryDist;
                    double tryZ = playerPos.z() + Mth.cos(rad) * tryDist;
                    double tryY = playerPos.y();

                    // Raycasting for obstruction: From player eyes to target eye height
                    Vec3 eyePos = minecraft.player.getEyePosition();
                    Vec3 targetEyePos = new Vec3(tryX, tryY + minecraft.player.getEyeHeight(), tryZ);

                    HitResult hitResult = minecraft.level.clip(new ClipContext(
                            eyePos, targetEyePos,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            minecraft.player
                    ));

                    // MISS indicates no blocks in between
                    if (hitResult.getType() == HitResult.Type.MISS) {
                        newX = tryX;
                        newY = tryY;
                        newZ = tryZ;
                        validPos = true;
                        break;
                    }
                }

                // Fallback: If no unobstructed position found, use a point 10 blocks directly ahead
                if (!validPos) {
                    float rad = minecraft.player.getYRot() * ((float) Math.PI / 180F);
                    newX = playerPos.x() - Mth.sin(rad) * 10D;
                    newZ = playerPos.z() + Mth.cos(rad) * 10D;
                }

                hallucinationEntity.setPos(newX, newY, newZ);
                hallucinationEntity.xo = newX;
                hallucinationEntity.yo = newY;
                hallucinationEntity.zo = newZ;
            }

            // Sync Tick, restore model animations, and make entity drift slowly towards player like a ghost
            if (hallucinationEntity.tickCount != minecraft.player.tickCount) {
                hallucinationEntity.tickCount = minecraft.player.tickCount;

                // [Fix Point]: If entity was just re-spawned, its position is newX, newY, newZ.
                // The 'entityPos' variable above stores the OLD position. Must re-fetch current position here.
                Vec3 currentPos = hallucinationEntity.position();
                Vec3 dir = playerPos.subtract(currentPos).normalize().scale(0.5);

                hallucinationEntity.xo = currentPos.x;
                hallucinationEntity.yo = currentPos.y;
                hallucinationEntity.zo = currentPos.z;
                hallucinationEntity.setPos(currentPos.x + dir.x, currentPos.y, currentPos.z + dir.z);

                if (hallucinationEntity instanceof LivingEntity living) {
                    living.walkAnimation.update(3F, 0.2F);
                }
            }

            // Head and body rotation to stare directly at the player
            double dX = playerPos.x() - hallucinationEntity.getX();
            double dY = (playerPos.y() + minecraft.player.getEyeHeight()) - (hallucinationEntity.getY() + hallucinationEntity.getEyeHeight());
            double dZ = playerPos.z() - hallucinationEntity.getZ();
            double horizontalDistance = Math.sqrt(dX * dX + dZ * dZ);
            float yaw = (float) (Mth.atan2(dZ, dX) * (180D / Math.PI)) - 90.0F;
            float pitch = (float) -(Mth.atan2(dY, horizontalDistance) * (180D / Math.PI));

            hallucinationEntity.setYRot(yaw);
            hallucinationEntity.yRotO = yaw;
            hallucinationEntity.setXRot(pitch);
            hallucinationEntity.xRotO = pitch;

            if (hallucinationEntity instanceof LivingEntity living) {
                living.yHeadRot = yaw;
                living.yHeadRotO = yaw;
                living.yBodyRot = yaw;
                living.yBodyRotO = yaw;
            }

            // Environment particle effects (Commented out in original)
//            if (minecraft.level.random.nextFloat() < 0.4F) {
//                minecraft.level.addParticle(ParticleTypes.SOUL,
//                        hallucinationEntity.getRandomX(0.8D),
//                        hallucinationEntity.getRandomY(),
//                        hallucinationEntity.getRandomZ(0.8D),
//                        0.0D, 0.05D, 0.0D);
//            }
//            if (minecraft.level.random.nextFloat() < 0.2F) {
//                minecraft.level.addParticle(ParticleTypes.LARGE_SMOKE,
//                        hallucinationEntity.getRandomX(0.5D),
//                        hallucinationEntity.getRandomY() + 0.5D,
//                        hallucinationEntity.getRandomZ(0.5D),
//                        0.0D, 0.0D, 0.0D);
//            }

            if (minecraft.player.isAlive()) {
                PoseStack poseStack = event.getPoseStack();
                Camera camera = event.getCamera();
                float partialTick = event.getPartialTick();

                EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
                MultiBufferSource bufferSource = minecraft.renderBuffers().bufferSource();

                double renderX = Mth.lerp(partialTick, hallucinationEntity.xo, hallucinationEntity.getX()) - camera.getPosition().x();
                double renderY = Mth.lerp(partialTick, hallucinationEntity.yo, hallucinationEntity.getY()) - camera.getPosition().y();
                double renderZ = Mth.lerp(partialTick, hallucinationEntity.zo, hallucinationEntity.getZ()) - camera.getPosition().z();

                poseStack.pushPose();

                // Translate matrix to entity position first to ensure subsequent scaling doesn't cause drift
                poseStack.translate(renderX, renderY, renderZ);

                // --- New: Pulsating scale effect ---
                float time = minecraft.player.tickCount + partialTick;
                // Use sine wave to cycle scale between 0.8x and 1.2x (Wait, sin * 0.9 + 1.0 is 0.1 to 1.9)
                float pulseScale = 1.0F + Mth.sin(time * 0.15F) * 0.9F;

                // Move scaling origin to entity center (rather than feet) for a more natural look
                float halfHeight = hallucinationEntity.getBbHeight() / 2.0F;
                poseStack.translate(0, halfHeight, 0);
                poseStack.scale(pulseScale, pulseScale, pulseScale);
                poseStack.translate(0, -halfHeight, 0);

                // Visual Glitch effect
                if (minecraft.level.random.nextFloat() < 0.05F) {
                    poseStack.translate(
                            (Math.random() - 0.5) * 0.4,
                            (Math.random() - 0.5) * 0.4,
                            (Math.random() - 0.5) * 0.4
                    );
                    poseStack.scale(
                            1.0F + (float) (Math.random() - 0.5) * 0.2F,
                            1.0F + (float) Math.random() * 0.3F,
                            1.0F
                    );
                }

                // Execute rendering (Offset passed as 0.0D since we manually translated the poseStack)
                dispatcher.render(
                        hallucinationEntity,
                        0.0D,
                        0.0D,
                        0.0D,
                        Mth.lerp(partialTick, hallucinationEntity.yRotO, hallucinationEntity.getYRot()),
                        partialTick,
                        poseStack,
                        bufferSource,
                        15728880 // Changed to 15728880 (Full Brightness) so it glows in the dark
                );

                poseStack.popPose();
            }
        } else {
            hallucinationEntity = null;
            // Reset spawn cooldown when sanity recovers
            nextSpawnTick = 0;
        }
    }

    // --- Vision reduction under hallucination (Custom Fog Distance) ---
    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        double san = ((StatAccessor) minecraft.player).getSanityManager().get();
        if (minecraft.player.hasEffect(HcsEffects.INSANITY.get()) && san < 0.15) {
            float time = minecraft.player.tickCount + (float) event.getPartialTick();
            // Add a slight "breathing" pulsation to enhance the eerie atmosphere
            float pulse = Mth.sin(time * 0.05F) * 0.15F;

            float baseDistance = 32.0F + 640.0F * (float) san; // Base view distance (smaller makes player "blinder")
            float fogEnd = baseDistance * (1.0F + pulse);
            float fogStart = fogEnd * 0.1F; // Small start distance creates a heavy, oppressive fog covering the screen

            event.setNearPlaneDistance(fogStart);
            event.setFarPlaneDistance(fogEnd);
            event.setFogShape(FogShape.SPHERE); // Spherical fog for better immersion, unaffected by view angle
            event.setCanceled(true); // Cancel event to apply custom fog override
        }
    }

    // --- New: Set fog color (background) to pure black during hallucinations ---
    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        if (minecraft.player.hasEffect(HcsEffects.INSANITY.get()) && ((StatAccessor) minecraft.player).getSanityManager().get() < 0.1) {
            // Force fog RGB color to pure black
            event.setRed(0.0F);
            event.setGreen(0.0F);
            event.setBlue(0.0F);
        }
    }

    // Sound

    // State recording variables (Client-only, static variables for local player state)
    private static boolean prevIsThirdPerson = false;
    private static int prevInsanityEffectId = 0;
    private static int ticks = 0;
    private static int horriblyPlayedTicks = 0;

    // --- Categorizing sounds by horror intensity ---

    // Mild Auditory Hallucinations (Sanity 0.15 ~ 0.3): Environment, light cracking, spiders, etc.
    private static final SoundEvent[] MILD_SOUNDS = {
            ROOTED_DIRT_BREAK, BONE_BLOCK_HIT, VINE_PLACE, SKELETON_AMBIENT, SPIDER_AMBIENT, ZOMBIE_ATTACK_WOODEN_DOOR
    };

    // Moderate Auditory Hallucinations (Sanity 0.05 ~ 0.15): Zombie breaking iron door, creeper priming, ghast scream, thunder, etc.
    private static final SoundEvent[] SCARY_SOUNDS = {
            HUSK_DEATH, ENDERMITE_DEATH, BLAZE_AMBIENT, DROWNED_DEATH, STRAY_DEATH, ZOMBIE_ATTACK_IRON_DOOR, CREEPER_PRIMED, GHAST_SCREAM, LIGHTNING_BOLT_THUNDER
    };

    // Extreme Auditory Hallucinations (Sanity < 0.05): Piercing sounds, explosions, enderman screams
    private static final SoundEvent[] EXTREME_SOUNDS = {
            ENDERMAN_HURT, BLAZE_DEATH, PIG_DEATH, GENERIC_EXPLODE, ENDERMAN_SCREAM, ELDER_GUARDIAN_CURSE
    };

    // Ambient low-frequency sounds
    private static final SoundEvent[] AMBIENT_SOUNDS = {
            AMBIENT_CAVE.value(), AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, AMBIENT_BASALT_DELTAS_MOOD.value(), AMBIENT_WARPED_FOREST_MOOD.value(), AMBIENT_BASALT_DELTAS_MOOD.value(), AMBIENT_SOUL_SAND_VALLEY_MOOD.value(), AMBIENT_UNDERWATER_LOOP, AMBIENT_BASALT_DELTAS_ADDITIONS.value(), AMBIENT_NETHER_WASTES_LOOP.value(), AMBIENT_NETHER_WASTES_ADDITIONS.value()
    };

    // --- Sound playing helper methods ---
    @SuppressWarnings("SameParameterValue")
    private static void playRandomSound(@NotNull LocalPlayer player, SoundEvent[] soundPool, float volume, float pitch) {
        if (player.level() instanceof ClientLevel clientLevel) {
            SoundEvent sound = soundPool[player.getRandom().nextInt(soundPool.length)];
            clientLevel.playLocalSound(player.getX(), player.getY(), player.getZ(), sound, SoundSource.AMBIENT, volume, pitch, false);
        }
    }

    private static void playSpecificSound(@NotNull LocalPlayer player, SoundEvent sound, float volume, float pitch) {
        if (player.level() instanceof ClientLevel clientLevel) {
            clientLevel.playLocalSound(player.getX(), player.getY(), player.getZ(), sound, SoundSource.AMBIENT, volume, pitch, false);
        }
    }

    @SubscribeEvent
    public static void onClientPlayerTick(TickEvent.@NotNull PlayerTickEvent event) {
        // Ensure execution only at the END phase of the client tick, and only for the local player
        if (event.side.isServer() || event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (event.player != minecraft.player) return;

        LocalPlayer player = minecraft.player;
        ++ticks;
        if (horriblyPlayedTicks > 0) --horriblyPlayedTicks;

        assert player != null;
        SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
        double sanity = sanityManager.get();

        boolean isThirdPerson = minecraft.gameRenderer.getMainCamera().isDetached();
        boolean darknessEnveloped = player.hasEffect(HcsEffects.DARKNESS_ENVELOPED.get());
        boolean hasInsanity = player.hasEffect(HcsEffects.INSANITY.get());

        // Logic to stop ambient sounds
        if (!hasInsanity && !darknessEnveloped && horriblyPlayedTicks > 0) {
            minecraft.getSoundManager().stop(null, SoundSource.AMBIENT);
            horriblyPlayedTicks = 0;
        }

        if (EntityHelper.IS_SURVIVAL_LIKE.test(player)) {

            // 1. Preserve original Darkness Enveloped logic
            if (darknessEnveloped && !HcsDifficulty.isOf(player, HcsDifficulty.HcsDifficultyEnum.relaxing)) {
                final int darkTicks = statusManager.getInDarknessTicks();
                if (darkTicks == 60) {
                    playRandomSound(player, AMBIENT_SOUNDS, 26.0F, 1.0F);
                } else if (darkTicks == 580) {
                    playSpecificSound(player, ENDERMAN_SCREAM, 26.0F, 1.0F);
                    playSpecificSound(player, ENDERMAN_STARE, 26.0F, 1.0F);
                } else if (darkTicks == 720) {
                    playSpecificSound(player, ELDER_GUARDIAN_CURSE, 1145.0F, 1.0F);
                } else if (darkTicks > 240 && Math.random() < 0.01) {
                    if (Math.random() < 0.5) playRandomSound(player, MILD_SOUNDS, 13.0F, 1.0F);
                    else playRandomSound(player, AMBIENT_SOUNDS, 26.0F, 1.0F);
                    playSpecificSound(player, SoundEvents.PLAYER_BREATH, 0.5f, player.level().random.nextFloat() * 0.1f + 0.9f);
                }
            }

            // 2. Visual Distortion: Triggered when Sanity < 0.65
            if (sanity < 0.65) {
                int insanityEffectId = Mth.clamp((int) (sanity * 20.0), 0, 12);
                if (prevInsanityEffectId != insanityEffectId || isThirdPerson != prevIsThirdPerson || ticks % 20 == 0) {
                    minecraft.gameRenderer.loadEffect(HcsFactory.createResourceLocation("shaders/post/insanity_" + insanityEffectId + ".json"));
                    prevInsanityEffectId = insanityEffectId;
                }
            } else if (minecraft.gameRenderer.currentEffect() != null) {
                minecraft.gameRenderer.shutdownEffect();
                prevInsanityEffectId = -1;
            }

            // 3. Auditory Hallucination Logic: Triggered when Sanity < 0.3 AND Insanity effect is active
            if (hasInsanity && sanity < 0.3) {

                // Deprive player of real sounds in extreme cases
                if (sanity < 0.15) {
                    for (SoundSource cate : new SoundSource[]{SoundSource.BLOCKS, SoundSource.HOSTILE, SoundSource.MUSIC, SoundSource.NEUTRAL, SoundSource.RECORDS, SoundSource.VOICE, SoundSource.WEATHER, SoundSource.PLAYERS}) {
                        minecraft.getSoundManager().stop(null, cate);
                    }
                }

                // Scale hallucination frequency based on sanity (lower sanity = higher frequency)
                int ambientFreq = sanity <= 0.05 ? 100 : (sanity < 0.15 ? 300 : 600);
                int hallFreq = sanity <= 0.05 ? 60 : (sanity < 0.15 ? 200 : 800);

                // Play random ambient background sounds
                if (player.level().getGameTime() % ambientFreq == 0) {
                    playRandomSound(player, AMBIENT_SOUNDS, 26.0F, 1.0F);
                }

                // Play substantive auditory hallucinations
                if (player.level().getGameTime() % hallFreq == 0) {
                    if (sanity <= 0.05) {
                        // San < 0.05: High probability (70%) of Enderman stare, otherwise extreme sounds
                        if (player.getRandom().nextFloat() < 0.7F) {
                            playSpecificSound(player, ENDERMAN_STARE, 13.0F, 1.0F);
                        } else {
                            playRandomSound(player, EXTREME_SOUNDS, 13.0F, 1.0F);
                        }
                    } else if (sanity < 0.15) {
                        // 0.05 <= San < 0.15: Moderate horror sounds
                        playRandomSound(player, SCARY_SOUNDS, 13.0F, 1.0F);
                    } else {
                        // 0.15 <= San < 0.30: Mild eerie sounds
                        playRandomSound(player, MILD_SOUNDS, 13.0F, 1.0F);
                    }
                }
            }

        } else if (sanity < 0.65) {
            // Clear screen effects in Creative/Spectator mode if sanity is low
            minecraft.gameRenderer.shutdownEffect();
        }

        prevIsThirdPerson = isThirdPerson;
    }


    // Player Entity Rendering
    // Cache a client-side Skeleton instance to avoid per-frame creation and memory overflow
    private static Skeleton cachedHallucinationSkeleton = null;

    @SuppressWarnings("ConstantValue")
    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.@NotNull Pre event) {
        Player player = event.getEntity();
        Level level = player.level();

        // Check for hallucination replacement under extreme low sanity
        if (level != null && player.hasEffect(HcsEffects.INSANITY.get()) &&
                ((StatAccessor) player).getSanityManager().get() < 0.05) {

            // --- 1. Initialize cached entity (Create only first time or upon world change) ---
            if (cachedHallucinationSkeleton == null || cachedHallucinationSkeleton.level() != level) {
                cachedHallucinationSkeleton = new Skeleton(EntityType.SKELETON, level);
            }

            // --- 2. Sync held items and equipment ---
            cachedHallucinationSkeleton.setItemInHand(InteractionHand.MAIN_HAND, player.getMainHandItem());
            cachedHallucinationSkeleton.setItemInHand(InteractionHand.OFF_HAND, player.getOffhandItem());
            cachedHallucinationSkeleton.setItemSlot(EquipmentSlot.HEAD, player.getItemBySlot(EquipmentSlot.HEAD));
            cachedHallucinationSkeleton.setItemSlot(EquipmentSlot.CHEST, player.getItemBySlot(EquipmentSlot.CHEST));
            cachedHallucinationSkeleton.setItemSlot(EquipmentSlot.LEGS, player.getItemBySlot(EquipmentSlot.LEGS));
            cachedHallucinationSkeleton.setItemSlot(EquipmentSlot.FEET, player.getItemBySlot(EquipmentSlot.FEET));

            // --- 3. Core Fix: Sync coordinates and walking animation ---
            cachedHallucinationSkeleton.copyPosition(player);

            // [CRITICAL]: Calculate real movement distance per tick to drive leg animation
            if (cachedHallucinationSkeleton.tickCount != player.tickCount) {
                cachedHallucinationSkeleton.tickCount = player.tickCount;

                // Calculate horizontal distance traveled this tick
                double dx = player.getX() - player.xo;
                double dz = player.getZ() - player.zo;
                float moveDist = (float) Math.sqrt(dx * dx + dz * dz);

                // Use vanilla LivingEntity logic to convert movement distance to leg swing speed
                float walkSpeed = Math.min(moveDist * 4.0F, 1.0F);

                // Advance skeleton's internal accumulator to animate legs
                cachedHallucinationSkeleton.walkAnimation.update(walkSpeed, 0.4F);
            }

            // --- 4. Sync body rotation and posture ---
            cachedHallucinationSkeleton.setYRot(player.getYRot());
            cachedHallucinationSkeleton.yRotO = player.yRotO;
            cachedHallucinationSkeleton.setXRot(player.getXRot());
            cachedHallucinationSkeleton.xRotO = player.xRotO;
            cachedHallucinationSkeleton.yBodyRot = player.yBodyRot;
            cachedHallucinationSkeleton.yBodyRotO = player.yBodyRotO;
            cachedHallucinationSkeleton.yHeadRot = player.yHeadRot;
            cachedHallucinationSkeleton.yHeadRotO = player.yHeadRotO;

            cachedHallucinationSkeleton.setPose(player.getPose());
            cachedHallucinationSkeleton.swinging = player.swinging;
            cachedHallucinationSkeleton.swingTime = player.swingTime;
            cachedHallucinationSkeleton.swingingArm = player.swingingArm;
            cachedHallucinationSkeleton.attackAnim = player.attackAnim;
            cachedHallucinationSkeleton.oAttackAnim = player.oAttackAnim;

            // Fix: Damage rendering only requires hurtTime to trigger red flash; hurtDir is not needed on client
            cachedHallucinationSkeleton.hurtTime = player.hurtTime;

            // Fix: Bypass protected restrictions using public methods to sync eating/bow drawing actions
            if (player.isUsingItem()) {
                cachedHallucinationSkeleton.startUsingItem(player.getUsedItemHand());
            } else {
                cachedHallucinationSkeleton.stopUsingItem();
            }

            // ==========================================
            // Begin Replacement Rendering (With Jitter Effect)
            // ==========================================

            // Cancel original player rendering
            event.setCanceled(true);

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose(); // Push matrix to protect original rendering context

            // --- Visual Distortion Jitter (Glitch) Effect ---
            RandomSource random = player.getRandom();

            // High-frequency micro-jitter (Simulating nervous shaking)
            float shakeX = (random.nextFloat() - 0.5F) * 0.05F;
            float shakeY = (random.nextFloat() - 0.5F) * 0.05F;
            float shakeZ = (random.nextFloat() - 0.5F) * 0.05F;
            poseStack.translate(shakeX, shakeY, shakeZ);

            // Low-probability large dislocation and distortion (Simulating sudden mental breakdown)
            if (random.nextFloat() < 0.15F) {
                float glitchX = (random.nextFloat() - 0.5F) * 0.5F;
                float glitchZ = (random.nextFloat() - 0.5F) * 0.5F;
                poseStack.translate(glitchX, 0, glitchZ);

                float glitchRot = (random.nextFloat() - 0.5F) * 20.0F;
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(glitchRot));
            }

            // --- 5. Execute Rendering ---
            Minecraft.getInstance().getEntityRenderDispatcher()
                    .getRenderer(cachedHallucinationSkeleton)
                    .render(
                            cachedHallucinationSkeleton,
                            player.getYRot(),
                            event.getPartialTick(),
                            poseStack,
                            event.getMultiBufferSource(),
                            event.getPackedLight()
                    );

            poseStack.popPose(); // Pop matrix to clean up
        }
    }
}