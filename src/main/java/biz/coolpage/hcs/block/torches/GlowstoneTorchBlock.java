package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Hcs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class GlowstoneTorchBlock extends TorchBlock {
    public GlowstoneTorchBlock(Properties settings) {
        super(settings, new DustParticleOptions(new Vector3f(1.0f, 1.0f, 0.0f), 1.0f));
    }

    @Override
    public Item asItem() {
        return Hcs.GLOWSTONE_TORCH_ITEM;
    }

    @Override
    protected Block asBlock() {
        return Hcs.GLOWSTONE_TORCH_BLOCK;
    }

    @Override
    public void animateTick(BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull RandomSource random) {
        double d = (double) pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
        double e = (double) pos.getY() + 0.7 + (random.nextDouble() - 0.5) * 0.2;
        double f = (double) pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
        world.addParticle(this.flameParticle, d, e, f, 0.0, 0.0, 0.0);
    }
}