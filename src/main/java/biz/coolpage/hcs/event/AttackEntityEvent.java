package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.util.EntityHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_AND_SERVER;


public class AttackEntityEvent {
    public static void init() {
        // Also view PlayerEntityMixin/attack()
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof LivingEntity /*victim*/ && IS_SURVIVAL_AND_SERVER.test(player)) {
                final double rand = Math.random();
                ItemStack mainHandStack = player.getMainHandStack();
                if (mainHandStack.isOf(Reg.ROCK) && rand < 0.02) {
                    // FIXME victim.damage(player.world.getDamageSources().playerAttack(player), 1.0F); drop a viscera before death
                    player.getMainHandStack().decrement(1);
                    player.sendToolBreakStatus(Hand.MAIN_HAND);
                    EntityHelper.dropItem(player, Reg.SHARP_ROCK);
                } else if (mainHandStack.isOf(Items.STICK) && rand < 0.05) {
                    player.getMainHandStack().decrement(1);
                    player.sendToolBreakStatus(Hand.MAIN_HAND);
                    EntityHelper.dropItem(player, Reg.SHORT_STICK, 2);
                } else if (mainHandStack.isOf(Items.BONE) && rand < 0.02) {
                    player.getMainHandStack().decrement(1);
                    player.sendToolBreakStatus(Hand.MAIN_HAND);
                    EntityHelper.dropItem(player, Reg.SHARP_BROKEN_BONE, 1);
                }
                int panic = EntityHelper.getEffectAmplifier(player, HcsEffects.PANIC);
                if (panic > -1 && rand < Math.min(0.6, (panic + 1) / HcsDifficulty.chooseVal(player, -1.0/*impossible*/, 15.0, 9.0))) {
                    EntityHelper.msgById(player, "hcs.tip.attack_failed");
                    return ActionResult.FAIL;
                }
                if (EntityHelper.IS_BAREHANDED.and(IS_SURVIVAL_AND_SERVER).test(player) && rand < 0.3) {
                    EntityHelper.msgById(player, "hcs.tip.hurt_hand_attack");
                    player.damage(player.world.getDamageSources().cactus(), 0.3F);
                }
            }
            return ActionResult.PASS;
        });
    }
}
