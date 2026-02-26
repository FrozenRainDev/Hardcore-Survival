package biz.coolpage.hcs.mixin.block;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.item.HotWaterBottleItem;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.RotHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {
    @Unique
    private static final BooleanProperty NATURALLY_GENERATED = BooleanProperty.create("hcs_naturally_gen");

    @Unique
    private static boolean isNaturallyGen(BlockState state) {
        return state != null && state.getBlock() instanceof ChestBlock && state.hasProperty(NATURALLY_GENERATED) && state.getValue(NATURALLY_GENERATED);
    }

    /*
     NOTES:
     1.Could Throw error if the name of property contains upper case letters
     2.The default value of BooleanProperty is true
    */

    @Inject(method = "setPlacedBy", at = @At("HEAD"))
    private void setPlacedBy(@NotNull Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        if (world.getBlockEntity(pos) instanceof ChestBlockEntity) {
            for (BlockPos p : new BlockPos[]{pos.east(), pos.west(), pos.south(), pos.north()}) {
                if (isNaturallyGen(world.getBlockState(p))) return;
            }
            world.setBlock(pos, state.setValue(NATURALLY_GENERATED, false), 3);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(BlockState state, @NotNull Level world, BlockPos pos, @NotNull Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        ItemStack mainHandStack = player.getMainHandItem();
        Item mainHand = mainHandStack.getItem();
        if (blockEntity instanceof ChestBlockEntity && EntityHelper.IS_SURVIVAL_LIKE.test(player)) {
            if (isNaturallyGen(state)) {
                cir.setReturnValue(InteractionResult.SUCCESS);
                if (mainHand instanceof PickaxeItem) {
                    EntityHelper.msgById(player, "hcs.tip.unlocked");
                    world.setBlock(pos, state.setValue(NATURALLY_GENERATED, false), 3);
                } else EntityHelper.msgById(player, "hcs.tip.need_unlock");
            }
        }
    }

    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"))
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(NATURALLY_GENERATED);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(BlockState state, @NotNull ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!world.isClientSide()) {
            ChestBlock chestBlock = (ChestBlock) state.getBlock();
            Container inv = ChestBlock.getContainer(chestBlock, state, world, pos, true);
            if (!state.is(Reg.ICEBOX)) {
                RotHelper.update(world, inv);
                HotWaterBottleItem.update(world, inv);
            }
        }
    }

}
