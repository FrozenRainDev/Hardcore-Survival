package biz.coolpage.hcs.client.mixin.render;

import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {
    protected WorldRendererMixin(RenderBuffers renderBuffers) {
        this.renderBuffers = renderBuffers;
    }

    @Shadow
    protected abstract void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers);

    @Mutable
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    Entity hallucinationEntity;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    public void render(PoseStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        if (this.minecraft.level != null && this.minecraft.player != null && this.minecraft.player.hasEffect(HcsEffects.INSANITY) && ((StatAccessor) this.minecraft.player).getSanityManager().get() < 0.1) {
            double dist;
            if (this.hallucinationEntity == null || (dist = this.minecraft.player.position().distanceTo(hallucinationEntity == null ? Vec3.ZERO : hallucinationEntity.position())) > 16 || dist < 6) {
                Entity[] entities = {
                        new Blaze(EntityType.BLAZE, this.minecraft.level),
                        new EnderMan(EntityType.ENDERMAN, this.minecraft.level),
                        new Ravager(EntityType.RAVAGER, this.minecraft.level),
                        new Spider(EntityType.SPIDER, this.minecraft.level),
                        new Illusioner(EntityType.ILLUSIONER, this.minecraft.level),
                        new Piglin(EntityType.PIGLIN, this.minecraft.level),
                        new Zombie(EntityType.ZOMBIE, this.minecraft.level),
                        new WitherSkeleton(EntityType.WITHER_SKELETON, this.minecraft.level)
                };
                hallucinationEntity = entities[(int) (entities.length * Math.random())];
                hallucinationEntity.setPos(this.minecraft.player.getX() + Mth.nextInt(RandomSource.create(), -16, 16), this.minecraft.player.getY() + 1, this.minecraft.player.getZ() + Mth.nextInt(RandomSource.create(), -16, 16));
                hallucinationEntity.xo = hallucinationEntity.getX();
                hallucinationEntity.yo = hallucinationEntity.getY();
                hallucinationEntity.zo = hallucinationEntity.getZ();
            }
            if (this.minecraft.player.isAlive())
                this.renderEntity(hallucinationEntity, camera.getPosition().x(), camera.getPosition().y(), camera.getPosition().z(), tickDelta, matrices, this.renderBuffers.outlineBufferSource());
        }
    }
}