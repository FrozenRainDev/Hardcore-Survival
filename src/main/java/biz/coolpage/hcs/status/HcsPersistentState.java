package biz.coolpage.hcs.status;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HcsPersistentState extends PersistentState {
    private boolean hasObtainedCopperPickaxe = false;
    private static final String OBTAINED_COPPER_PICK = "hcs_obtained_copper_pick";

    public boolean hasObtainedCopperPickaxe() {
        return hasObtainedCopperPickaxe;
    }

    public void setHasObtainedCopperPickaxe(boolean val) {
        hasObtainedCopperPickaxe = val;
    }

    @Override
    public NbtCompound writeNbt(@NotNull NbtCompound nbt) {
        nbt.putBoolean(OBTAINED_COPPER_PICK, hasObtainedCopperPickaxe);
        return nbt;
    }

    public static @NotNull HcsPersistentState createFromNbt(@NotNull NbtCompound nbt) {
        HcsPersistentState state = new HcsPersistentState();
        state.setHasObtainedCopperPickaxe(nbt.contains(OBTAINED_COPPER_PICK) && nbt.getBoolean(OBTAINED_COPPER_PICK));
        state.markDirty();
        return state;
    }

    public static @Nullable HcsPersistentState getServerState(@Nullable ServerWorld world) {
//        ServerWorld world = server.getWorld(World.OVERWORLD);
        if (world == null || world.getRegistryKey() != World.OVERWORLD) return null;
        PersistentStateManager persistentStateManager = world.getPersistentStateManager();
        HcsPersistentState state = persistentStateManager.getOrCreate(HcsPersistentState::createFromNbt, HcsPersistentState::new, "hcs");
        state.markDirty();
        return state;
    }
}
