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
    HOSTILE_COW("hcsCowKicking"),
    NO_JUMP_PLACEMENT("hcsNoJumpPlacement"),
    SLOW_PLANT_GROWTH("hcsSlowPlantGrowth");

    // The field 'ruleName' must be declared here to resolve the symbol in HcsServerConfig
    public final String ruleName;
    public final GameRules.Key<GameRules.BooleanValue> gameRule;

    Configs(String name) {
        this(name, GameRules.Category.PLAYER);
    }

    Configs(String name, GameRules.Category category) {
        // Initialize the ruleName field
        this.ruleName = name;

        // Register way for Forge/Vanilla 1.20.1
        // Added listener to sync GameRule changes to Forge Server Config
        this.gameRule = GameRules.register(name, category, GameRules.BooleanValue.create(true, (server, value) -> {
            if (HcsConfigs.BOOLEAN_CONFIGS != null && HcsConfigs.BOOLEAN_CONFIGS.containsKey(this)) {
                HcsConfigs.BOOLEAN_CONFIGS.get(this).set(value.get());
            }
        }));
    }

    public static boolean isEnabled(Configs name) {
        @Nullable ServerLevel world = WorldHelper.getServerWorld();
        if (world == null) {
            Hcs.error("{}: StatConfig:WorldHelper.getServerWorld() null", name.name());
            // Fallback to reading from the Forge Server Config
            return HcsConfigs.BOOLEAN_CONFIGS.get(name).get();
        }
        return isEnabled(world, name);
    }

    public static boolean isEnabled(@Nullable ServerLevel world, Configs name) {
        if (world == null || world.getGameRules() == null) {
            Hcs.error("{}: StatConfig:ServerWorld Invalid world or gameRules", name.name());
            return HcsConfigs.BOOLEAN_CONFIGS.get(name).get();
        }

        var gameRuleValue = world.getGameRules().getRule(name.gameRule);
        if (gameRuleValue == null) {
            Hcs.error("{}: StatConfig: Invalid gameRule", name.gameRule.toString());
            return true;
        }

        // Act as the single source of truth from Forge Config
        return HcsConfigs.BOOLEAN_CONFIGS.get(name).get();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isEnabled(@Nullable Player player, Configs name) {
        if (player == null) {
            Hcs.error("{}: StatConfig:ServerWorld Invalid player", name.name());
            return isEnabled(name);
        }
        return ((StatAccessor) player).getConfigManager().get(name);
    }
}