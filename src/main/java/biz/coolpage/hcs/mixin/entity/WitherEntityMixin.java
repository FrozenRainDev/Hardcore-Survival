package biz.coolpage.hcs.mixin.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(WitherEntity.class)
public abstract class WitherEntityMixin extends HostileEntity {
    protected WitherEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract boolean shouldRenderOverlay();

    @Unique
    private int summonSkeletonCooldown = 100;

    @Unique
    private @NotNull ArrayList<BlockPos> locateSummonPosForSkeletons() {
        ArrayList<BlockPos> results = new ArrayList<>();
        if (this.world instanceof ServerWorld serverWorld) {
            var initPos = this.getBlockPos().up(10);
            for (int i = 5; i >= 0; --i) {
                for (var pos : new BlockPos[]{initPos.east(i), initPos.west(i), initPos.south(i), initPos.north(i)}) {
                    //Check vertically
                    var pos1 = BlockPos.fromLong(pos.asLong()); // Clone
                    for (int j = 0; j < 11; ++j) {
                        if (!serverWorld.isOutOfHeightLimit(pos1.getY())) {
                            if (serverWorld.getBlockState(pos1).isAir() && serverWorld.getBlockState(pos1.down()).isOpaque()
                                //TODO invalid SpawnHelper.canSpawn(SpawnRestriction.Location.ON_GROUND, serverWorld, pos1.down(), EntityType.WITHER_SKELETON)
                            ) {
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

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void tickMovement(CallbackInfo ci) {
        if (this.world instanceof ServerWorld serverWorld) {
            if (this.summonSkeletonCooldown > 0) --this.summonSkeletonCooldown;
            else if (!this.shouldRenderOverlay()) {
                this.summonSkeletonCooldown = (int) ((2 - this.getHealth() / this.getMaxHealth()) * 100);
                this.locateSummonPosForSkeletons().forEach(pos -> EntityType.WITHER_SKELETON.spawn(serverWorld, pos, SpawnReason.TRIGGERED));
            }
        }
    }
}
