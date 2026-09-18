package biz.coolpage.hcs.mixin.entity;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor mixin replacing the former {@code public net.minecraft.world.entity.LivingEntity m_5639_} access transformer entry.
 */
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    /**
     * Invokes the protected {@code LivingEntity.calculateFallDamage} method directly.
     *
     * @param distance the fall distance
     * @param damageMultiplier the damage multiplier of the block landed on
     * @return the fall damage that would be dealt
     */
    @Invoker("calculateFallDamage")
    int callCalculateFallDamage(float distance, float damageMultiplier);
}
