package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.entity.goal.KickEnemyGoal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GoalModifierEvent {

    @SubscribeEvent
    public static void onEntityJoin(@NotNull EntityJoinLevelEvent event) {
        // Only run on logical server and for pathfinder mobs
        if (event.getEntity() instanceof PathfinderMob mob && !event.getLevel().isClientSide()) {
            List<WrappedGoal> toRemove = new ArrayList<>();
            int priority = -1;

            // Find the vanilla PanicGoal
            for (WrappedGoal wrappedGoal : mob.goalSelector.getAvailableGoals()) {
                Goal goal = wrappedGoal.getGoal();
                // Exact class match to avoid replacing subclasses unnecessarily
                if (goal.getClass() == PanicGoal.class) {
                    toRemove.add(wrappedGoal);
                    priority = wrappedGoal.getPriority();
                }
            }

            // Replace with KickEnemyGoal if found
            if (!toRemove.isEmpty()) {
                for (WrappedGoal wrappedGoal : toRemove) {
                    mob.goalSelector.removeGoal(wrappedGoal.getGoal());
                }

                // Keep the same priority and assign an estimated vanilla speed modifier
                double speed = KickEnemyGoal.getDefaultSpeed(mob);
                mob.goalSelector.addGoal(priority != -1 ? priority : 1, new KickEnemyGoal(mob, speed));
            }
        }
    }
}