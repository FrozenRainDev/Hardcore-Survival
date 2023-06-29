package com.hcs.mixin.entity.player;

import com.hcs.status.manager.SanityManager;
import com.hcs.status.HcsEffects;
import com.hcs.status.accessor.StatAccessor;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.sound.SoundEvents.*;

@Environment(value = EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity {
    @Shadow
    @Final
    protected MinecraftClient client;
    private static final SoundEvent[] HALLUCINATION_SOUNDS = {ENTITY_ENDERMAN_DEATH, ENTITY_ENDERMAN_HURT, ENTITY_ENDERMAN_AMBIENT, ENTITY_HUSK_DEATH, ENTITY_ENDERMITE_DEATH, ENTITY_BLAZE_AMBIENT, ENTITY_BLAZE_DEATH, ENTITY_PIG_DEATH, ENTITY_DROWNED_DEATH, ENTITY_STRAY_DEATH, BLOCK_ROOTED_DIRT_BREAK, BLOCK_BONE_BLOCK_HIT, BLOCK_FIRE_AMBIENT, BLOCK_VINE_PLACE, ENTITY_LIGHTNING_BOLT_THUNDER, ENTITY_GHAST_SCREAM, ENTITY_GENERIC_EXPLODE, ENTITY_ZOMBIE_ATTACK_IRON_DOOR, ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, ENTITY_SKELETON_AMBIENT, ENTITY_CREEPER_PRIMED, ENTITY_SPIDER_AMBIENT};
    private static final SoundEvent[] HALLUCINATION_AMBIENT_SOUNDS = {AMBIENT_CAVE.value(), AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, AMBIENT_BASALT_DELTAS_MOOD.value(), AMBIENT_WARPED_FOREST_MOOD.value(), AMBIENT_BASALT_DELTAS_MOOD.value(), AMBIENT_SOUL_SAND_VALLEY_MOOD.value(), AMBIENT_UNDERWATER_LOOP, AMBIENT_BASALT_DELTAS_ADDITIONS.value(), AMBIENT_NETHER_WASTES_LOOP.value(), AMBIENT_NETHER_WASTES_ADDITIONS.value()};
    private boolean prevIsThirdPerson = false;
    private int prevInsanityEffectId = 0;
    private int ticks = 0;

    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        ++ticks;
        SanityManager sanityManager = ((StatAccessor) this).getSanityManager();
        boolean isThirdPerson = this.client.gameRenderer.getCamera().isThirdPerson();
        if (!this.isCreative() && !this.isSpectator() && sanityManager.get() < 0.65) {
            int insanityEffectId = Math.min(12, Math.max(0, (int) (sanityManager.get() * 20.0)));
            if (prevInsanityEffectId != insanityEffectId || isThirdPerson != prevIsThirdPerson || this.ticks % 20 == 0) {
                this.client.gameRenderer.loadPostProcessor(new Identifier("hcs", "shaders/post/insanity_" + insanityEffectId + ".json"));
                this.prevInsanityEffectId = insanityEffectId;
            }
            if (this.hasStatusEffect(HcsEffects.INSANITY)) {
                if (sanityManager.get() < 0.15) {
                    for (SoundCategory cate : new SoundCategory[]{SoundCategory.BLOCKS, SoundCategory.HOSTILE, SoundCategory.MUSIC, SoundCategory.NEUTRAL, SoundCategory.RECORDS, SoundCategory.VOICE, SoundCategory.WEATHER, SoundCategory.PLAYERS})
                        this.client.getSoundManager().stopSounds(null, cate);
                    this.world.spawnEntity(new BlazeEntity(EntityType.BLAZE, this.world));
                }
                if (this.world.getTime() % (sanityManager.get() < 0.15F ? 30 : 600) == 0)
                    this.world.playSound(this.getX(), this.getY(), this.getZ(), HALLUCINATION_AMBIENT_SOUNDS[(int) (HALLUCINATION_AMBIENT_SOUNDS.length * Math.random())], SoundCategory.AMBIENT, 26, -13, false);
                if (this.world.getTime() % (sanityManager.get() < 0.15F ? 60 : 1200) == 0)
                    this.world.playSound(this.getX(), this.getY(), this.getZ(), HALLUCINATION_SOUNDS[(int) (HALLUCINATION_SOUNDS.length * Math.random())], SoundCategory.AMBIENT, 13, -26, false);
            }
        } else if (this.client.gameRenderer.getPostProcessor() != null) {
            this.client.gameRenderer.getPostProcessor().close();
            this.prevInsanityEffectId = -1;
        }
        this.prevIsThirdPerson = isThirdPerson;
    }
}
