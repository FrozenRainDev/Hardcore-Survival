package com.hcs.main.event;

import com.hcs.main.Reg;
import com.hcs.main.helper.EntityHelper;
import com.hcs.main.helper.RotHelper;
import com.hcs.misc.accessor.StatAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ServerEntityEvent {
    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register((
                Entity entity, ServerWorld world) -> {
            if (entity instanceof ServerPlayerEntity player) {
                RotHelper.theWorld = world;
                if (player.getLastDeathPos().isEmpty() && player.getHungerManager().getFoodLevel() == 20 && player.getHungerManager().getExhaustion() == 0.0F && ((StatAccessor) player).getThirstManager().get() == 1.0 && player.getScore() == 0 && player.totalExperience == 0 && player.getInventory().isEmpty()) {
                    //Novice gift
                    EntityHelper.dropItem(player, Reg.FLINT_KNIFE, 1);
                    EntityHelper.dropItem(player, Reg.FLINT_CONE, 1);
                    EntityHelper.dropItem(player, Items.BOWL, 1);
                    EntityHelper.dropItem(player, Reg.PURIFIED_WATER_BOTTLE, 3);
                    EntityHelper.dropItem(player, Items.BREAD, 3);
                }
            }
        });

        //Also see PlayerManagerMixin for player quit event
    }
}
