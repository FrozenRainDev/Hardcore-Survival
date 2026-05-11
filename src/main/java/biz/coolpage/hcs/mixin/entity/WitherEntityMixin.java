package biz.coolpage.hcs.mixin.entity;

import biz.coolpage.hcs.entity.goal.ChargingAtPlayerGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(WitherBoss.class)
@SuppressWarnings("ConstantValue")
public abstract class WitherEntityMixin extends Monster {
    protected WitherEntityMixin(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
    }

    @Shadow
    public abstract boolean isPowered();

    @Shadow
    public abstract boolean addEffect(@NotNull MobEffectInstance effect, @Nullable Entity source);

    @Unique
    private int summonSkeletonCooldown = 400;

    @Unique
    private @NotNull ArrayList<BlockPos> locateSummonPosForSkeletons() {
        ArrayList<BlockPos> results = new ArrayList<>();
        if (this.level() instanceof ServerLevel serverWorld) {
            var initPos = this.blockPosition().above(10);
            for (int i = 10; i > 4; --i) {
                for (var pos : new BlockPos[]{initPos.east(i), initPos.west(i), initPos.south(i), initPos.north(i)}) {
                    //Check vertically
                    var pos1 = BlockPos.of(pos.asLong()); // Clone
                    for (int j = 0; j < 21; ++j) {
                        if (!serverWorld.isOutsideBuildHeight(pos1.getY())) {
                            if (serverWorld.getBlockState(pos1).isAir() && serverWorld.getBlockState(pos1.below()).canOcclude()) {
                                if (results.size() < 4) results.add(pos1);
                                else return results;
                            }
                            pos1 = pos1.below();
                        }
                    }
                }
            }
        }
        return results;
    }

    @Inject(method = "registerGoals", at = @At("HEAD"))
    protected void initGoals(CallbackInfo ci) {
        if ((Object) this instanceof WitherBoss wither)
            this.targetSelector.addGoal(1, new ChargingAtPlayerGoal<>(wither, w -> w.isPowered() && w.getTarget() instanceof Player));
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    public void tickMovement(CallbackInfo ci) {
        if (this.level() instanceof ServerLevel serverWorld) {
            boolean isPowered = this.isPowered();
            if (this.summonSkeletonCooldown > 0) --this.summonSkeletonCooldown;
            else if (!isPowered) {
                if (this.getLastAttacker() instanceof Player) {
                    this.summonSkeletonCooldown = 400;
                    this.locateSummonPosForSkeletons().forEach(pos -> {
                        this.level().levelEvent(LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0);
                        EntityType.WITHER_SKELETON.spawn(serverWorld, pos, MobSpawnType.TRIGGERED);
                    });
                }
            }
            if (isPowered) {
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5, 1, false, false, false), null);
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 5, 1, false, false, false), null);
            }
        }
    }
}