package biz.coolpage.hcs.entity;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.status.accessor.ICampfireBlockEntity;
import biz.coolpage.hcs.util.CombustionHelper;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("CommentedOutCode")
public class SmolderingOrBurntCampfireBlockEntity extends BlockEntity implements BlockEntityProvider, ICampfireBlockEntity {
    public SmolderingOrBurntCampfireBlockEntity(BlockPos pos, BlockState state) {
        super(Reg.SMOLDERING_OR_BURNT_CAMPFIRE_BLOCK_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
    }

    private static final long CAMPFIRE_MAX_BURNING_LENGTH = CombustionHelper.MAX_CAMPFIRE_BURNING_LENGTH * 2L;
    private long extinguishTime = Long.MAX_VALUE;

    @Unique
    public long getBurnOutTime() {
        return this.extinguishTime;
    }

    @Unique
    public void resetBurnOutTime() {
        if (this.world != null) {
            CampfireBlockEntity.markDirty(this.world, this.pos, this.world.getBlockState(pos));
            this.extinguishTime = this.world.getTime() + CAMPFIRE_MAX_BURNING_LENGTH; // note: *2 here
        }
    }

    @Override
    public boolean setBurnOutTime(long val) {
        Reg.LOGGER.error("SmolderingOrBurntCampfireBlockEntity::setBurnOutTime should not be called; find why called and call CampfireBlockEntity::setBurnOutTime instead");
        return false;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SmolderingOrBurntCampfireBlockEntity(pos, state);
    }

    @SuppressWarnings({"unused", "GrazieInspection"})
    public static void litServerTick(@NotNull World world, BlockPos pos, @NotNull BlockState state, SmolderingOrBurntCampfireBlockEntity campfire) {
        if (state.isOf(Reg.BURNT_CAMPFIRE_BLOCK)) return;
        CombustionHelper.onServerTick(world, pos, state, campfire);
        /*
        if (world.getTime() % 2 == 0) return; // Halve the speed of charcoal grilling
        boolean flag = false;
        for (int i = 0; i < campfire.itemsBeingCooked.size(); ++i) {
            SimpleInventory inventory;
            ItemStack itemStack2;
            ItemStack itemStack = campfire.itemsBeingCooked.get(i);
            if (itemStack.isEmpty()) continue;
            flag = true;
            campfire.cookingTimes[i]++;
            if (campfire.cookingTimes[i] < campfire.cookingTotalTimes[i] || !(itemStack2 = campfire.matchGetter.getFirstMatch(inventory = new SimpleInventory(itemStack), world).map(recipe -> recipe.craft(inventory, world.getRegistryManager())).orElse(itemStack)).isItemEnabled(world.getEnabledFeatures()))
                continue;
            ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), itemStack2);
            campfire.itemsBeingCooked.set(i, ItemStack.EMPTY);
            world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
        }
        if (flag) {
            SmolderingOrBurntCampfireBlockEntity.markDirty(world, pos, state);
        }*/
    }


    // These var still not being used
    /*
    private final DefaultedList<ItemStack> itemsBeingCooked;
    private final int[] cookingTimes;

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private final int[] cookingTotalTimes;
    private final RecipeManager.MatchGetter<Inventory, CampfireCookingRecipe> matchGetter;

    public SmolderingOrBurntCampfireBlockEntity(BlockPos pos, BlockState state, DefaultedList<ItemStack> itemsBeingCooked) {
        super(Reg.SMOLDERING_OR_BURNT_CAMPFIRE_BLOCK_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
        this.itemsBeingCooked = itemsBeingCooked;
        this.cookingTimes = new int[4];
        this.cookingTotalTimes = new int[4];
        this.matchGetter = RecipeManager.createCachedMatchGetter(RecipeType.CAMPFIRE_COOKING);
    }

    public SmolderingOrBurntCampfireBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, DefaultedList.ofSize(4));
    }
    */
}
