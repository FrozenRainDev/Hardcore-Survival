package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.item.LeavesReinforcerItem;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin extends Block {
    public LeavesBlockMixin(Properties pProperties) {
        super(pProperties);
    }

    // Yarn decaying() typically corresponds to isRandomlyTicking or manual distance check in Mojang, and the logic structure is retained here.
    @Shadow
    public abstract boolean isRandomlyTicking(BlockState state);

    @Inject(method = "randomTick", at = @At("TAIL"))
    void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (state == null || world == null) return;
        int dropItem = 0;
        float temp = world.getBiome(pos).value().getBaseTemperature();
        if (this.isRandomlyTicking(state)) dropItem = 1;
        else if (!state.getValue(BlockStateProperties.PERSISTENT) && world.getBlockState(pos.below()).isAir() && Math.random() < (0.001 * Math.pow(Math.abs(temp) + 0.05, 2)))
            dropItem = 2;
        if (dropItem > 0) {
            BlockPos pos1 = dropItem == 2 ? pos.below() : pos;
            if (Math.random() < 0.008 && temp >= 0.8)
                EntityHelper.dropItem(world, pos1, Hcs.ORANGE.get()); // Reg -> Hcs
            else if (Math.random() < 0.003) EntityHelper.dropItem(world, pos1, Items.APPLE);
            else if (Math.random() < 0.005) EntityHelper.dropItem(world, pos1, Items.STICK);
        }
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    protected void hcs$injectReinforcedProperty(StateDefinition.@NotNull Builder<Block, BlockState> pBuilder, CallbackInfo ci) {
        // Inject the custom property into leaves block states
        pBuilder.add(LeavesReinforcerItem.REINFORCED_LEAVES);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hcs$setDefaultState(BlockBehaviour.Properties pProperties, CallbackInfo ci) {
        // Set the default state of the custom property to false
        this.registerDefaultState(this.defaultBlockState().setValue(LeavesReinforcerItem.REINFORCED_LEAVES, false));
    }
}