package biz.coolpage.hcs.mixin.entity;

import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor mixin replacing the former {@code public net.minecraft.world.entity.monster.Zombie m_5884_} access transformer entry.
 */
@Mixin(Zombie.class)
public interface ZombieAccessor {
    /**
     * Invokes the protected {@code Zombie.isSunSensitive} method directly.
     *
     * @return {@code true} when this zombie burns in daylight
     */
    @Invoker("isSunSensitive")
    boolean callIsSunSensitive();
}
