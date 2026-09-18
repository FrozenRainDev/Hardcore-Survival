package biz.coolpage.hcs.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FarmBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor mixin replacing the former {@code public net.minecraft.world.level.block.FarmBlock m_53258_} access transformer entry.
 */
@Mixin(FarmBlock.class)
public interface FarmBlockAccessor {
    /**
     * Invokes the private static {@code FarmBlock.isNearWater} method directly.
     *
     * @param level the level reader to inspect
     * @param pos the position whose surroundings are checked
     * @return {@code true} when water is found near the given position
     */
    @Invoker("isNearWater")
    static boolean callIsNearWater(LevelReader level, BlockPos pos) {
        throw new AssertionError();
    }
}
