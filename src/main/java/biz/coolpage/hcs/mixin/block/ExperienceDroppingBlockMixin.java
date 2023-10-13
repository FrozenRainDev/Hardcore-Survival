package biz.coolpage.hcs.mixin.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceDroppingBlock.class)
public class ExperienceDroppingBlockMixin {
    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "onStacksDropped", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/ExperienceDroppingBlock;dropExperienceWhenMined(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/intprovider/IntProvider;)V"), cancellable = true)
    public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience, CallbackInfo ci) {
        if (state != null && (state.isOf(Blocks.COAL_ORE) || state.isOf(Blocks.DEEPSLATE_COAL_ORE))) ci.cancel();
    }
}
