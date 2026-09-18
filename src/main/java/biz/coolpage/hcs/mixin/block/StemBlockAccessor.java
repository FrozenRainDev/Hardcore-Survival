package biz.coolpage.hcs.mixin.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.StemBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

/**
 * Accessor mixin replacing the former {@code public net.minecraft.world.level.block.StemBlock f_154726_} access transformer entry.
 */
@Mixin(StemBlock.class)
public interface StemBlockAccessor {
    /**
     * Getter for the private final {@code Supplier<Item>} field {@code seedSupplier}.
     *
     * @return the supplier of the seed item this stem grows from
     */
    @Accessor("seedSupplier")
    Supplier<Item> getSeedSupplier();
}
