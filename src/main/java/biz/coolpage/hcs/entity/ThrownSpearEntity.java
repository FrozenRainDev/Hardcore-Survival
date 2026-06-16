package biz.coolpage.hcs.entity;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.item.SpearItem;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ThrownSpearEntity extends ThrownTrident {
    // [S2C] Use SynchedEntityData to sync the ItemStack to the client
    // Declaring a variable as static would make all instances of the class share that variable, but this is not the case in Minecraft's entity data synchronization (SynchedEntityData) system. Making it static is not only not for sharing data among instances, but it's actually the correct way required by Minecraft.
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
        // Independently saves the item stack to override vanilla trident property
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

    // In the Trident code, there is no damage bonus based on speed.
    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        Entity entity = pResult.getEntity();
        ItemStack spearItem = this.entityData.get(SPEAR_DATA);

        // [Crucial Fix] Retrieve the damage from the base arrow logic instead of hardcoding 8.0F
        float damage = this.spearItem.getThrownDamage();

        // Calculate enchantment bonus (e.g., Impaling)
        // Note: Change 'this.tridentItem' to whatever variable name you use to store the Spear ItemStack in your entity
        if (entity instanceof LivingEntity livingEntity) {
            damage += EnchantmentHelper.getDamageBonus(spearItem, livingEntity.getMobType());
        }

        Entity owner = this.getOwner();
        // You might want to create a custom DamageSource for your spear later,
        // but using trident damage source works for now.
        DamageSource damageSource = this.damageSources().trident(this, owner == null ? this : owner);

        this.dealtDamage = true;
        SoundEvent soundEvent = SoundEvents.ARROW_HIT;  // SoundEvents.TRIDENT_HIT; // Replace with your custom hit sound if needed

        if (entity.hurt(damageSource, damage)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (entity instanceof LivingEntity hitEntity) {
                if (owner instanceof LivingEntity livingOwner) {
                    EnchantmentHelper.doPostHurtEffects(hitEntity, owner);
                    EnchantmentHelper.doPostDamageEffects(livingOwner, hitEntity);
                }
                this.doPostHurtEffects(hitEntity);
            }
        }

        // Bouncing off behavior
        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        float volume = 1.0F;

        // Channeling logic (optional: you can keep or remove this depending on if your spear supports Channeling)
        if (this.level() instanceof ServerLevel && this.level().isThundering() && this.isChanneling()) {
            BlockPos blockPos = entity.blockPosition();
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
    }
}