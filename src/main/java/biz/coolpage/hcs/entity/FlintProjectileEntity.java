package biz.coolpage.hcs.entity;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class FlintProjectileEntity extends ThrowableItemProjectile {

    public FlintProjectileEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public FlintProjectileEntity(LivingEntity owner, Level world) {
        super(Hcs.FLINT_PROJECTILE_ENTITY.get(), owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FLINT;
    }

    // Fixed bug: Renamed from "onEntityHit" to "onHitEntity" to properly override the method
    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        entity.hurt(this.level().damageSources().thrown(this, this.getOwner()), 4.0F);
        entity.playSound(SoundEvents.STONE_HIT, 2.0F, 1.0F);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            boolean isHardBlock = false;

            // Check if the projectile hit a block
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockState blockState = this.level().getBlockState(blockHitResult.getBlockPos());
                float hardness = blockState.getDestroySpeed(this.level(), blockHitResult.getBlockPos());

                // Determines if the block is considered "hard"
                // Hardness >= 1.5F (e.g., Stone) or == -1.0F (Unbreakable blocks like Bedrock)
                if ((hardness >= 1.5F && !blockState.is(BlockTags.MINEABLE_WITH_AXE)) || hardness == -1.0F) {
                    isHardBlock = true;
                }
            }

            // Always drop a sharp flint if hitting a hard block
            if (isHardBlock) {
                EntityHelper.dropItem(this, Hcs.SHARP_FLINT.get());
            } else {
                if (Math.random() < 0.85) EntityHelper.dropItem(this, Items.FLINT);
                else EntityHelper.dropItem(this, Hcs.SHARP_FLINT.get());
            }

            this.playSound(SoundEvents.STONE_HIT, 2.0F, 1.0F);
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }


    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3)
            for (int i = 0; i < 8; ++i)
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.FLINT)), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

}