package biz.coolpage.hcs.entity;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;


public class FlintProjectileEntity extends ThrownItemEntity {

    public FlintProjectileEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public FlintProjectileEntity(LivingEntity owner, World world) {
        super(Reg.FLINT_PROJECTILE_ENTITY, owner, world);
    }

    @Override
    public Item getDefaultItem() {
        return Items.FLINT;
    }

    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        entity.damage(entity.world.getDamageSources().thrown(this, this.getOwner()), 4.0F);
        entity.playSound(SoundEvents.BLOCK_STONE_HIT, 2.0F, 1.0F);
    }

    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.world.isClient) {
            if (Math.random() < 0.8) EntityHelper.dropItem(this, Items.FLINT);
            else EntityHelper.dropItem(this, Reg.SHARP_FLINT);
            this.playSound(SoundEvents.BLOCK_STONE_HIT, 2.0F, 1.0F);
            this.world.sendEntityStatus(this, (byte) 3);
            this.kill();
        }
    }


    @Override
    public void handleStatus(byte id) {
        if (id == 3)
            for (int i = 0; i < 8; ++i)
                this.world.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.FLINT)), this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

}
