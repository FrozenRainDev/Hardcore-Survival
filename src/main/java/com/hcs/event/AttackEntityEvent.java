package com.hcs.event;

import com.hcs.Reg;
import com.hcs.util.EntityHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;


public class AttackEntityEvent {
    public static void init() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof LivingEntity victim) {
                double rand = Math.random();
                ItemStack mainHandStack = player.getMainHandStack();
                if (mainHandStack.isOf(Items.STICK) && rand < 0.3) {
                    player.getMainHandStack().decrement(1);
                    player.sendToolBreakStatus(Hand.MAIN_HAND);
                    EntityHelper.dropItem(player, Reg.SHORT_STICK, 2);
                } else if (mainHandStack.isOf(Items.BONE) && rand < 0.2) {
                    player.getMainHandStack().decrement(1);
                    player.sendToolBreakStatus(Hand.MAIN_HAND);
                    EntityHelper.dropItem(player, Reg.SHARP_BROKEN_BONE,1);
                }
            }
            return ActionResult.PASS;
        });
    }
}
