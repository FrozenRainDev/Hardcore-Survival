package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static biz.coolpage.hcs.util.EntityHelper.IS_SURVIVAL_AND_SERVER;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID)
public class AttackEntityEventListener { // 修改了类名，避免冲突

    @SubscribeEvent
    public static void onAttackEntity(net.minecraftforge.event.entity.player.AttackEntityEvent event) {
        Player player = event.getEntity();
        if (event.getTarget() instanceof LivingEntity && IS_SURVIVAL_AND_SERVER.test(player)) {
            final double rand = Math.random();
            ItemStack mainHandStack = player.getMainHandItem();

            if (mainHandStack.is(Hcs.ROCK.get()) && rand < 0.02) {
                player.getMainHandItem().shrink(1);
                player.broadcastBreakEvent(InteractionHand.MAIN_HAND);
                EntityHelper.dropItem(player, Hcs.SHARP_ROCK.get());
            } else if (mainHandStack.is(Items.STICK) && rand < 0.05) {
                player.getMainHandItem().shrink(1);
                player.broadcastBreakEvent(InteractionHand.MAIN_HAND);
                EntityHelper.dropItem(player, Hcs.SHORT_STICK.get(), 2);
            } else if (mainHandStack.is(Items.BONE) && rand < 0.02) {
                player.getMainHandItem().shrink(1);
                player.broadcastBreakEvent(InteractionHand.MAIN_HAND);
                EntityHelper.dropItem(player, Hcs.SHARP_BROKEN_BONE.get(), 1);
            }

            int panic = EntityHelper.getEffectAmplifier(player, HcsEffects.PANIC.get());
            if (panic > -1 && rand < Math.min(0.6, (panic + 1) / HcsDifficulty.chooseVal(player, -1.0, 15.0, 9.0))) {
                EntityHelper.msgById(player, "hcs.tip.attack_failed");
                event.setCanceled(true); // 现在可以正确识别该方法
                return;
            }

            if (EntityHelper.IS_BAREHANDED.and(IS_SURVIVAL_AND_SERVER).test(player) && rand < 0.3) {
                EntityHelper.msgById(player, "hcs.tip.hurt_hand_attack");
                player.hurt(player.level().damageSources().generic(), 0.3F);
            }
        }
    }
}