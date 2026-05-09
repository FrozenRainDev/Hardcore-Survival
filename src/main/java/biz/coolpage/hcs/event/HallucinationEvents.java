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
    // 新增：记录下一次允许生成幻觉实体的游戏刻
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
            // 注意这里：entityPos 是在生成逻辑【之前】获取的旧位置
            Vec3 entityPos = hallucinationEntity == null ? Vec3.ZERO : hallucinationEntity.position();

            // 严格保留原有效果：距离大于16，或者离玩家较近（小于6）时，幻觉实体消失
            if (hallucinationEntity != null && ((dist = playerPos.distanceTo(entityPos)) > 16 || dist < 4 || Double.isNaN(dist))) {
                hallucinationEntity = null;
                // 添加短暂的随机冷却时间：20 ~ 80 刻（即 1秒 ~ 4秒）
                nextSpawnTick = minecraft.player.tickCount + Mth.nextInt(RandomSource.create(), 20 + (int) (san * 150), 60 + (int) (san * 150));
            }

            // 如果实体为空，并且当前时间还未达到冷却结束时间，则直接跳过后续渲染
            if (hallucinationEntity == null && minecraft.player.tickCount < nextSpawnTick) {
                return;
            }

            // 冷却结束且实体为空时，重新在远处随机生成
            if (hallucinationEntity == null) {
                // 改为从注册表中动态获取所有被归类为怪物（敌对生物）的实体，包括原版和所有模组的怪物
                List<EntityType<?>> monsters = ForgeRegistries.ENTITY_TYPES.getValues().stream()
                        .filter(type -> type.getCategory() == MobCategory.MONSTER)
                        .filter(type -> type != EntityType.SHULKER && type != EntityType.ENDER_DRAGON) // 排除潜影贝、末影龙
                        .toList();

                if (!monsters.isEmpty()) {
                    EntityType<?> randomType = monsters.get(Mth.nextInt(RandomSource.create(), 0, monsters.size() - 1));
                    hallucinationEntity = randomType.create(minecraft.level);
                }

                // 兜底方案：如果注册表异常或无法在客户端生成，默认退回僵尸
                if (hallucinationEntity == null) {
                    hallucinationEntity = new Zombie(EntityType.ZOMBIE, minecraft.level);
                }

                double newX = playerPos.x();
                double newY = playerPos.y(); // 高度y与玩家一致
                double newZ = playerPos.z();
                boolean validPos = false;

                // 尝试寻找玩家视野内（面前）且无遮挡的生成点，最多尝试 15 次
                for (int i = 0; i < 15; i++) {
                    // 在玩家面朝方向的 ±60 度范围内随机（保证在玩家视野前方）
                    float randomYaw = minecraft.player.getYRot() + Mth.nextFloat(RandomSource.create(), -60F, 60F);
                    float rad = randomYaw * ((float) Math.PI / 180F);
                    double tryDist = Mth.nextDouble(RandomSource.create(), 8D, 16D);

                    // 根据偏航角和距离计算目标坐标
                    double tryX = playerPos.x() - Mth.sin(rad) * tryDist;
                    double tryZ = playerPos.z() + Mth.cos(rad) * tryDist;
                    double tryY = playerPos.y();

                    // 视线遮挡检测（光线追踪）：从玩家眼睛到试图生成的位置的眼睛高度
                    Vec3 eyePos = minecraft.player.getEyePosition();
                    Vec3 targetEyePos = new Vec3(tryX, tryY + minecraft.player.getEyeHeight(), tryZ);

                    HitResult hitResult = minecraft.level.clip(new ClipContext(
                            eyePos, targetEyePos,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            minecraft.player
                    ));

                    // MISS表示中间没有方块遮挡
                    if (hitResult.getType() == HitResult.Type.MISS) {
                        newX = tryX;
                        newY = tryY;
                        newZ = tryZ;
                        validPos = true;
                        break;
                    }
                }

                // 如果多次尝试仍未找到无遮挡位置，使用正前方距离10的位置兜底
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

            // 同步Tick，恢复正常模型动画，并让实体像幽灵一样缓慢向玩家飘移
            if (hallucinationEntity.tickCount != minecraft.player.tickCount) {
                hallucinationEntity.tickCount = minecraft.player.tickCount;

                // 【修复点】：如果是刚刚重新生成的实体，它的实际位置已经变成了 newX, newY, newZ
                // 但上面的 entityPos 变量存的还是旧位置。所以这里必须重新获取最新位置。
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

            // 头部与身体旋转，死死盯着玩家
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

            // 生成环境粒子效果
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

                // 先将矩阵平移到实体所在位置，保证后续的缩放不会导致坐标飞走
                poseStack.translate(renderX, renderY, renderZ);

                // --- 新增：忽大忽小的脉冲缩放效果 ---
                float time = minecraft.player.tickCount + partialTick;
                // 利用正弦波，在 0.8x 到 1.2x 之间循环缩放
                float pulseScale = 1.0F + Mth.sin(time * 0.15F) * 0.9F;

                // 将缩放原点移到实体中心位置（而非脚底）显得更自然
                float halfHeight = hallucinationEntity.getBbHeight() / 2.0F;
                poseStack.translate(0, halfHeight, 0);
                poseStack.scale(pulseScale, pulseScale, pulseScale);
                poseStack.translate(0, -halfHeight, 0);

                // 视觉故障特效 (Glitch)
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

                // 执行渲染（因为上面已经手动平移过坐标，所以这里的偏移值传 0.0D）
                dispatcher.render(
                        hallucinationEntity,
                        0.0D,
                        0.0D,
                        0.0D,
                        Mth.lerp(partialTick, hallucinationEntity.yRotO, hallucinationEntity.getYRot()),
                        partialTick,
                        poseStack,
                        bufferSource,
                        15728880 // 原为 dispatcher.getPackedLightCoords，修改为 15728880 (即最高亮度)，使其在黑夜中也能发光
                );

                poseStack.popPose();
            }
        } else {
            hallucinationEntity = null;
            // 玩家理智恢复时重置冷却时间
            nextSpawnTick = 0;
        }
    }

    // --- 幻觉状态下视野缩减（自定义雾气距离） ---
    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        double san = ((StatAccessor) minecraft.player).getSanityManager().get();
        if (minecraft.player.hasEffect(HcsEffects.INSANITY.get()) && san < 0.15) {
            float time = minecraft.player.tickCount + (float) event.getPartialTick();
            // 加入轻微的呼吸波动感，增加诡异氛围
            float pulse = Mth.sin(time * 0.05F) * 0.15F;

            float baseDistance = 32.0F + 640.0F * (float) san; // 基础视野距离，可以根据你的硬核难度调整（越小越瞎）
            float fogEnd = baseDistance * (1.0F + pulse);
            float fogStart = fogEnd * 0.1F; // 雾气开始渐变的距离，设置得极小能让整个屏幕笼罩在压抑感中

            event.setNearPlaneDistance(fogStart);
            event.setFarPlaneDistance(fogEnd);
            event.setFogShape(FogShape.SPHERE); // 采用球形雾气，包裹感更强，不受视角影响
            event.setCanceled(true); // 必须取消事件以应用我们自定义的雾气距离覆盖原版
        }
    }

    // --- 新增：幻觉状态下将雾气颜色（即背景颜色）改为纯黑色 ---
    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        if (minecraft.player.hasEffect(HcsEffects.INSANITY.get()) && ((StatAccessor) minecraft.player).getSanityManager().get() < 0.1) {
            // 强制将雾气RGB颜色设置为纯黑
            event.setRed(0.0F);
            event.setGreen(0.0F);
            event.setBlue(0.0F);
        }
    }

    // Sound


    // 状态记录变量（因为是纯客户端，直接用静态变量记录本地玩家状态即可）
    private static boolean prevIsThirdPerson = false;
    private static int prevInsanityEffectId = 0;
    private static int ticks = 0;
    private static int horriblyPlayedTicks = 0;

    // --- 将音效按惊悚程度分级 ---

    // 轻度幻听（San值 0.15 ~ 0.3）：环境音、轻微破裂、蜘蛛等
    private static final SoundEvent[] MILD_SOUNDS = {
            ROOTED_DIRT_BREAK, BONE_BLOCK_HIT, VINE_PLACE, SKELETON_AMBIENT, SPIDER_AMBIENT, ZOMBIE_ATTACK_WOODEN_DOOR
    };

    // 中度幻听（San值 0.05 ~ 0.15）：僵尸砸铁门、苦力怕点燃、恶魂尖叫、雷声等压迫感音效
    private static final SoundEvent[] SCARY_SOUNDS = {
            HUSK_DEATH, ENDERMITE_DEATH, BLAZE_AMBIENT, DROWNED_DEATH, STRAY_DEATH, ZOMBIE_ATTACK_IRON_DOOR, CREEPER_PRIMED, GHAST_SCREAM, LIGHTNING_BOLT_THUNDER
    };

    // 极端幻听（San值 < 0.05）：极其刺耳、爆炸、末影人尖叫
    private static final SoundEvent[] EXTREME_SOUNDS = {
            ENDERMAN_HURT, BLAZE_DEATH, PIG_DEATH, GENERIC_EXPLODE, ENDERMAN_SCREAM, ELDER_GUARDIAN_CURSE
    };

    // 环境低频音（保持原样）
    private static final SoundEvent[] AMBIENT_SOUNDS = {
            AMBIENT_CAVE.value(), AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, AMBIENT_BASALT_DELTAS_MOOD.value(), AMBIENT_WARPED_FOREST_MOOD.value(), AMBIENT_BASALT_DELTAS_MOOD.value(), AMBIENT_SOUL_SAND_VALLEY_MOOD.value(), AMBIENT_UNDERWATER_LOOP, AMBIENT_BASALT_DELTAS_ADDITIONS.value(), AMBIENT_NETHER_WASTES_LOOP.value(), AMBIENT_NETHER_WASTES_ADDITIONS.value()
    };

    // --- 音效播放辅助方法 ---
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
        // 确保只在客户端的 Tick 结束阶段执行，且只针对当前操作的本地玩家
        if (event.side.isServer() || event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (event.player != minecraft.player) return;

        LocalPlayer player = minecraft.player;
        ++ticks;
        if (horriblyPlayedTicks > 0) --horriblyPlayedTicks;

        SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
        double sanity = sanityManager.get();

        boolean isThirdPerson = minecraft.gameRenderer.getMainCamera().isDetached();
        boolean darknessEnveloped = player.hasEffect(HcsEffects.DARKNESS_ENVELOPED.get());
        boolean hasInsanity = player.hasEffect(HcsEffects.INSANITY.get());

        // 停止环境音效的逻辑
        if (!hasInsanity && !darknessEnveloped && horriblyPlayedTicks > 0) {
            minecraft.getSoundManager().stop(null, SoundSource.AMBIENT);
            horriblyPlayedTicks = 0;
        }

        if (EntityHelper.IS_SURVIVAL_LIKE.test(player)) {

            // 1. 保留原版的黑暗笼罩 (DARKNESS_ENVELOPED) 逻辑
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

            // 2. 视觉扭曲效果：依然在 San值 < 0.65 时触发
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

            // 3. 听觉幻觉逻辑：仅在 San值 < 0.3 且拥有 Insanity 效果时触发
            if (hasInsanity && sanity < 0.3) {

                // 极端情况下剥夺玩家真实的听觉
                if (sanity < 0.15) {
                    for (SoundSource cate : new SoundSource[]{SoundSource.BLOCKS, SoundSource.HOSTILE, SoundSource.MUSIC, SoundSource.NEUTRAL, SoundSource.RECORDS, SoundSource.VOICE, SoundSource.WEATHER, SoundSource.PLAYERS}) {
                        minecraft.getSoundManager().stop(null, cate);
                    }
                }

                // 根据 San 值动态缩放幻听播放的频率（值越低，频率越高）
                int ambientFreq = sanity <= 0.05 ? 100 : (sanity < 0.15 ? 300 : 600);
                int hallFreq = sanity <= 0.05 ? 60 : (sanity < 0.15 ? 200 : 800);

                // 播放随机背景环境音
                if (player.level().getGameTime() % ambientFreq == 0) {
                    playRandomSound(player, AMBIENT_SOUNDS, 26.0F, 1.0F);
                }

                // 播放实质性幻听
                if (player.level().getGameTime() % hallFreq == 0) {
                    if (sanity <= 0.05) {
                        // San < 0.05：有极高概率 (70%) 直接播放末影人死亡音效，否则播放其他极端音效
                        if (player.getRandom().nextFloat() < 0.7F) {
                            playSpecificSound(player, ENDERMAN_STARE, 13.0F, 1.0F);
                        } else {
                            playRandomSound(player, EXTREME_SOUNDS, 13.0F, 1.0F);
                        }
                    } else if (sanity < 0.15) {
                        // 0.05 <= San < 0.15：播放中度惊悚音效
                        playRandomSound(player, SCARY_SOUNDS, 13.0F, 1.0F);
                    } else {
                        // 0.15 <= San < 0.30：播放轻度诡异音效
                        playRandomSound(player, MILD_SOUNDS, 13.0F, 1.0F);
                    }
                }
            }

        } else if (sanity < 0.65) {
            // 创造/旁观者模式下，且San值不足时，清除屏幕效果
            minecraft.gameRenderer.shutdownEffect();
        }

        prevIsThirdPerson = isThirdPerson;
    }


    // Player Entity Rendering
