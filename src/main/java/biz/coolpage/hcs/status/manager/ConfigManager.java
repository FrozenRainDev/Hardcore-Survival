package biz.coolpage.hcs.status.manager;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.config.Configs;
import net.minecraft.server.level.ServerLevel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ConfigManager {
    private final HashMap<Enum<Configs>, Boolean> configStats = Arrays.stream(Configs.values())
            .collect(Collectors.toMap(
                    config -> config, // keys
                    config -> true,  // vals(default: true)
                    (existing, replacement) -> existing,  // distinct, no duplicate
                    HashMap::new
            ));

    private boolean contains(Enum<Configs> name) {
        if (configStats.containsKey(name)) return true;
        Hcs.error("Unknown stat config {}", name);
        configStats.put(name, true);
        return false;
    }

    public boolean get(Enum<Configs> name) {
        if (contains(name)) return configStats.get(name);
        return true;
    }

    @Deprecated
    public HashMap<Enum<Configs>, Boolean> getAll() {
        return new HashMap<>(configStats); // copy, not ref
    }

    public void set(Enum<Configs> name, boolean val) {
        contains(name);
        configStats.put(name, val);
    }

    public void update(Object worldObj) {
        if (worldObj instanceof ServerLevel world) {
            for (Configs name : Configs.values()) {
                // World game rule data -> distributed to cache in player info
                set(name, Configs.isEnabled(world, name));
            }
        }
    }
}