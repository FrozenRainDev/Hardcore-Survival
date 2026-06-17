package biz.coolpage.hcs.entity;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.item.SpearItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ThrownSpearEntity extends ThrownTrident {
    // [S2C] Use SynchedEntityData to sync the ItemStack to the client
    private static final EntityDataAccessor<ItemStack> SPEAR_DATA = SynchedEntityData.defineId(ThrownSpearEntity.class, EntityDataSerializers.ITEM_STACK);
    private SpearItem spearItem = Hcs.STONE_SPEAR.get();

    public ThrownSpearEntity(EntityType<? extends ThrownSpearEntity> type, Level level) {
        super(type, level);
    }

    public ThrownSpearEntity(Level level, LivingEntity shooter, @NotNull ItemStack stack) {
        // Must call EntityType and Level constructor to prevent rendering as a vanilla trident
        super(Hcs.THROWN_SPEAR.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
        this.entityData.set(SPEAR_DATA, stack.copy());
        if (stack.getItem() instanceof SpearItem spear) this.spearItem = spear;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SPEAR_DATA, new ItemStack(Hcs.STONE_SPEAR.get()));
    }

    @Override
    public @NotNull ItemStack getPickupItem() {
        return this.entityData.get(SPEAR_DATA).copy();
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Spear", 10)) {
            this.entityData.set(SPEAR_DATA, ItemStack.of(tag.getCompound("Spear")));
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Spear", this.entityData.get(SPEAR_DATA).save(new CompoundTag()));
    }

    // Damage the spear upon hitting an obstacle, breaking it if durability is depleted
    private void damageSpearOnImpact() {
        ItemStack spearStack = this.entityData.get(SPEAR_DATA);
        if (!this.level().isClientSide && this.pickup != AbstractArrow.Pickup.CREATIVE_ONLY && !spearStack.isEmpty()) {
            ServerPlayer serverPlayer = this.getOwner() instanceof ServerPlayer sp ? sp : null;
            // Respect Unbreaking enchantment
            if (spearStack.hurt(1, this.random, serverPlayer)) {
                this.playSound(SoundEvents.ITEM_BREAK, 0.8F, 0.8F + this.random.nextFloat() * 0.4F);

                // Broadcast entity event (ID 3) to spawn death/break particles on the client side
                this.level().broadcastEntityEvent(this, (byte) 3);

                this.discard(); // Destroy the entity to simulate item breaking
            } else {
                // Sync updated durability
                this.entityData.set(SPEAR_DATA, spearStack.copy());
            }
        }
    }

    // Hit Block Logic
    @Override
    protected void onHitBlock(@NotNull BlockHitResult pResult) {
        super.onHitBlock(pResult);
        this.damageSpearOnImpact();
    }

    // Hit Entity Logic - Replicating parent class but with correct custom variables
    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        Entity hitEntity = pResult.getEntity();
        ItemStack spearStack = this.entityData.get(SPEAR_DATA);

        // Retrieve the damage from the base arrow logic instead of hardcoding 8.0F
        float damage = this.spearItem.getThrownDamage();

        // Calculate enchantment bonus (e.g., Impaling) referencing SPEAR_DATA instead of parent's tridentItem
        if (hitEntity instanceof LivingEntity livingEntity) {
            damage += EnchantmentHelper.getDamageBonus(spearStack, livingEntity.getMobType());
        }

        Entity owner = this.getOwner();
        DamageSource damageSource = this.damageSources().trident(this, owner == null ? this : owner);

        this.dealtDamage = true;
        SoundEvent soundEvent = SoundEvents.ARROW_HIT;

        if (hitEntity.hurt(damageSource, damage)) {
            if (hitEntity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (hitEntity instanceof LivingEntity livingHit) {
                if (owner instanceof LivingEntity livingOwner) {
                    EnchantmentHelper.doPostHurtEffects(livingHit, owner);
                    EnchantmentHelper.doPostDamageEffects(livingOwner, livingHit);
                }
                this.doPostHurtEffects(livingHit);
            }
        }

        // Bouncing off behavior
        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        float volume = 1.0F;

        // Channeling logic
        if (this.level() instanceof ServerLevel && this.level().isThundering() && EnchantmentHelper.hasChanneling(spearStack)) {
            BlockPos blockPos = hitEntity.blockPosition();
            if (this.level().canSeeSky(blockPos)) {
                LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(this.level());
                if (lightningBolt != null) {
                    lightningBolt.moveTo(Vec3.atBottomCenterOf(blockPos));
                    lightningBolt.setCause(owner instanceof ServerPlayer serverplayer ? serverplayer : null);
                    this.level().addFreshEntity(lightningBolt);
                    soundEvent = SoundEvents.TRIDENT_THUNDER;
                    volume = 5.0F;
                }
            }
        }
        this.playSound(soundEvent, volume, 1.0F);

        // Damage durability upon hitting entity
        this.damageSpearOnImpact();
    }

    // Spawns item breaking particles on the client side when receiving event ID 3
    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 3) {
            ItemStack spearStack = this.entityData.get(SPEAR_DATA);
            if (!spearStack.isEmpty()) {
                for (int i = 0; i < 8; ++i) {
                    this.level().addParticle(
                            new ItemParticleOption(ParticleTypes.ITEM, spearStack),
                            this.getX(), this.getY(), this.getZ(),
                            ((double) this.random.nextFloat() - 0.5D) * 0.1D,
                            ((double) this.random.nextFloat() - 0.5D) * 0.1D,
                            ((double) this.random.nextFloat() - 0.5D) * 0.1D
                    );
                }
            }
        } else {
            super.handleEntityEvent(pId);
        }
    }

    @Override
    protected float getWaterInertia() {
        return 0.5F;
    }
}