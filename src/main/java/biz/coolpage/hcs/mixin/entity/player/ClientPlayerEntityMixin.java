package biz.coolpage.hcs.mixin.entity.player;

import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.SanityManager;
import biz.coolpage.hcs.util.EntityHelper;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.sound.SoundEvents.*;

@Environment(value = EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    @Shadow
    @Final
    protected MinecraftClient client;
    @Unique
    private static final SoundEvent[] HALLUCINATION_SOUNDS = {ENTITY_ENDERMAN_DEATH, ENTITY_ENDERMAN_HURT, ENTITY_ENDERMAN_AMBIENT, ENTITY_HUSK_DEATH, ENTITY_ENDERMITE_DEATH, ENTITY_BLAZE_AMBIENT, ENTITY_BLAZE_DEATH, ENTITY_PIG_DEATH, ENTITY_DROWNED_DEATH, ENTITY_STRAY_DEATH, BLOCK_ROOTED_DIRT_BREAK, BLOCK_BONE_BLOCK_HIT, BLOCK_FIRE_AMBIENT, BLOCK_VINE_PLACE, ENTITY_LIGHTNING_BOLT_THUNDER, ENTITY_GHAST_SCREAM, ENTITY_GENERIC_EXPLODE, ENTITY_ZOMBIE_ATTACK_IRON_DOOR, ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, ENTITY_SKELETON_AMBIENT, ENTITY_CREEPER_PRIMED, ENTITY_SPIDER_AMBIENT};
    //    private static final SoundEvent[] HALLUCINATION_SOUNDS_VERY_HORRIBLE = {ENTITY_ENDERMAN_SCREAM, ENTITY_ENDERMAN_STARE};
    @Unique
    private static final SoundEvent[] HALLUCINATION_AMBIENT_SOUNDS = {AMBIENT_CAVE.value(), AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, AMBIENT_BASALT_DELTAS_MOOD.value(), AMBIENT_WARPED_FOREST_MOOD.value(), AMBIENT_BASALT_DELTAS_MOOD.value(), AMBIENT_SOUL_SAND_VALLEY_MOOD.value(), AMBIENT_UNDERWATER_LOOP, AMBIENT_BASALT_DELTAS_ADDITIONS.value(), AMBIENT_NETHER_WASTES_LOOP.value(), AMBIENT_NETHER_WASTES_ADDITIONS.value()};

    @Unique
    private static void playHall(@NotNull AbstractClientPlayerEntity player) {
        player.world.playSound(player.getX(), player.getY(), player.getZ(), HALLUCINATION_SOUNDS[(int) (HALLUCINATION_SOUNDS.length * Math.random())], SoundCategory.AMBIENT, 13, -26, false);
    }

    @Unique
    private static void playHallAmbient(@NotNull AbstractClientPlayerEntity player) {
        player.world.playSound(player.getX(), player.getY(), player.getZ(), HALLUCINATION_AMBIENT_SOUNDS[(int) (HALLUCINATION_AMBIENT_SOUNDS.length * Math.random())], SoundCategory.AMBIENT, 26, -13, false);
    }

    @Unique
    private boolean prevIsThirdPerson = false;
    @Unique
    private int prevInsanityEffectId = 0;
    @Unique
    private int ticks = 0;
    @Unique
    private int horriblyPlayedTicks = 0;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }


    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        ++ticks;
        if (horriblyPlayedTicks > 0) --horriblyPlayedTicks;
        SanityManager sanityManager = ((StatAccessor) this).getSanityManager();
        boolean isThirdPerson = this.client.gameRenderer.getCamera().isThirdPerson();
        boolean darknessEnveloped = this.hasStatusEffect(HcsEffects.DARKNESS_ENVELOPED);
        boolean hasInsanity = this.hasStatusEffect(HcsEffects.INSANITY);
        if (!hasInsanity && !darknessEnveloped && this.horriblyPlayedTicks > 0) {
            this.client.getSoundManager().stopSounds(null, SoundCategory.AMBIENT);
            this.horriblyPlayedTicks = 0;
        }
        if (EntityHelper.IS_SURVIVAL_LIKE.test(this)) {
            if (darknessEnveloped) {
                final int darkTicks = ((StatAccessor) this).getStatusManager().getInDarknessTicks();
                if (darkTicks == 60) playHallAmbient(this);
                else if (darkTicks == 290) {
                    this.world.playSound(this.getX(), this.getY(), this.getZ(), ENTITY_ENDERMAN_SCREAM, SoundCategory.AMBIENT, 26, -13, false);
                    this.world.playSound(this.getX(), this.getY(), this.getZ(), ENTITY_ENDERMAN_STARE, SoundCategory.AMBIENT, 26, -13, false);
                } else if (darkTicks == 360) // Also view SoundSystemMixin/getAdjustedVolume()
                    this.world.playSound(this.getX(), this.getY(), this.getZ(), ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.AMBIENT, 1145.0F, -13, false);
                else if (darkTicks > 120 && Math.random() < 0.01) {
                    if (Math.random() < 0.5) playHall(this);
                    else playHallAmbient(this);
                    this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_BREATH, SoundCategory.AMBIENT, 0.5f, world.random.nextFloat() * 0.1f + 0.9f, false);
                }
            }
            if (sanityManager.get() < 0.65) {
                int insanityEffectId = MathHelper.clamp((int) (sanityManager.get() * 20.0), 0, 12);
                if (prevInsanityEffectId != insanityEffectId || isThirdPerson != prevIsThirdPerson || this.ticks % 20 == 0) {
                    this.client.gameRenderer.loadPostProcessor(new Identifier("hcs", "shaders/post/insanity_" + insanityEffectId + ".json"));
                    this.prevInsanityEffectId = insanityEffectId;
                }
                if (hasInsanity) //noinspection CommentedOutCode
                {
                    if (sanityManager.get() < 0.15) {
                        for (SoundCategory cate : new SoundCategory[]{SoundCategory.BLOCKS, SoundCategory.HOSTILE, SoundCategory.MUSIC, SoundCategory.NEUTRAL, SoundCategory.RECORDS, SoundCategory.VOICE, SoundCategory.WEATHER, SoundCategory.PLAYERS})
                            this.client.getSoundManager().stopSounds(null, cate);
//                        this.world.spawnEntity(new BlazeEntity(EntityType.BLAZE, this.world));
                    }
                    if (this.world.getTime() % (sanityManager.get() < 0.15F ? 30 : 600) == 0)
                        playHallAmbient(this);
                    if (this.world.getTime() % (sanityManager.get() < 0.15F ? 60 : 1200) == 0)
                        playHall(this);
                /*
                if (sanityManager.getRealPain() < 0.05F && this.world.getTime() % 80 == 0) {
                    this.horriblyPlayedTicks = 2400;
                    this.world.playSound(this.getX(), this.getY(), this.getZ(), HALLUCINATION_SOUNDS_VERY_HORRIBLE[(int) (HALLUCINATION_SOUNDS_VERY_HORRIBLE.length * Math.random())], SoundCategory.AMBIENT, 13, -26, false);
                }
                 */
                }
            } else if (this.client.gameRenderer.getPostProcessor() != null) {
                this.client.gameRenderer.getPostProcessor().close();
                this.prevInsanityEffectId = -1;
            }
        }
        this.prevIsThirdPerson = isThirdPerson;
    }
}
