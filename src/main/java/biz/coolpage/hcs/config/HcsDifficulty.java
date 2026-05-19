package biz.coolpage.hcs.config;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import static biz.coolpage.hcs.Hcs.HCS_DIFFICULTY;

public class HcsDifficulty {
    // CreativeModeTab.builder().title(Component.translatable("itemGroup.hcsurvival.main")).icon(() -> new ItemStack(FLINT_HATCHET)).build();
    public static final String HCS_DIFFICULTY_NAME = "hcsDifficulty";

    public enum HcsDifficultyEnum {
        relaxing, standard, challenging
    }

    public static Enum<HcsDifficultyEnum> getDifficulty(@Nullable Level level) {
        if (!(level instanceof ServerLevel) || level.getGameRules() == null) {
            Hcs.warn("{}: Invalid level or game rules", HcsDifficulty.class.getSimpleName());
            return HcsDifficultyEnum.standard;
        }
        int id = level.getGameRules().getRule(HCS_DIFFICULTY).get();
        HcsDifficultyEnum[] vals = HcsDifficultyEnum.values();
        if (id < 0) return HcsDifficultyEnum.relaxing;
        if (id >= vals.length) return HcsDifficultyEnum.standard;
        return HcsDifficultyEnum.values()[id];
    }

    public static Enum<HcsDifficultyEnum> getDifficulty(@Nullable Player player) {
        if (player == null) return HcsDifficultyEnum.standard;
        return ((StatAccessor) player).getStatusManager().getHcsDifficulty();
    }

    public static boolean isOf(Level level, Enum<HcsDifficultyEnum> difficulty) {
        Enum<HcsDifficultyEnum> levelDiff = getDifficulty(level);
        if (levelDiff == null) return false;
        return levelDiff == difficulty;
    }

    public static boolean isOf(Player player, Enum<HcsDifficultyEnum> difficulty) {
        if (player == null || difficulty == null) return false;
        return ((StatAccessor) player).getStatusManager().getHcsDifficulty() == difficulty;
    }

    public static <T> T chooseVal(@Nullable Player player, T relax, T standard, T challenge) {
        if (player != null) {
            if (player.level() instanceof ServerLevel serverLevel)
                return chooseVal(getDifficulty(serverLevel), relax, standard, challenge);
            return chooseVal(getDifficulty(player), relax, standard, challenge);
        }
        return standard;
    }

    public static <T> T chooseVal(@Nullable Level level, T relax, T standard, T challenge) {
        if (level instanceof ServerLevel serverLevel)
            return chooseVal(getDifficulty(serverLevel), relax, standard, challenge);
        return standard;
    }

    private static <T> T chooseVal(@Nullable Enum<HcsDifficultyEnum> difficulty, T relax, T standard, T challenge) {
        if (difficulty != null) {
            if (difficulty == HcsDifficultyEnum.relaxing) return relax;
            if (difficulty == HcsDifficultyEnum.challenging) return challenge;
        }
        return standard;
    }
}