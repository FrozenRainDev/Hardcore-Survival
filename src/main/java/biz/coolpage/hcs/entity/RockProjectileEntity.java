package biz.coolpage.hcs.entity;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;


public class RockProjectileEntity extends ThrowableItemProjectile {
    public RockProjectileEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public RockProjectileEntity(LivingEntity owner, Level world) {
        super(Hcs.ROCK_PROJECTILE_ENTITY.get(), owner, world);
    }

    @Deprecated
    public RockProjectileEntity(Level world, double x, double y, double z) {
        super(Hcs.ROCK_PROJECTILE_ENTITY.get(), x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Hcs.ROCK.get();
    }

    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        entity.hurt(this.level().damageSources().thrown(this, this.getOwner()), 3.0F); // deals damage
        entity.playSound(SoundEvents.STONE_HIT, 2.0F, 1.0F); // plays a sound for the entity hit only
    }

    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            if (Math.random() < 0.8) EntityHelper.dropItem(this, Hcs.ROCK.get());
            else EntityHelper.dropItem(this, Hcs.SHARP_ROCK.get());
            this.playSound(SoundEvents.STONE_HIT, 2.0F, 1.0F);
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }


    @Override
    public void handleEntityEvent(byte id) {//Particles rendering needs client world
        if (id == 3)
            for (int i = 0; i < 8; ++i)
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Hcs.ROCK.get())), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

}