package biz.coolpage.hcs.event;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.block.torches.*;
import biz.coolpage.hcs.entity.BurningCrudeTorchBlockEntity;
import biz.coolpage.hcs.item.BurningCrudeTorchItem;
import biz.coolpage.hcs.item.KnifeItem;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.util.CombustionHelper;
import biz.coolpage.hcs.util.EntityHelper;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ItemScatterer;


public class BreakBlockEvent {
    public static void init() {
        PlayerBlockBreakEvents.BEFORE.register(((world, player, pos, state, blockEntity) -> {
            if (EntityHelper.IS_SURVIVAL_AND_SERVER.test(player)) {
                Block block = state.getBlock();
                int x = pos.getX(), y = pos.getY(), z = pos.getZ();
                if (state.isOf(Blocks.BAMBOO) && world.getBlockState(pos.down()).isIn(BlockTags.BAMBOO_PLANTABLE_ON) && world.getBlockState(pos.up(10)).isOf(Blocks.BAMBOO) && world.getBlockState(pos.east()).isOf(Blocks.BAMBOO) && world.getBlockState(pos.west()).isOf(Blocks.BAMBOO) && world.getBlockState(pos.south()).isOf(Blocks.BAMBOO) && world.getBlockState(pos.north()).isOf(Blocks.BAMBOO))
                    ItemScatterer.spawn(world, x, y, z, Reg.BAMBOO_SHOOT.getDefaultStack());
                boolean isBurningCrudeTorch = block instanceof BurningCrudeTorchBlock, isBurnt = block instanceof BurntTorchBlock || block instanceof WallBurntTorchBlock;
                if (!isBurnt && (block instanceof CrudeTorchBlock || isBurningCrudeTorch || block instanceof GlowstoneTorchBlock)) {
                    ItemStack result = block.asItem().getDefaultStack();
                    if (isBurningCrudeTorch && world.getBlockEntity(pos) instanceof BurningCrudeTorchBlockEntity torch)
                        result.getOrCreateNbt().putLong(BurningCrudeTorchItem.LIT_NBT, torch.getLastLitTime());
                    ItemScatterer.spawn(world, x + 0.5, y + 0.5, z + 0.5, result);
                }
            }
            return true;
        }));

        //See breaking event at PlayerEntityMixin/getBlockBreakingSpeed()
        PlayerBlockBreakEvents.AFTER.register(((world, player, pos, state, blockEntity) -> {
            if (EntityHelper.IS_SURVIVAL_AND_SERVER.test(player)) {
                double rand = world.getRandom().nextDouble();
                Block block = state.getBlock();
                Item mainHand = player.getMainHandStack().getItem();
                int x = pos.getX(), y = pos.getY(), z = pos.getZ();
                if (mainHand != Items.SHEARS) {
                    if (block == Blocks.GRASS) {
                        if (rand < 0.007) EntityHelper.dropItem(player, x, y, z, Reg.FEARLESSNESS_HERB, 1);
                        else if (rand < 0.012) EntityHelper.dropItem(player, x, y, z, Reg.WORM, 1);
                        else if (rand < 0.02) EntityHelper.dropItem(player, x, y, z, Reg.POTHERB, 1);
                        else if (rand < 0.35) EntityHelper.dropItem(player, x, y, z, Reg.ROCK, 1);
                        else if (rand < 0.55) EntityHelper.dropItem(player, x, y, z, Reg.GRASS_FIBER, 1);
                        else if (rand < 0.6) EntityHelper.dropItem(player, x, y, z, Items.STICK, 1);
                        else if (rand < 0.6009) EntityHelper.dropItem(player, x, y, z, Reg.SELAGINELLA, 1);
                        else if (rand < 0.6083) EntityHelper.dropItem(player, x, y, z, Reg.GINGER, 1);
                        else if (player.getMainHandStack().getItem() instanceof KnifeItem)
                            EntityHelper.dropItem(player, x, y, z, Reg.GRASS_FIBER, 1);
                    } else if (block == Blocks.TALL_GRASS) {
                        if (rand < 0.007) EntityHelper.dropItem(player, x, y, z, Reg.FEARLESSNESS_HERB, 2);
                        else if (rand < 0.012) EntityHelper.dropItem(player, x, y, z, Reg.WORM, 2);
                        else if (rand < 0.02) EntityHelper.dropItem(player, x, y, z, Reg.POTHERB, 2);
                        else if (rand < 0.35) EntityHelper.dropItem(player, x, y, z, Reg.ROCK, 2);
                        else if (rand < 0.55) EntityHelper.dropItem(player, x, y, z, Reg.GRASS_FIBER, 2);
                        else if (rand < 0.6) EntityHelper.dropItem(player, x, y, z, Items.STICK, 2);
                        else if (rand < 0.6009) EntityHelper.dropItem(player, x, y, z, Reg.SELAGINELLA, 2);
                        else if (rand < 0.6083) EntityHelper.dropItem(player, x, y, z, Reg.GINGER, 2);
                        else if (player.getMainHandStack().getItem() instanceof KnifeItem)
                            EntityHelper.dropItem(player, x, y, z, Reg.GRASS_FIBER, 2);
                    } else if (block == Blocks.VINE) EntityHelper.dropItem(player, x, y, z, Reg.FIBER_STRING, 1);
                }
                if (mainHand == Items.WOODEN_SHOVEL && block == Blocks.GRAVEL && rand < 0.02)
                    ItemScatterer.spawn(world, x + 0.5, y + 0.5, z + 0.5, Reg.RAW_COPPER_POWDER.getDefaultStack());
                // !mainHand.isEnchantable(new ItemStack(mainHand))
                if (!(mainHand instanceof ShovelItem)) {
                    if (block == Blocks.SNOW || block == Blocks.POWDER_SNOW)
                        EntityHelper.dropItem(player, x, y, z, Items.SNOWBALL, 1);
                    else if (block == Blocks.SNOW_BLOCK) EntityHelper.dropItem(player, x, y, z, Items.SNOWBALL, 4);
                }
                if (!(mainHand instanceof PickaxeItem)) {
                    if (block instanceof AbstractFurnaceBlock || block instanceof BrewingStandBlock || block instanceof AnvilBlock || (block instanceof CraftingTableBlock && state.isToolRequired()))
                        EntityHelper.dropItem(player, x, y, z, block.asItem(), 1);
                }
                if (!(mainHand instanceof AxeItem)) {
                    if (block == Reg.DRYING_RACK) EntityHelper.dropItem(player, x, y, z, Reg.DRYING_RACK_ITEM, 1);
                }
                if ((block == Blocks.CACTUS || block instanceof AbstractGlassBlock || block instanceof PaneBlock) && player.getMainHandStack().isEmpty()) {
                    player.damage(world.getDamageSources().cactus(), 2f);
                    ((StatAccessor) player).getInjuryManager().addBleeding(1.2);
                } else if (block == Blocks.SWEET_BERRY_BUSH) EntityHelper.dropItem(player, x, y, z, Reg.BERRY_BUSH, 1);
                else if (block == Blocks.CAMPFIRE) {
                    if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, player.getMainHandStack()) == 0
                            && state.contains(CombustionHelper.COMBUST_STAGE)) {
                        int stage = state.get(CombustionHelper.COMBUST_STAGE),
                                stickCount = (int) (stage * 0.7F),
                                ashCount = (int) ((15 - stage) * 0.7F);
                        if (stickCount > 0)
                            ItemScatterer.spawn(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(Items.STICK, stickCount));
                        if (ashCount > 0)
                            ItemScatterer.spawn(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(Reg.ASHES, ashCount));
                    }
                }
            }
        }));
    }
}