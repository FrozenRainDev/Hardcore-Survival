package biz.coolpage.hcs.config;

import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.util.WorldHelper;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

// Factory design patterns
public enum Configs {
    DISEASE("hcsDisease"),
    MOOD("hcsMood"),
    NUTRITION("hcsNutrition"),
    SANITY("hcsSanity"),
    STAMINA("hcsStamina"),
    THIRST("hcsThirst"),
    TEMPERATURE("hcsTemperature"),
    WET("hcsWetness"),
    SOUL_IMPAIR("hcsSoulImpairment"),
    DARK_THREAT("hcsDarknessThreatening"),
    DIG_CONSTRAIN("hcsDiggingConstraints"),
    HEAVY_LOAD("hcsHeavyLoading"),
    INJURY("hcsInjuryPunishment"),
    FOOD_SPOIL("hcsFoodSpoilage"),
    FOOD_POISON("hcsFoodPoisoning"),
    OXYGEN("hcsOxygenDeficiency"),
    SLOW_HEAL("hcsSlowHealing"),
    BURN("hcsBurningTimeLimits"),
    HOSTILE_ZOMBIE("hcsZombieExtraHostility"),
    HOSTILE_COW("hcsCowKicking");

    public final GameRules.Key<GameRules.BooleanRule> gameRule;

    Configs(String name) {
        this(name, GameRules.Category.PLAYER);
    }

    Configs(String name, GameRules.Category category) {
        this.gameRule = GameRules.register(name, category, GameRuleFactory.createBooleanRule(true));
    }

    public static boolean isEnabled(Configs name) {
        @Nullable ServerWorld world = WorldHelper.getServerWorld();
        if (world == null) {
            Reg.LOGGER.error("{}: StatConfig:WorldHelper.getServerWorld() null", name.name());
            return true;
        }
        return isEnabled(world, name);
    }

    public static boolean isEnabled(@Nullable ServerWorld world, Configs name) {
        if (!(world instanceof ServerWorld) || world.getGameRules() == null) {
            Reg.LOGGER.error("{}: StatConfig:ServerWorld Invalid world or gameRules", name.name());
            return true;
        }
        var gameRule = world.getGameRules().get(name.gameRule);
        if (gameRule == null) {
            Reg.LOGGER.error("{}: StatConfig: Invalid gameRule", name.gameRule.toString());
            return true;
        }
        return gameRule.get();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isEnabled(@Nullable PlayerEntity player, Configs name) {
        if (player == null) {
            Reg.LOGGER.warn("{}: StatConfig:ServerWorld Invalid player", name.name());
            return isEnabled(name);
        }
        return ((StatAccessor) player).getConfigManager().get(name);
    }
}
