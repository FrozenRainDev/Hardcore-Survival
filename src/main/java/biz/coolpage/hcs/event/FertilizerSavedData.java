package biz.coolpage.hcs.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class FertilizerSavedData extends SavedData {
    private static final String DATA_NAME = "hcs_fertilizer_data";
    // Stores the positions of fertilized plants
    private final Set<BlockPos> fertilizedPositions = new HashSet<>();

    public static FertilizerSavedData get(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalArgumentException("FertilizerSavedData can only be accessed on the server!");
        }
        return serverLevel.getDataStorage().computeIfAbsent(
                FertilizerSavedData::load,
                FertilizerSavedData::new,
                DATA_NAME
        );
    }

    public boolean isFertilized(BlockPos pos) {
        return fertilizedPositions.contains(pos);
    }

    public void addFertilized(BlockPos pos) {
        if (fertilizedPositions.add(pos)) {
            this.setDirty(); // Mark as dirty to ensure it saves to disk
        }
    }

    public void removeFertilized(BlockPos pos) {
        if (fertilizedPositions.remove(pos)) {
            this.setDirty();
        }
    }

    public static FertilizerSavedData load(@NotNull CompoundTag tag) {
        FertilizerSavedData data = new FertilizerSavedData();
        ListTag list = tag.getList("Positions", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            data.fertilizedPositions.add(NbtUtils.readBlockPos(list.getCompound(i)));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag list = new ListTag();
        for (BlockPos pos : fertilizedPositions) {
            list.add(NbtUtils.writeBlockPos(pos));
        }
        tag.put("Positions", list);
        return tag;
    }
}