package com.hcs.status.network;

import com.hcs.event.UseBlockEvent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerC2S {
    public static final Identifier DRINK_WATER_WITH_BARE_HAND = new Identifier("hcs", "c2s_drink_water_with_bare_hand");
    public static final Identifier ON_PLAYER_ENTER = new Identifier("hcs", "c2s_on_player_enter");

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(DRINK_WATER_WITH_BARE_HAND, (server, player, handler, buf, responseSender) -> {
            int[] bufArr = buf.readIntArray();
            server.execute(() -> {
                if (player != null && player.world != null && player.world.getEntityById(bufArr[0]) != null) {
                    Entity targetPlayer = player.world.getEntityById(bufArr[0]);
                    if (targetPlayer instanceof ServerPlayerEntity serverPlayerEntity)
                        UseBlockEvent.onDrinkWaterWithBareHand(serverPlayerEntity, new BlockPos(bufArr[1], bufArr[2], bufArr[3]));
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ON_PLAYER_ENTER, (server, player, handler, buf, responseSender) -> {
            int[] bufArr = buf.readIntArray();
            server.execute(() -> {
                //Debug when player effects reload
                if (player != null && player.world != null && player.world.getEntityById(bufArr[0]) != null) {
                    Entity targetPlayer = player.world.getEntityById(bufArr[0]);
                    if (targetPlayer instanceof ServerPlayerEntity serverPlayerEntity) {
                        List<StatusEffect> list = new ArrayList<>();
                        for (StatusEffectInstance effect : serverPlayerEntity.getStatusEffects()) {
                            StatusEffect type = effect.getEffectType();
                            if (type.getTranslationKey().contains("effect.hcs.") && type.getCategory() == StatusEffectCategory.HARMFUL)
                                list.add(type);
                        }
                        Iterator<StatusEffect> iterator = list.iterator();
                        //noinspection WhileLoopReplaceableByForEach
                        while (iterator.hasNext()) {
                            //Avoid java.util.ConcurrentModificationException: null
                            StatusEffect next = iterator.next();
                            serverPlayerEntity.removeStatusEffect(next);
                        }
                    }
                }
            });
        });
    }
}
