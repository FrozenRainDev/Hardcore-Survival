package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.block.torches.*;
import biz.coolpage.hcs.entity.BurningCrudeTorchBlockEntity;
import biz.coolpage.hcs.item.BurningCrudeTorchItem;
import biz.coolpage.hcs.item.KnifeItem;
import biz.coolpage.hcs.status.accessor.ICampfireBlockEntity;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.util.CombustionHelper;
import biz.coolpage.hcs.util.EntityHelper;
import net.minecraft.world.level.block.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemUtils; // 已修复：改为 net.minecraft.world.item.ItemUtils
import net.minecraft.world.Containers;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import static biz.coolpage.hcs.util.EntityHelper.dropItem;

@Mod.EventBusSubscriber(modid =Hcs.MOD_ID)
public class BreakBlockEvent {
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.@NotNull BreakEvent event) {
        // todo distinguish before break & after break
        Player player = event.getPlayer();
        if (EntityHelper.IS_SURVIVAL_AND_SERVER.test(player)) {
            Level world = (Level) event.getLevel();
            BlockPos pos = event.getPos();
            BlockState state = event.getState();
            Block block = state.getBlock();
            int x = pos.getX(), y = pos.getY(), z = pos.getZ();

            // Before logic
            if (state.is(Blocks.BAMBOO) && world.getBlockState(pos.below()).is(BlockTags.BAMBOO_PLANTABLE_ON) && world.getBlockState(pos.above(10)).is(Blocks.BAMBOO) && world.getBlockState(pos.east()).is(Blocks.BAMBOO) && world.getBlockState(pos.west()).is(Blocks.BAMBOO) && world.getBlockState(pos.south()).is(Blocks.BAMBOO) && world.getBlockState(pos.north()).is(Blocks.BAMBOO))
                Containers.dropItemStack(world, x, y, z, Hcs.BAMBOO_SHOOT.get().getDefaultInstance());

            boolean isBurningCrudeTorch = block instanceof BurningCrudeTorchBlock, isBurnt = block instanceof BurntTorchBlock || block instanceof WallBurntTorchBlock;
            if (!isBurnt && (block instanceof CrudeTorchBlock || isBurningCrudeTorch || block instanceof GlowstoneTorchBlock)) {
                ItemStack result = block.asItem().getDefaultInstance();
                if (isBurningCrudeTorch && world.getBlockEntity(pos) instanceof BurningCrudeTorchBlockEntity torch)
                    result.getOrCreateTag().putLong(BurningCrudeTorchItem.EXTINGUISH_NBT, torch.getExtinguishTime());
                Containers.dropItemStack(world, x + 0.5, y + 0.5, z + 0.5, result);
            }

            int silkTouch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, player.getMainHandItem());
            if (silkTouch > 0 && CombustionHelper.isFuelableCampfire(state.getBlock().asItem())) {
                ItemStack drop = new ItemStack(Items.CAMPFIRE);
                if (world.getBlockEntity(pos) instanceof ICampfireBlockEntity campfire)
                    drop.getOrCreateTag().putLong(BurningCrudeTorchItem.EXTINGUISH_NBT, campfire.getBurnOutTime());
                Containers.dropItemStack(world, x + 0.5, y + 0.5, z + 0.5, drop);
            }

            // After logic (In Forge, BreakEvent is a good place for additional drops if not canceled)
            double rand = world.getRandom().nextDouble();
            Item mainHand = player.getMainHandItem().getItem();
            if (mainHand != Items.SHEARS) {
                if (block == Blocks.GRASS) {
                    if (rand < 0.007) dropItem(player, x, y, z, Hcs.FEARLESSNESS_HERB.get(), 1);
                    else if (rand < 0.012) dropItem(player, x, y, z, Hcs.WORM.get(), 1);
                    else if (rand < 0.02) dropItem(player, x, y, z, Hcs.POTHERB.get(), 1);
                    else if (rand < 0.35) {} //dropItem(player, x, y, z, Hcs.ROCK.get(), 1);
                    else if (rand < 0.55) {} // dropItem(player, x, y, z, Hcs.GRASS_FIBER.get(), 1);
                    else if (rand < 0.7) {} // dropItem(player, x, y, z, Items.STICK, 1);
                    else if (rand < 0.7009) dropItem(player, x, y, z, Hcs.SELAGINELLA.get(), 1);
                    else if (rand < 0.7083) dropItem(player, x, y, z, Hcs.GINGER.get(), 1);
                    else if (player.getMainHandItem().getItem() instanceof KnifeItem)
                        dropItem(player, x, y, z, Hcs.GRASS_FIBER.get(), 1);
                } else if (block == Blocks.TALL_GRASS) {
                    if (rand < 0.007) dropItem(player, x, y, z, Hcs.FEARLESSNESS_HERB.get(), 2);
                    else if (rand < 0.012) dropItem(player, x, y, z, Hcs.WORM.get(), 2);
                    else if (rand < 0.02) dropItem(player, x, y, z, Hcs.POTHERB.get(), 2);
                    else if (rand < 0.35) {} // dropItem(player, x, y, z, Hcs.ROCK.get(), 2);
                    else if (rand < 0.55) {} // dropItem(player, x, y, z, Hcs.GRASS_FIBER.get(), 2);
                    else if (rand < 0.7) {} // dropItem(player, x, y, z, Items.STICK, 2);
                    else if (rand < 0.7009) dropItem(player, x, y, z, Hcs.SELAGINELLA.get(), 2);
                    else if (rand < 0.7083) dropItem(player, x, y, z, Hcs.GINGER.get(), 2);
                    else if (player.getMainHandItem().getItem() instanceof KnifeItem)
                        dropItem(player, x, y, z, Hcs.GRASS_FIBER.get(), 2);
                } else if (block == Blocks.VINE)
                    dropItem(player, x, y, z, Hcs.FIBER_STRING.get(), 1);
                else if (block instanceof LeavesBlock) {
                    if (mainHand instanceof SwordItem)
                        dropItem(player, x, y, z, Items.STICK, 1);
                }
            }
            if (mainHand == Items.WOODEN_SHOVEL && block == Blocks.GRAVEL && rand < 0.02)
                Containers.dropItemStack(world, x + 0.5, y + 0.5, z + 0.5, Hcs.RAW_COPPER_POWDER.get().getDefaultInstance());

            if (!(mainHand instanceof ShovelItem)) {
                if (block == Blocks.SNOW || block == Blocks.POWDER_SNOW)
                    dropItem(player, x, y, z, Items.SNOWBALL, 1);
                else if (block == Blocks.SNOW_BLOCK) dropItem(player, x, y, z, Items.SNOWBALL, 4);
            }
            if (!(mainHand instanceof PickaxeItem)) {
                if (block instanceof AbstractFurnaceBlock || block == Hcs.ICEBOX.get() || block instanceof BrewingStandBlock || block instanceof AnvilBlock || (block instanceof CraftingTableBlock && state.requiresCorrectToolForDrops()))
                    dropItem(player, x, y, z, block.asItem(), 1);
            }
            if (!(mainHand instanceof AxeItem)) {
                if (block == Hcs.DRYING_RACK.get()) dropItem(player, x, y, z, Hcs.DRYING_RACK_ITEM.get(), 1);
            }
            if ((block == Blocks.CACTUS || block instanceof AbstractGlassBlock || block instanceof IronBarsBlock/*todo pane block*/) && player.getMainHandItem().isEmpty()) {
                player.hurt(world.damageSources().cactus(), 2f);
                ((StatAccessor) player).getInjuryManager().addBleeding(1.2);
            } else if (block == Blocks.SWEET_BERRY_BUSH) dropItem(player, x, y, z, Hcs.BERRY_BUSH.get(), 1);
            else if (block == Blocks.CAMPFIRE) {
                if (silkTouch == 0 && state.hasProperty(CombustionHelper.COMBUST_LUMINANCE)) {
                    int stage = state.getValue(CombustionHelper.COMBUST_LUMINANCE),
                            stickCount = (int) (stage / 1.3F),
                            ashCount = (int) ((15 - stage) * 0.4F);
                    if (stickCount > 0)
                        Containers.dropItemStack(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(Items.STICK, stickCount));
                    if (ashCount > 0)
                        Containers.dropItemStack(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(Hcs.ASHES.get(), ashCount));
                }
            } else if (block == Hcs.SMOLDERING_CAMPFIRE_BLOCK.get() || block == Hcs.BURNT_CAMPFIRE_BLOCK.get()) {
                Containers.dropItemStack(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(Hcs.ASHES.get(), 6));
            }
        }
    }
}