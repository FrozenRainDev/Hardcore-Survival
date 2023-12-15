package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.entity.goal.ChargingAtPlayerGoal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(WitherEntity.class)
@SuppressWarnings("ConstantValue")
public abstract class WitherEntityMixin extends HostileEntity {
    protected WitherEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract boolean shouldRenderOverlay();

    @Shadow
    public abstract boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source);

    @Unique
    private int summonSkeletonCooldown = 400;

    @Unique
    private @NotNull ArrayList<BlockPos> locateSummonPosForSkeletons() {
        ArrayList<BlockPos> results = new ArrayList<>();
        if (this.world instanceof ServerWorld serverWorld) {
            var initPos = this.getBlockPos().up(10);
            for (int i = 10; i > 4; --i) {
                for (var pos : new BlockPos[]{initPos.east(i), initPos.west(i), initPos.south(i), initPos.north(i)}) {
                    //Check vertically
                    var pos1 = BlockPos.fromLong(pos.asLong()); // Clone
                    for (int j = 0; j < 21; ++j) {
                        if (!serverWorld.isOutOfHeightLimit(pos1.getY())) {
                            if (serverWorld.getBlockState(pos1).isAir() && serverWorld.getBlockState(pos1.down()).isOpaque()) {
                                if (results.size() < 4) results.add(pos1);
                                else return results;
                            }
                            pos1 = pos1.down();
                        }
                    }
                }
            }
        }
        return results;
    }

    @Inject(method = "initGoals", at = @At("HEAD"))
    protected void initGoals(CallbackInfo ci) {
        if ((Object) this instanceof WitherEntity wither)
            this.targetSelector.add(1, new ChargingAtPlayerGoal<>(wither, w -> w.shouldRenderOverlay() && w.getTarget() instanceof PlayerEntity));
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void tickMovement(CallbackInfo ci) {
        if (this.world instanceof ServerWorld serverWorld) {
            boolean shouldRenderOverlay = this.shouldRenderOverlay();
            if (this.summonSkeletonCooldown > 0) --this.summonSkeletonCooldown;
            else if (!shouldRenderOverlay) {
                if (this.getAttacker() instanceof PlayerEntity) {
                    this.summonSkeletonCooldown = 400;
                    this.locateSummonPosForSkeletons().forEach(pos -> {
                        this.world.syncWorldEvent(WorldEvents.FIRE_EXTINGUISHED, pos, 0);
                        EntityType.WITHER_SKELETON.spawn(serverWorld, pos, SpawnReason.TRIGGERED);
                    });
                }
            }
            if (shouldRenderOverlay) {
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 5, 1, false, false, false));
                this.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 5, 1, false, false, false));
            }
        }
    }
}
