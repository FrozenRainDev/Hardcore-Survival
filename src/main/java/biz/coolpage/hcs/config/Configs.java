package biz.coolpage.hcs.config;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.util.WorldHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
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

    public final GameRules.Key<GameRules.BooleanValue> gameRule;

    Configs(String name) {
        this(name, GameRules.Category.PLAYER);
    }

    Configs(String name, GameRules.Category category) {
        // Forge/Vanilla 1.20.1 注册方式
        this.gameRule = GameRules.register(name, category, GameRules.BooleanValue.create(true));
    }

    public static boolean isEnabled(Configs name) {
        @Nullable ServerLevel world = WorldHelper.getServerWorld();
        if (world == null) {
            Hcs.error("{}: StatConfig:WorldHelper.getServerWorld() null", name.name());
            return true;
        }
        return isEnabled(world, name);
    }

    public static boolean isEnabled(@Nullable ServerLevel world, Configs name) {
        if (world == null || world.getGameRules() == null) {
            Hcs.error("{}: StatConfig:ServerWorld Invalid world or gameRules", name.name());
            return true;
        }
        var gameRuleValue = world.getGameRules().getRule(name.gameRule);
        if (gameRuleValue == null) {
            Hcs.error("{}: StatConfig: Invalid gameRule", name.gameRule.toString());
            return true;
        }
        return gameRuleValue.get();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isEnabled(@Nullable Player player, Configs name) {
        if (player == null) {
            // 注意：原代码此处使用了 warn，若需严格遵守“改为Hcs.error”指令，可统一使用 error
            Hcs.error("{}: StatConfig:ServerWorld Invalid player", name.name());
            return isEnabled(name);
        }
        return ((StatAccessor) player).getConfigManager().get(name);
    }
}