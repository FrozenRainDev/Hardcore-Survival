package biz.coolpage.hcs.mixin.fluid;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LavaFluid.class)
public class LavaFluidMixin {
    @Inject(method = "onRandomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isAir()Z"))
    public void onRandomTick(World world, @NotNull BlockPos pos, FluidState state, Random random, CallbackInfo ci) {
        for (BlockPos p : new BlockPos[]{pos.east(), pos.west(), pos.south(), pos.north()}) {
            BlockState stat = world.getBlockState(p);
            if (stat.isIn(BlockTags.CAMPFIRES) && stat.contains(CampfireBlock.LIT) && !stat.get(CampfireBlock.LIT)) {
                world.setBlockState(p, stat.with(CampfireBlock.LIT, true));
                break;
            }
        }
    }
}
