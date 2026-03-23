package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs; // Reg -> Hcs
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID)
public class AttackBlockEvent {
    // todo test
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        ItemStack mainHandStack = player.getMainHandItem();
        Item mainHand = mainHandStack.getItem();

        if (EntityHelper.IS_SURVIVAL_AND_SERVER.test(player)) {
            if (blockEntity instanceof ChestBlockEntity) {
                ChestBlock chestBlock = (ChestBlock) state.getBlock();
                if (!Objects.requireNonNull(ChestBlock.getContainer(chestBlock, state, world, pos, true)).isEmpty()) {
                    EntityHelper.msgById(player, "hcs.tip.cant_break_chest");
                    Hcs.info("Don't worry when the mismatch warning comes out. It is just a normal result after prevent player from attacking a nonempty chest.");
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }
            }

            if ((state.requiresCorrectToolForDrops() && !state.is(Blocks.COBWEB)) || block == Blocks.BEDROCK) {
                if (mainHand == Items.BONE) {
                    world.levelEvent(2001, pos, Block.getId(state));
                    mainHandStack.shrink(1);
                    EntityHelper.msgById(player, "hcs.tip.chip_succeed");
                    EntityHelper.dropItem(player, Hcs.SHARP_BROKEN_BONE);
                }
            }
        }
    }
}