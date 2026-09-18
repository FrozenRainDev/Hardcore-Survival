package biz.coolpage.hcs.mixin.block;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor mixin replacing the former {@code public net.minecraft.world.level.block.ComposterBlock m_51920_} access transformer entry.
 */
@Mixin(ComposterBlock.class)
public interface ComposterBlockAccessor {
    /**
     * Invokes the private static {@code ComposterBlock.add} method directly.
     *
     * @param levelIncreaseChance the chance that the composter level increases
     * @param item the item to register as compostable
     */
    @Invoker("add")
    static void callAdd(float levelIncreaseChance, ItemLike item) {
        throw new AssertionError();
    }
}
