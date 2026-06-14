package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
//import biz.coolpage.hcs.entity.goal.ZombieBreakBlockGoal;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID)
public class ZombieGoalEvents {

    @SubscribeEvent
    public static void onEntityJoinLevel(@NotNull EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof Zombie zombie)) {
            return;
        }

        boolean replaced = false;

        // dont have at all shit if (wrappedGoal.getGoal() instanceof BreakDoorGoal) {

        // Iterate through a copy of the goals to avoid ConcurrentModificationException
//        for (WrappedGoal wrappedGoal : new ArrayList<>(zombie.goalSelector.getAvailableGoals())) {
//            System.out.println("onEntityJoinLevel " + wrappedGoal.getGoal().getClass());
//            if (wrappedGoal.getGoal() instanceof BreakDoorGoal) {
//                zombie.goalSelector.removeGoal(wrappedGoal.getGoal());
//                System.out.println("onEntityJoinLevel Removed door");
//                replaced = true;
//            }
//        }
//
//        // Add the custom goal with the same priority (1) as the vanilla BreakDoorGoal
//        if (replaced) {
//            zombie.goalSelector.addGoal(1, new ZombieBreakBlockGoal(zombie));
//        }
    }
}