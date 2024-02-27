package biz.coolpage.hcs.block.torches;

import biz.coolpage.hcs.Reg;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.item.Item;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class GlowstoneTorchBlock extends TorchBlock {
    public GlowstoneTorchBlock(Settings settings) {
        super(settings, new DustParticleEffect(Vec3d.unpackRgb(0xffff00).toVector3f(), 1.0f));
    }

    @Override
    public Item asItem() {
        return Reg.GLOWSTONE_TORCH_ITEM;
    }

    @Override
    protected Block asBlock() {
        return Reg.GLOWSTONE_TORCH_BLOCK;
    }

    @Override
    public void randomDisplayTick(BlockState state, @NotNull World world, @NotNull BlockPos pos, @NotNull Random random) {
        double d = (double) pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
        double e = (double) pos.getY() + 0.7 + (random.nextDouble() - 0.5) * 0.2;
        double f = (double) pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
        world.addParticle(this.particle, d, e, f, 0.0, 0.0, 0.0);
    }

}
