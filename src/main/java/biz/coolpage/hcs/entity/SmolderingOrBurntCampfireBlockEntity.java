package biz.coolpage.hcs.entity;

import biz.coolpage.hcs.Reg;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;

public class SmolderingOrBurntCampfireBlockEntity extends BlockEntity implements BlockEntityProvider {
    private final DefaultedList<ItemStack> itemsBeingCooked;
    private final int[] cookingTimes;

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private final int[] cookingTotalTimes;
    private final RecipeManager.MatchGetter<Inventory, CampfireCookingRecipe> matchGetter;

    public SmolderingOrBurntCampfireBlockEntity(BlockPos pos, BlockState state, DefaultedList<ItemStack> itemsBeingCooked/*TODO del this param?*/) {
        super(Reg.SMOLDERING_OR_BURNT_CAMPFIRE_BLOCK_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
        this.itemsBeingCooked = itemsBeingCooked;
        this.cookingTimes = new int[4];
        this.cookingTotalTimes = new int[4];
        this.matchGetter = RecipeManager.createCachedMatchGetter(RecipeType.CAMPFIRE_COOKING);
    }

    public SmolderingOrBurntCampfireBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, DefaultedList.ofSize(4));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SmolderingOrBurntCampfireBlockEntity(pos, state);
    }


//    private void updateListeners() {
//        this.markDirty();
//        Objects.requireNonNull(this.getWorld()).updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), 3);
//    }

    public static void litServerTick(@NotNull World world, BlockPos pos, BlockState state, SmolderingOrBurntCampfireBlockEntity campfire) {
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
        }
    }
}
