package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.entity.goal.HcsAvoidEntityGoal;
import biz.coolpage.hcs.entity.goal.WolfStareGoal;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID)
public class AddMobGoalEvent {
    @SubscribeEvent
    public static void onAnimalJoinWorld(@NotNull EntityJoinLevelEvent event) {
        // AI only runs on the server side, so we ignore the client side
        if (event.getLevel().isClientSide()) {
            return;
        }

        // Check if the joined entity is an instance of Animal
        if (event.getEntity() instanceof Animal animal && !(animal instanceof NeutralMob)) {
            // Add AvoidEntityGoal to the animal's goal selector.
            // Parameters:
            // 1. Priority (4 is used by Rabbit for avoiding players)
            // 2. The goal instance (Entity, EntityClassToAvoid, MaxDistance, WalkSpeedModifier, SprintSpeedModifier)
            // Note: 2.2D is the sprint speed modifier used by rabbits. You may adjust this if animals run too fast.
            animal.goalSelector.addGoal(4, new HcsAvoidEntityGoal<>(
                    animal,
                    Player.class,
                    64.0F,
                    2.0D,
                    2.3D
            ));
        }

        if (event.getEntity() instanceof Wolf wolf) {
            // Add custom stare goal to the wolf's target selector
            // Priority is set to 3 (lower number = higher priority in execution order)
            wolf.targetSelector.addGoal(3, new WolfStareGoal(wolf));
        }
    }
}