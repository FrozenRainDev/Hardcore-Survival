package biz.coolpage.hcs.status;

import biz.coolpage.hcs.Hcs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HcsPersistentState extends SavedData {
    private boolean hasObtainedCopperPickaxe = false;
    private static final String OBTAINED_COPPER_PICK = "hcs_obtained_copper_pick";

    public boolean hasObtainedCopperPickaxe() {
        return hasObtainedCopperPickaxe;
    }

    public void setHasObtainedCopperPickaxe(boolean val) {
        hasObtainedCopperPickaxe = val;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
        nbt.putBoolean(OBTAINED_COPPER_PICK, hasObtainedCopperPickaxe);
        return nbt;
    }

    public static @NotNull HcsPersistentState load(@NotNull CompoundTag nbt) {
        HcsPersistentState state = new HcsPersistentState();
        state.setHasObtainedCopperPickaxe(nbt.contains(OBTAINED_COPPER_PICK) && nbt.getBoolean(OBTAINED_COPPER_PICK));
        state.setDirty();
        return state;
    }

    public static @Nullable HcsPersistentState getServerState(@Nullable ServerLevel world) {
//        ServerWorld world = server.getWorld(World.OVERWORLD);
        if (world == null || world.dimension() != Level.OVERWORLD) return null;
        DimensionDataStorage dimensionDataStorage = world.getDataStorage();
        HcsPersistentState state = dimensionDataStorage.computeIfAbsent(HcsPersistentState::load, HcsPersistentState::new, Hcs.MOD_ID);
        state.setDirty();
        return state;
    }
}