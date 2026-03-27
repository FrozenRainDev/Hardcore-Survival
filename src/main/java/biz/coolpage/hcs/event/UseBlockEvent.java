package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StaminaManager;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.effect.MobEffectInstance; // 已更名: StatusEffectInstance -> MobEffectInstance
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID)
public class UseBlockEvent {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && !serverPlayer.isSpectator()) {
            StaminaManager staminaManager = ((StatAccessor) serverPlayer).getStaminaManager();
            if (staminaManager.get() <= 0.005F) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                return;
            }
            Level world = event.getLevel();
            BlockPos pos = event.getPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            BlockPos posUp = pos.above(1);
            BlockState stateUp = world.getBlockState(posUp);
            Block blockUp = stateUp.getBlock();
            ItemStack mainHandStack = serverPlayer.getMainHandItem();
            Item mainHand = mainHandStack.getItem();

            if ((block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || state.canBeReplaced()) && (stateUp.canBeReplaced() || blockUp == Blocks.AIR || blockUp == Blocks.CAVE_AIR) && mainHand == Hcs.BERRY_BUSH) {
                if (!serverPlayer.isCreative()) mainHandStack.shrink(1);
                world.setBlockAndUpdate(state.canBeReplaced() ? (world.getBlockState(pos.below(1)).canBeReplaced() ? pos.below(1) : pos) : posUp, Blocks.SWEET_BERRY_BUSH.defaultBlockState());
            }
            if (mainHand == Items.POISONOUS_POTATO && block == Blocks.FARMLAND) {
                world.setBlockAndUpdate(posUp, Blocks.POTATOES.defaultBlockState());
                if (!serverPlayer.isCreative()) mainHandStack.shrink(1);
            }
            if (block instanceof BedBlock && applyNullable(world.dimensionType(), d -> d.bedWorks(), false)) {
                boolean b1 = EntityHelper.getEffectAmplifier(serverPlayer, HcsEffects.PAIN) > 0;
                boolean b2 = ((StatAccessor) serverPlayer).getSanityManager().get() < 0.15;
                int hour = WorldHelper.getTimeAsReal(world)[0];
                boolean b3 = hour > 6 && hour < 21 && world.isNight();
                if (b1 || b2 || b3) {
                    if (b1) EntityHelper.msgById(serverPlayer, "hcs.tip.too_pain_to_sleep");
                    else if (b2) EntityHelper.msgById(serverPlayer, "hcs.tip.insanity_insomnia");
                    else EntityHelper.msgById(serverPlayer, "hcs.tip.too_early_to_sleep");
                    serverPlayer.setRespawnPosition(world.dimension(), pos, 0.0f, false, true);
                    EntityHelper.msgById(serverPlayer, "block.minecraft.set_spawn", false);
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.FAIL);
                }
            }
        }
    }

    public static void onDrinkWaterWithBareHand(ServerPlayer player, BlockPos pos) {
        if (player == null || pos == null) {
            Hcs.error("UseBlockEvent/onDrinkWaterWithBareHand;player==null||pos==null"); // 已改为 Hcs.error
            return;
        }
        if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) {
            ((StatAccessor) player).getThirstManager().addDirectly(0.05);
            TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
            if (temperatureManager.get() > 0.8) temperatureManager.add(-0.005);
            if (WorldHelper.IS_SALTY_WATER_BIOME.test(player.level().getBiome(pos))) {
                if (player.hasEffect(HcsEffects.THIRST))
                    player.addEffect(new MobEffectInstance(HcsEffects.THIRST, Math.min(Objects.requireNonNull(player.getEffect(HcsEffects.THIRST)).getDuration() + 200, 9600), 0, false, false, true));
                else
                    player.addEffect(new MobEffectInstance(HcsEffects.THIRST, 1200, 0, false, false, true));
            } else {
                double rand = Math.random();
                if (rand < 0.0001) ((StatAccessor) player).getDiseaseManager().addParasite(0.12);
                else player.addEffect(new MobEffectInstance(HcsEffects.DIARRHEA, 600, 0, false, false, true));
            }
        }
    }
}