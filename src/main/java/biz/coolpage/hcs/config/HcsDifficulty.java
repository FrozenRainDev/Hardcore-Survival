package biz.coolpage.hcs.config;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static biz.coolpage.hcs.Reg.HCS_DIFFICULTY;

public class HcsDifficulty {
    public enum HcsDifficultyEnum {
        relaxing, standard, challenging
    }

    public static Enum<HcsDifficultyEnum> getDifficulty(@Nullable World world) {
        if (!(world instanceof ServerWorld) || world.getGameRules() == null) {
            Reg.LOGGER.warn(HcsDifficulty.class.getSimpleName() + ": Invalid world or game rules");
            return HcsDifficultyEnum.standard;
        }
        return world.getGameRules().get(HCS_DIFFICULTY).get();
    }

    public static Enum<HcsDifficultyEnum> getDifficulty(@Nullable PlayerEntity player) {
        if (player == null) return HcsDifficultyEnum.standard;
        return ((StatAccessor) player).getStatusManager().getHcsDifficulty();
    }

    public static boolean isOf(World world, Enum<HcsDifficultyEnum> difficulty) {
        Enum<HcsDifficultyEnum> worldDiff = getDifficulty(world);
        if (worldDiff == null) return false;
        return worldDiff.ordinal() == difficulty.ordinal();
    }

    public static boolean isOf(PlayerEntity player, Enum<HcsDifficultyEnum> difficulty) {
        if (player == null || difficulty == null) return false;
        return ((StatAccessor) player).getStatusManager().getHcsDifficulty().ordinal() == difficulty.ordinal();
    }

    public static <T> T chooseVal(@Nullable PlayerEntity player, T relax, T standard, T challenge) {
        if (player != null) {
            if (player.world instanceof ServerWorld serverWorld)
                return chooseVal(getDifficulty(serverWorld), relax, standard, challenge);
            return chooseVal(getDifficulty(player), relax, standard, challenge);
        }
        return standard;
    }

    public static <T> T chooseVal(@Nullable World world, T relax, T standard, T challenge) {
        if (world instanceof ServerWorld serverWorld)
            return chooseVal(getDifficulty(serverWorld), relax, standard, challenge);
        return standard;
    }

    private static <T> T chooseVal(@Nullable Enum<HcsDifficultyEnum> difficulty, T relax, T standard, T challenge) {
        if (difficulty != null) {
            if (difficulty.ordinal() == HcsDifficulty.HcsDifficultyEnum.relaxing.ordinal()) return relax;
            if (difficulty.ordinal() == HcsDifficulty.HcsDifficultyEnum.challenging.ordinal()) return challenge;
        }
        return standard;
    }
}
