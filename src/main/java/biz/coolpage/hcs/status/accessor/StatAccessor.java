package biz.coolpage.hcs.status.accessor;

import biz.coolpage.hcs.status.manager.*;

public interface StatAccessor {
    ThirstManager getThirstManager();

    StaminaManager getStaminaManager();

    TemperatureManager getTemperatureManager();

    StatusManager getStatusManager();

    SanityManager getSanityManager();

    NutritionManager getNutritionManager();

    WetnessManager getWetnessManager();

    InjuryManager getInjuryManager();

    MoodManager getMoodManager();

    DiseaseManager getDiseaseManager();

    ConfigManager getConfigManager();

    OxygenManager getOxygenManager();
}

/*
* # Forge 能力系统 (Capability System) 完整指南

能力系统是 Forge 提供的一个**数据附加机制**，允许你向原版实体、物品、方块等对象附加自定义数据和行为，而无需直接修改原版类。

---

## 一、核心概念

### 1. 什么是 Capability？

- **定义**: 一种接口，定义了你想要附加的数据和方法
- **用途**: 向玩家添加魔法值、向方块添加能量存储、向物品添加自定义数据等
- **优势**:
  - 兼容性好（不直接修改原版类）
  - 其他模组可以访问你的能力
  - 你也可以访问其他模组的能力

### 2. 三大核心组件

```
┌─────────────────┐
│  1. Interface   │ ← 定义功能接口
│  (ISanity)      │
└─────────────────┘
         ↓
┌─────────────────┐
│ 2. Provider     │ ← 提供能力实例
│ (附加到实体上)   │
└─────────────────┘
         ↓
┌─────────────────┐
│ 3. Registration │ ← 注册和事件处理
└─────────────────┘
```

---

## 二、完整实现示例：玩家理智系统

### 步骤 1: 创建能力接口

```java
// 文件: capability/ISanity.java
package com.yourmod.capability;

public interface ISanity {
    double get();
    void set(double value);
    void add(double amount);

    // 可选：保存和加载数据的方法
    void copyFrom(ISanity source);
}
```

### 步骤 2: 创建实现类

```java
// 文件: capability/Sanity.java
package com.yourmod.capability;

public class Sanity implements ISanity {
    private double sanity = 100.0; // 默认理智值
    private static final double MAX_SANITY = 100.0;
    private static final double MIN_SANITY = 0.0;

    @Override
    public double get() {
        return sanity;
    }

    @Override
    public void set(double value) {
        this.sanity = Math.max(MIN_SANITY, Math.min(MAX_SANITY, value));
    }

    @Override
    public void add(double amount) {
        set(get() + amount);
    }

    @Override
    public void copyFrom(ISanity source) {
        this.sanity = source.get();
    }
}
```

### 步骤 3: 创建 Provider（重要！）

```java
// 文件: capability/SanityProvider.java
package com.yourmod.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SanityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    // 1.20.1 推荐使用 CapabilityToken
    public static Capability<ISanity> SANITY = CapabilityManager.get(new CapabilityToken<>(){});

    private ISanity sanity = null;
    private final LazyOptional<ISanity> optional = LazyOptional.of(this::createSanity);

    private ISanity createSanity() {
        if (this.sanity == null) {
            this.sanity = new Sanity();
        }
        return this.sanity;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == SANITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createSanity();
        nbt.putDouble("sanity", sanity.get());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createSanity();
        sanity.set(nbt.getDouble("sanity"));
    }
}
```

### 步骤 4: 注册能力（在主类或专门的注册类）

```java
// 文件: HcsCapabilities.java
package com.yourmod;

import com.yourmod.capability.SanityProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hcs.MOD_ID)
public class HcsCapabilities {

    // 1. 注册能力类型
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ISanity.class);
    }

    // 2. 附加能力到玩家
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(SanityProvider.SANITY).isPresent()) {
                event.addCapability(
                    new ResourceLocation(Hcs.MOD_ID, "sanity"),
                    new SanityProvider()
                );
            }
        }
    }

    // 3. 玩家死亡/复活时复制数据
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(SanityProvider.SANITY).ifPresent(oldStore -> {
                event.getEntity().getCapability(SanityProvider.SANITY).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }
}
```

---

## 三、使用能力

### 1. 在药水效果中使用

```java
@Override
public void applyEffectTick(LivingEntity entity, int amplifier) {
    if (entity instanceof ServerPlayer player && !entity.isInvisible()) {
        entity.setSprinting(false);

        // 使用能力
        player.getCapability(SanityProvider.SANITY).ifPresent(sanity -> {
            sanity.add(-0.00001 * (amplifier + 1));
        });
    }
}
```

### 2. 在命令中使用

```java
public class SanityCommand {
    public static void setSanity(ServerPlayer player, double value) {
        player.getCapability(SanityProvider.SANITY).ifPresent(sanity -> {
            sanity.set(value);
            player.sendSystemMessage(Component.literal("理智值设置为: " + value));
        });
    }
}
```

### 3. 在事件中使用

```java
@SubscribeEvent
public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
        player.getCapability(SanityProvider.SANITY).ifPresent(sanity -> {
            // 每秒自然恢复 0.1 理智值
            if (player.tickCount % 20 == 0) {
                sanity.add(0.1);
            }
        });
    }
}
```

---

## 四、客户端同步（重要！）

能力数据默认只在服务端，需要手动同步到客户端。

### 方法 1: 使用自定义网络包

```java
// 发送包（服务端 -> 客户端）
public static void syncSanity(ServerPlayer player) {
    player.getCapability(SanityProvider.SANITY).ifPresent(sanity -> {
        // 发送自定义数据包
        PacketHandler.sendToPlayer(new SanitySyncPacket(sanity.get()), player);
    });
}
```

### 方法 2: 使用实体数据同步器（简单方法）

```java
// 在 PlayerTickEvent 中定期同步
@SubscribeEvent
public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.player instanceof ServerPlayer player && player.tickCount % 20 == 0) {
        player.getCapability(SanityProvider.SANITY).ifPresent(sanity -> {
            // 使用原版的数据同步机制
            player.entityData.set(SANITY_DATA, (float) sanity.get());
        });
    }
}
```

---

## 五、完整文件结构

```
src/main/java/com/yourmod/
├── Hcs.java                          (主类)
├── HcsCapabilities.java              (能力注册和事件)
├── capability/
│   ├── ISanity.java                  (接口)
│   ├── Sanity.java                   (实现)
│   └── SanityProvider.java           (提供者)
└── network/
    ├── PacketHandler.java            (网络处理)
    └── SanitySyncPacket.java         (同步包)
```

---

## 六、常见问题

### Q1: 能力数据不保存？
**A:** 确保实现了 `INBTSerializable<CompoundTag>` 接口。

### Q2: 客户端获取不到数据？
**A:** 需要手动同步数据到客户端（见第四节）。

### Q3: 玩家死亡后数据丢失？
**A:** 监听 `PlayerEvent.Clone` 事件并复制数据。

### Q4: 1.20.1 报错找不到 Capability？
**A:** 使用 `CapabilityToken` 而不是旧版的 `@CapabilityInject`。

---

如果你需要网络同步的完整代码，或者其他类型的能力示例（方块能力、物品能力），请告诉我！
* */