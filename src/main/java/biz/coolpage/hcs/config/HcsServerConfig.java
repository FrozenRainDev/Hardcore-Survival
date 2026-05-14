package biz.coolpage.hcs.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.EnumMap;
import java.util.Map;

// Import MOD_ID from Hcs main class
import static biz.coolpage.hcs.Hcs.MOD_ID;

public class HcsServerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Map to store the ForgeConfigSpec for each enum dynamically
    public static final Map<Configs, ForgeConfigSpec.BooleanValue> BOOLEAN_CONFIGS = new EnumMap<>(Configs.class);

    static {
        BUILDER.push("hcs_settings");

        // Dynamically register boolean configs from the Configs enum
        for (Configs config : Configs.values()) {
            BOOLEAN_CONFIGS.put(config, BUILDER
                    .comment("Enable or disable " + config.name())
                    // Here we use config.ruleName which must be defined in the Configs enum
                    .define(config.ruleName, true));
        }

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    // Suppress the deprecation warning as this is the standard method for 1.20.1
    public static void register() {
        // Register the server config using the MOD_ID
        //noinspection removal
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC, MOD_ID + "-server.toml");
    }
}