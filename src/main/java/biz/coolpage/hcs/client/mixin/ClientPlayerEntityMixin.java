package biz.coolpage.hcs.client.mixin;

import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.SanityManager;
import biz.coolpage.hcs.status.manager.StatusManager;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.HcsFactory;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static net.minecraft.sounds.SoundEvents.*;

@OnlyIn(Dist.CLIENT)
@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayer {
    @Shadow
    @Final
    protected Minecraft minecraft;

    @Shadow
    public abstract void sendSystemMessage(Component message);

    @Unique
    private static final SoundEvent[] HALLUCINATION_SOUNDS = {ENDERMAN_DEATH, ENDERMAN_HURT, ENDERMAN_AMBIENT, HUSK_DEATH, ENDERMITE_DEATH, BLAZE_AMBIENT, BLAZE_DEATH, PIG_DEATH, DROWNED_DEATH, STRAY_DEATH, ROOTED_DIRT_BREAK, BONE_BLOCK_HIT, FIRE_AMBIENT, VINE_PLACE, LIGHTNING_BOLT_THUNDER, GHAST_SCREAM, GENERIC_EXPLODE, ZOMBIE_ATTACK_IRON_DOOR, ZOMBIE_ATTACK_WOODEN_DOOR, SKELETON_AMBIENT, CREEPER_PRIMED, SPIDER_AMBIENT};

    @Unique
    private static final SoundEvent[] HALLUCINATION_AMBIENT_SOUNDS = {AMBIENT_CAVE.value(), AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, AMBIENT_BASALT_DELTAS_MOOD.value(), AMBIENT_WARPED_FOREST_MOOD.value(), AMBIENT_BASALT_DELTAS_MOOD.value(), AMBIENT_SOUL_SAND_VALLEY_MOOD.value(), AMBIENT_UNDERWATER_LOOP, AMBIENT_BASALT_DELTAS_ADDITIONS.value(), AMBIENT_NETHER_WASTES_LOOP.value(), AMBIENT_NETHER_WASTES_ADDITIONS.value()};

    @Unique
    private static void playHall(@NotNull AbstractClientPlayer player) {
        // 在客户端使用 playLocalSound 以匹配 (double, double, double, SoundEvent, SoundSource, float, float, boolean)
        if (player.level() instanceof ClientLevel clientLevel) {
            clientLevel.playLocalSound(player.getX(), player.getY(), player.getZ(), HALLUCINATION_SOUNDS[(int) (HALLUCINATION_SOUNDS.length * Math.random())], SoundSource.AMBIENT, 13.0F, 1.0F, false);
        }
    }

    @Unique
    private static void playHallAmbient(@NotNull AbstractClientPlayer player) {
        if (player.level() instanceof ClientLevel clientLevel) {
            clientLevel.playLocalSound(player.getX(), player.getY(), player.getZ(), HALLUCINATION_AMBIENT_SOUNDS[(int) (HALLUCINATION_AMBIENT_SOUNDS.length * Math.random())], SoundSource.AMBIENT, 26.0F, 1.0F, false);
        }
    }

    @Unique
    private boolean prevIsThirdPerson = false;
    @Unique
    private int prevInsanityEffectId = 0;
    @Unique
    private int ticks = 0;
    @Unique
    private int horriblyPlayedTicks = 0;

    public ClientPlayerEntityMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        ++ticks;
        if (horriblyPlayedTicks > 0) --horriblyPlayedTicks;
        SanityManager sanityManager = ((StatAccessor) this).getSanityManager();
        boolean isThirdPerson = this.minecraft.gameRenderer.getMainCamera().isDetached();
        boolean darknessEnveloped = this.hasEffect(HcsEffects.DARKNESS_ENVELOPED);
        boolean hasInsanity = this.hasEffect(HcsEffects.INSANITY);
        StatusManager statusManager = ((StatAccessor) this).getStatusManager();

        if (!hasInsanity && !darknessEnveloped && this.horriblyPlayedTicks > 0) {
            this.minecraft.getSoundManager().stop(null, SoundSource.AMBIENT);
            this.horriblyPlayedTicks = 0;
        }

        if (EntityHelper.IS_SURVIVAL_LIKE.test(this)) {
            if (darknessEnveloped && !HcsDifficulty.isOf(this.minecraft.player, HcsDifficulty.HcsDifficultyEnum.relaxing)) {
                final int darkTicks = statusManager.getInDarknessTicks();
                if (this.level() instanceof ClientLevel clientLevel) {
                    if (darkTicks == 60) playHallAmbient(this);
                    else if (darkTicks == 580) {
                        clientLevel.playLocalSound(this.getX(), this.getY(), this.getZ(), ENDERMAN_SCREAM, SoundSource.AMBIENT, 26.0F, 1.0F, false);
                        clientLevel.playLocalSound(this.getX(), this.getY(), this.getZ(), ENDERMAN_STARE, SoundSource.AMBIENT, 26.0F, 1.0F, false);
                    } else if (darkTicks == 720)
                        clientLevel.playLocalSound(this.getX(), this.getY(), this.getZ(), ELDER_GUARDIAN_CURSE, SoundSource.AMBIENT, 1145.0F, 1.0F, false);
                    else if (darkTicks > 240 && Math.random() < 0.01) {
                        if (Math.random() < 0.5) playHall(this);
                        else playHallAmbient(this);
                        clientLevel.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_BREATH, SoundSource.AMBIENT, 0.5f, this.level().random.nextFloat() * 0.1f + 0.9f, false);
                    }
                }
            }

            if (sanityManager.get() < 0.65) {
                int insanityEffectId = Mth.clamp((int) (sanityManager.get() * 20.0), 0, 12);
                if (prevInsanityEffectId != insanityEffectId || isThirdPerson != prevIsThirdPerson || this.ticks % 20 == 0) {
                    this.minecraft.gameRenderer.loadEffect(HcsFactory.createResourceLocation("shaders/post/insanity_" + insanityEffectId + ".json"));
                    this.prevInsanityEffectId = insanityEffectId;
                }
                if (hasInsanity) {
                    if (sanityManager.get() < 0.15) {
                        for (SoundSource cate : new SoundSource[]{SoundSource.BLOCKS, SoundSource.HOSTILE, SoundSource.MUSIC, SoundSource.NEUTRAL, SoundSource.RECORDS, SoundSource.VOICE, SoundSource.WEATHER, SoundSource.PLAYERS})
                            this.minecraft.getSoundManager().stop(null, cate);
                    }
                    if (this.level().getGameTime() % (sanityManager.get() < 0.15F ? 30 : 600) == 0)
                        playHallAmbient(this);
                    if (this.level().getGameTime() % (sanityManager.get() < 0.15F ? 60 : 1200) == 0)
                        playHall(this);
                }
            } else if (this.minecraft.gameRenderer.currentEffect() != null) {
                this.minecraft.gameRenderer.shutdownEffect();
                this.prevInsanityEffectId = -1;
            }
        } else if (sanityManager.get() < 0.65) {
            // 修复赋值错误：方法不能作为左值，使用 shutdownEffect()
            this.minecraft.gameRenderer.shutdownEffect();
        }
        this.prevIsThirdPerson = isThirdPerson;
    }
}