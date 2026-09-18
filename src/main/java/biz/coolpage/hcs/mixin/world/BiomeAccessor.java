package biz.coolpage.hcs.mixin.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor mixin replacing the former {@code public net.minecraft.world.level.biome.Biome m_47505_} access transformer entry.
 *
 * <p>{@link Biome} is final, so call sites must cast through {@code Object} before casting to this interface.</p>
 */
@Mixin(Biome.class)
public interface BiomeAccessor {
    /**
     * Invokes the private {@code Biome.getTemperature} method directly, including the biome's temperature modifier.
     *
     * @param pos the position the temperature is sampled at
     * @return the effective temperature of this biome at the given position
     */
    @Invoker("getTemperature")
    float callGetTemperature(BlockPos pos);
}
