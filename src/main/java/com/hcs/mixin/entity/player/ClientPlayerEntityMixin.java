package com.hcs.mixin.entity.player;

import com.hcs.main.manager.SanityManager;
import com.hcs.misc.HcsEffects;
import com.hcs.misc.accessor.StatAccessor;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
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
    private static final SoundEvent[] HALLU_SOUNDS = {ENTITY_ENDERMAN_DEATH, ENTITY_ENDERMAN_HURT, ENTITY_ENDERMAN_AMBIENT, ENTITY_HUSK_DEATH, ENTITY_ENDERMITE_DEATH, ENTITY_BLAZE_AMBIENT, ENTITY_BLAZE_DEATH, ENTITY_PIG_DEATH, ENTITY_DROWNED_DEATH, ENTITY_STRAY_DEATH, BLOCK_ROOTED_DIRT_BREAK, BLOCK_BONE_BLOCK_HIT, BLOCK_FIRE_AMBIENT, BLOCK_VINE_PLACE, ENTITY_LIGHTNING_BOLT_THUNDER, ENTITY_GHAST_SCREAM, ENTITY_GENERIC_EXPLODE, ENTITY_ZOMBIE_ATTACK_IRON_DOOR, ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, ENTITY_SKELETON_AMBIENT, ENTITY_CREEPER_PRIMED, ENTITY_SPIDER_AMBIENT};

    private static final SoundEvent[] HALLU_AMBIENT_SOUNDS = {AMBIENT_CAVE.value(), AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, AMBIENT_BASALT_DELTAS_MOOD.value(), AMBIENT_WARPED_FOREST_MOOD.value(), AMBIENT_BASALT_DELTAS_MOOD.value(), AMBIENT_SOUL_SAND_VALLEY_MOOD.value(), AMBIENT_UNDERWATER_LOOP, AMBIENT_BASALT_DELTAS_ADDITIONS.value(), AMBIENT_NETHER_WASTES_LOOP.value(), AMBIENT_NETHER_WASTES_ADDITIONS.value()};

    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        SanityManager sanityManager = ((StatAccessor) this).getSanityManager();
        boolean hasInsanityRenderEffect = this.client.gameRenderer.getPostProcessor() != null && sanityManager.getHasRenderedInsanityEffect();
        if (sanityManager.get() < 0.15F && this.hasStatusEffect(HcsEffects.INSANITY)) {
            if (!hasInsanityRenderEffect) {
                this.client.gameRenderer.loadPostProcessor(new Identifier("hcs", "shaders/post/insanity.json"));
                sanityManager.setHasRenderedInsanityEffect(true);
            }
            for (SoundCategory cate : new SoundCategory[]{SoundCategory.BLOCKS, SoundCategory.HOSTILE, SoundCategory.MUSIC, SoundCategory.NEUTRAL, SoundCategory.RECORDS, SoundCategory.VOICE, SoundCategory.WEATHER, SoundCategory.PLAYERS})
                this.client.getSoundManager().stopSounds(null, cate);
            if (this.world.getTime() % 30 == 0)
                this.world.playSound(this.getX(), this.getY(), this.getZ(), HALLU_AMBIENT_SOUNDS[(int) (HALLU_AMBIENT_SOUNDS.length * Math.random())], SoundCategory.AMBIENT, 26, -13, false);
            if (this.world.getTime() % 60 == 0)
                this.world.playSound(this.getX(), this.getY(), this.getZ(), HALLU_SOUNDS[(int) (HALLU_SOUNDS.length * Math.random())], SoundCategory.AMBIENT, 13, -26, false);
        } else if (hasInsanityRenderEffect || !sanityManager.getHasRenderedInsanityEffect()) {
            if (this.client.gameRenderer.getPostProcessor() != null)
                this.client.gameRenderer.getPostProcessor().close();
            sanityManager.setHasRenderedInsanityEffect(false);
        }
    }
}
