package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.block.torches.CrudeTorchBlock;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.StaminaManager;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.WorldHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

import java.util.Objects;

import static biz.coolpage.hcs.util.CommUtil.applyNullable;

public class UseBlockEvent {
    // For more onInteract block events, view mixin/item
    private static HitResult debugger;

    public static void init() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player != null && !player.isSpectator() && player instanceof ServerPlayerEntity serverPlayer) {
                StaminaManager staminaManager = ((StatAccessor) serverPlayer).getStaminaManager();
                if (staminaManager.get() <= 0.005F) return ActionResult.FAIL;
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
                } else if ((mainHand == Reg.CRUDE_TORCH_ITEM || mainHand == Reg.UNLIT_TORCH_ITEM)
                        && (state.isIn(BlockTags.FIRE) || state.isIn(BlockTags.CAMPFIRES)
                        || block instanceof TorchBlock || CrudeTorchBlock.isFlammableTorch(block.asItem()))
                        || block == Blocks.LAVA || block == Blocks.MAGMA_BLOCK
                        || (block instanceof AbstractFurnaceBlock && state.get(Properties.LIT))) {
                    mainHandStack.decrement(1);
                    EntityHelper.dropItem(player, mainHand == Reg.CRUDE_TORCH_ITEM ? Reg.BURNING_CRUDE_TORCH_ITEM : Items.TORCH);
                    world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS);
                    return ActionResult.FAIL;
                }
                if (block instanceof BedBlock && applyNullable(world.getDimension(), DimensionType::bedWorks, false)) {
                    //HCS sleeping failure reasons
                    boolean b1 = EntityHelper.getEffectAmplifier(player, HcsEffects.PAIN) > 0;
                    boolean b2 = ((StatAccessor) player).getSanityManager().get() < 0.15;
                    int hour = WorldHelper.getTimeAsReal(world)[0];
                    boolean b3 = hour > 6 && hour < 21 && world.isNight();
                    if (b1 || b2 || b3) {
                        if (b1) EntityHelper.msgById(player, "hcs.tip.too_pain_to_sleep");
                        else if (b2) EntityHelper.msgById(player, "hcs.tip.insanity_insomnia");
                        else EntityHelper.msgById(player, "hcs.tip.too_early_to_sleep");
                        serverPlayer.setSpawnPoint(world.getRegistryKey(), pos, 0.0f, false, true);
                        EntityHelper.msgById(player, "block.minecraft.set_spawn", false);
                        return ActionResult.FAIL;
                    }
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
            if (WorldHelper.IS_SALTY_WATER_BIOME.test(player.world.getBiome(pos))) {
                if (player.hasStatusEffect(HcsEffects.THIRST))
                    player.addStatusEffect(new StatusEffectInstance(HcsEffects.THIRST, Math.min(Objects.requireNonNull(player.getStatusEffect(HcsEffects.THIRST)).getDuration() + 200, 9600), 0, false, false, true));
                else
                    player.addStatusEffect(new StatusEffectInstance(HcsEffects.THIRST, 1200, 0, false, false, true));
            } else {
                double rand = Math.random();
                if (rand < 0.01) ((StatAccessor) player).getDiseaseManager().addParasite(0.12);
                else if (rand < 0.03)
                    player.addStatusEffect(new StatusEffectInstance(HcsEffects.FOOD_POISONING, 1200, 0, false, false, true));
                else player.addStatusEffect(new StatusEffectInstance(HcsEffects.DIARRHEA, 600, 0, false, false, true));
            }
        }
    }

}
