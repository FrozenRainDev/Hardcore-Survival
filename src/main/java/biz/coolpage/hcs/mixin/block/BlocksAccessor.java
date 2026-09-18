package biz.coolpage.hcs.mixin.block;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.ToIntFunction;

/**
 * Accessor mixin replacing the former {@code public net.minecraft.world.level.block.Blocks m_50759_} access transformer entry.
 */
@Mixin(Blocks.class)
public interface BlocksAccessor {
    /**
     * Invokes the private static {@code Blocks.litBlockEmission} method directly.
     *
     * @param lightValue the light emission value used while the block is lit
     * @return a light level function honouring the {@code LIT} block state property
     */
    @Invoker("litBlockEmission")
    static ToIntFunction<BlockState> callLitBlockEmission(int lightValue) {
        throw new AssertionError();
    }
}
