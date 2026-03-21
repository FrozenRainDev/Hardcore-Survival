package biz.coolpage.hcs.mixin.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LavaFluid.class)
public class LavaFluidMixin {
    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    public void randomTick(Level level, @NotNull BlockPos pos, FluidState state, RandomSource random, CallbackInfo ci) {
        for (BlockPos p : new BlockPos[]{pos.east(), pos.west(), pos.south(), pos.north()}) {
            BlockState stat = level.getBlockState(p);
            if (stat.is(BlockTags.CAMPFIRES) && stat.hasProperty(CampfireBlock.LIT) && !stat.getValue(CampfireBlock.LIT)) {
                level.setBlock(p, stat.setValue(CampfireBlock.LIT, true), 3);
                break;
            }
        }
    }
}