// 缓存一个客户端专用的骷髅实例，避免每帧创建导致内存溢出
    private static Skeleton cachedHallucinationSkeleton = null;

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.@NotNull Pre event) {
        Player player = event.getEntity();
        Level level = player.level();

        // 判断是否触发极端掉San情况的幻觉替换
        if (level != null && player.hasEffect(HcsEffects.INSANITY.get()) &&
                ((StatAccessor) player).getSanityManager().get() < 0.05) {

            // --- 1. 初始化缓存实体（仅在第一次或切换世界时创建） ---
            if (cachedHallucinationSkeleton == null || cachedHallucinationSkeleton.level() != level) {
                cachedHallucinationSkeleton = new Skeleton(EntityType.SKELETON, level);
            }

            // --- 2. 同步手持物品和装备 ---
            cachedHallucinationSkeleton.setItemInHand(InteractionHand.MAIN_HAND, player.getMainHandItem());
            cachedHallucinationSkeleton.setItemInHand(InteractionHand.OFF_HAND, player.getOffhandItem());
            cachedHallucinationSkeleton.setItemSlot(EquipmentSlot.HEAD, player.getItemBySlot(EquipmentSlot.HEAD));
            cachedHallucinationSkeleton.setItemSlot(EquipmentSlot.CHEST, player.getItemBySlot(EquipmentSlot.CHEST));
            cachedHallucinationSkeleton.setItemSlot(EquipmentSlot.LEGS, player.getItemBySlot(EquipmentSlot.LEGS));
            cachedHallucinationSkeleton.setItemSlot(EquipmentSlot.FEET, player.getItemBySlot(EquipmentSlot.FEET));

            // --- 3. 核心修复：同步坐标与走路动画 ---
            cachedHallucinationSkeleton.copyPosition(player);

            // 【关键】：每刻（Tick）计算一次真实移动距离，推进腿部动画
            if (cachedHallucinationSkeleton.tickCount != player.tickCount) {
                cachedHallucinationSkeleton.tickCount = player.tickCount;

                // 计算玩家这一刻在水平方向上的真实移动距离
                double dx = player.getX() - player.xo;
                double dz = player.getZ() - player.zo;
                float moveDist = (float) Math.sqrt(dx * dx + dz * dz);

                // 使用原版 LivingEntity 的算法，将移动距离转换为腿部摆动速度
                float walkSpeed = Math.min(moveDist * 4.0F, 1.0F);

                // 推进骷髅的内部累加器，让腿动起来！
                cachedHallucinationSkeleton.walkAnimation.update(walkSpeed, 0.4F);
            }

            // --- 4. 同步身体旋转与姿态 ---
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

            // 修复：受伤渲染只需要 hurtTime 即可触发变红闪烁，hurtDir 客户端不需要
            cachedHallucinationSkeleton.hurtTime = player.hurtTime;

            // 修复：绕过 protected 限制，利用原生公开方法同步吃东西/拉弓动作
            if (player.isUsingItem()) {
                cachedHallucinationSkeleton.startUsingItem(player.getUsedItemHand());
            } else {
                cachedHallucinationSkeleton.stopUsingItem();
            }

            // ==========================================
            // 开始执行替换渲染逻辑 (自带抽搐特效)
            // ==========================================

            // 取消原版玩家的渲染
            event.setCanceled(true);

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose(); // 压入矩阵，保护原有的渲染上下文

            // --- 视觉扭曲抽搐 (Glitch) 特效 ---
            RandomSource random = player.getRandom();

            // 高频微小抖动 (模拟精神紧张发抖)
            float shakeX = (random.nextFloat() - 0.5F) * 0.05F;
            float shakeY = (random.nextFloat() - 0.5F) * 0.05F;
            float shakeZ = (random.nextFloat() - 0.5F) * 0.05F;
            poseStack.translate(shakeX, shakeY, shakeZ);

            // 低概率大幅度错位与扭曲 (模拟突然的神经崩溃)
            if (random.nextFloat() < 0.15F) {
                float glitchX = (random.nextFloat() - 0.5F) * 0.5F;
                float glitchZ = (random.nextFloat() - 0.5F) * 0.5F;
                poseStack.translate(glitchX, 0, glitchZ);

                float glitchRot = (random.nextFloat() - 0.5F) * 20.0F;
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(glitchRot));
            }

            // --- 5. 执行渲染 ---
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

            poseStack.popPose(); // 弹出矩阵，清理现场
        }
    }
}