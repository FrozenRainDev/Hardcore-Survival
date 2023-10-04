package com.hcs.event;

import com.hcs.Reg;
import com.hcs.status.HcsEffects;
import com.hcs.status.accessor.StatAccessor;
import com.hcs.status.manager.TemperatureManager;
import com.hcs.util.EntityHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

public class UseBlockEvent {
    // For more use block events, view mixin/item
    private static HitResult debugger;

    public static void init() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!player.isSpectator() && !world.isClient) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                Material material = state.getMaterial();
                BlockPos posUp = pos.up(1);
                BlockState stateUp = world.getBlockState(posUp);
                Block blockUp = stateUp.getBlock();
                Material materialUp = stateUp.getMaterial();
                ItemStack mainHandStack = player.getMainHandStack();
                Item mainHand = mainHandStack.getItem();
                //prevent the method from being called twice after a single action
                if (Objects.equals(debugger, hitResult)) return ActionResult.PASS;
                //for Items.SWEET_BERRIES, see at BlockItemMixin
                if ((block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || material == Material.REPLACEABLE_PLANT) && (materialUp == Material.REPLACEABLE_PLANT || blockUp == Blocks.AIR || blockUp == Blocks.CAVE_AIR) && mainHand == Reg.BERRY_BUSH) {
                    if (!player.isCreative()) mainHandStack.decrement(1);
                    world.setBlockState(material == Material.REPLACEABLE_PLANT ? (world.getBlockState(pos.down(1)).getMaterial() == Material.REPLACEABLE_PLANT ? pos.down(1) : pos) : posUp, Blocks.SWEET_BERRY_BUSH.getDefaultState());
                }
                if (mainHand == Items.POISONOUS_POTATO && block == Blocks.FARMLAND) {
                    world.setBlockState(posUp, Blocks.POTATOES.getDefaultState());
                    if (!player.isCreative()) mainHandStack.decrement(1);
                }
                if (block instanceof BedBlock && (EntityHelper.getEffectAmplifier(player, HcsEffects.PAIN) > 0)) {
                    EntityHelper.msgById(player, "hcs.tip.too_pain_to_sleep");
                    return ActionResult.FAIL;
                }
                debugger = hitResult;
            }
            return ActionResult.PASS;
        });
    }

    public static void onDrinkWaterWithBareHand(ServerPlayerEntity player, BlockPos pos) {
        if (player == null || pos == null) {
            Reg.LOGGER.error("UseBlockEvent/onDrinkWaterWithBareHand;player==null||pos==null");
            return;
        }
        if (player.isSneaking() && player.getMainHandStack().isEmpty() && player.getOffHandStack().isEmpty()) {
            ((StatAccessor) player).getThirstManager().addDirectly(0.05);
            TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
            if (temperatureManager.get() > 0.8) temperatureManager.add(-0.005);
            if (player.world.getBiome(pos).isIn(BiomeTags.IS_OCEAN) || player.world.getBiome(pos).isIn(BiomeTags.IS_DEEP_OCEAN) || player.world.getBiome(pos).isIn(BiomeTags.IS_BEACH)) {
                if (player.hasStatusEffect(HcsEffects.THIRST))
                    player.addStatusEffect(new StatusEffectInstance(HcsEffects.THIRST, Math.min(Objects.requireNonNull(player.getStatusEffect(HcsEffects.THIRST)).getDuration() + 200, 9600), 0, false, false, true));
                else
                    player.addStatusEffect(new StatusEffectInstance(HcsEffects.THIRST, 1200, 0, false, false, true));
            } else player.addStatusEffect(new StatusEffectInstance(HcsEffects.DIARRHEA, 600, 0, false, false, true));
        }
    }

}
