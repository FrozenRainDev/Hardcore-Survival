package biz.coolpage.hcs.mixin.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BambooStalkBlock.class)
public abstract class BambooBlockMixin extends Block {
    public BambooBlockMixin(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Inject(at = @At("HEAD"), method = "getDestroyProgress", cancellable = true)
    public void getDestroyProgress(BlockState state, @NotNull Player player, BlockGetter world, BlockPos pos, @NotNull CallbackInfoReturnable<Float> cir) {
        if (player.getMainHandItem().getItem() instanceof SwordItem) cir.setReturnValue(0.04F);
    }
}
