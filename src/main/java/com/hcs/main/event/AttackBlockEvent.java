package com.hcs.main.event;

import com.hcs.main.Reg;
import com.hcs.main.helper.EntityHelper;
import com.hcs.main.helper.RotHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.WorldEvents;

import java.util.Objects;

import static com.hcs.main.helper.EntityHelper.msgById;


public class AttackBlockEvent {
    public static void init() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            boolean isSuccessful = Math.random() < (0.25 + player.experienceLevel / 40.0);
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            BlockEntity blockEntity = world.getBlockEntity(pos);
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            ItemStack mainHandStack = player.getMainHandStack();
            Item mainHand = mainHandStack.getItem();
            if (!player.isCreative() && !world.isClient()) {
                if (blockEntity instanceof ChestBlockEntity) {
                    ChestBlock chestBlock = (ChestBlock) state.getBlock();
                    if (!Objects.requireNonNull(ChestBlock.getInventory(chestBlock, state, world, pos, true)).isEmpty()) {//break without empty
                        msgById(player, "hcs.tip.cant_break_chest", true);
                        Reg.LOGGER.info("Don't worry when the mismatch warning comes out. It is just a normal result after prevent player from attacking a nonempty chest.");
                        return ActionResult.SUCCESS;
                    }
                }

                if ((state.isToolRequired() && !state.isOf(Blocks.COBWEB)) || block == Blocks.BEDROCK) {
                    if (mainHand == Reg.ROCK) {
                        // Add sound and particles
                        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
                        mainHandStack.decrement(1);
                        if (isSuccessful) {
                            EntityHelper.dropItem(player, x, y, z, Reg.SHARP_ROCK, 1);
                            EntityHelper.msgById(player, "hcs.tip.chip_succeed", true);
                        } else EntityHelper.msgById(player, "hcs.tip.chip_failed", true);
                    } else if (mainHand == Items.FLINT) {
                        mainHandStack.decrement(1);
                        if (isSuccessful) {
                            EntityHelper.dropItem(player, x, y, z, Reg.SHARP_FLINT, 1);
                            EntityHelper.msgById(player, "hcs.tip.chip_succeed", true);
                        } else EntityHelper.msgById(player, "hcs.tip.chip_failed", true);
                    } else if (mainHand == Items.BONE) {
                        world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, pos, Block.getRawIdFromState(state));
                        mainHandStack.decrement(1);
                        EntityHelper.msgById(player, "hcs.tip.chip_succeed", true);
                        EntityHelper.dropItem(player, x, y, z, Reg.SHARP_BROKEN_BONE, 1);
                    }
                }

                if (RotHelper.canRot(mainHand)) {
                    String name = mainHandStack.getItem().getName().toString();
                    if (RotHelper.getFresh(world, mainHandStack) <= 0.0001F && RotHelper.getPackageType(name) == 1) {
                        if (name.contains("stew") || name.contains("salad") || name.contains("soup")) {
                            player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BOWL, mainHandStack.getCount()));
                            EntityHelper.dropItem(player, new ItemStack(Reg.ROT));
                        }
                        if (name.contains("bucket"))
                            player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BUCKET, mainHandStack.getCount()));
                        if (name.contains("bottle") || name.contains("juice"))
                            player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE, mainHandStack.getCount()));
                    }
                }

            }
            return ActionResult.PASS;
        });
    }
}
