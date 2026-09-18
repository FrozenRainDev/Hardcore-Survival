package biz.coolpage.hcs.mixin.entity;

import net.minecraft.world.entity.projectile.ThrownTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin replacing the former {@code public net.minecraft.world.entity.projectile.ThrownTrident f_37556_} access transformer entry.
 */
@Mixin(ThrownTrident.class)
public interface ThrownTridentAccessor {
    /**
     * Setter for the private boolean field {@code dealtDamage}.
     *
     * @param dealtDamage whether this trident already dealt damage
     */
    @Accessor("dealtDamage")
    void setDealtDamage(boolean dealtDamage);

    /**
     * Getter for the private boolean field {@code dealtDamage}.
     *
     * @return whether this trident already dealt damage
     */
    @Accessor("dealtDamage")
    boolean isDealtDamage();
}
