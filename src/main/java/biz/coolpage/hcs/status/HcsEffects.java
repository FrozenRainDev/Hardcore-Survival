package biz.coolpage.hcs.status;

import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.status.accessor.IDamageSources;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.InjuryManager;
import biz.coolpage.hcs.status.manager.StatusManager;
import biz.coolpage.hcs.status.manager.ThirstManager;
import biz.coolpage.hcs.util.EntityHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class HcsEffects {

    public static final RegistryObject<MobEffect> RETURN = Hcs.MOB_EFFECTS.register("return", () -> new HcsMobEffect(MobEffectCategory.NEUTRAL, 0x22d3f6) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void addAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
            if (entity instanceof ServerPlayer player && !entity.isSpectator())
                EntityHelper.msgById(player, "tip.hcsurvival.return_wait");
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && !player.isSpectator()) {
                StatusManager statusManager = ((StatAccessor) player).getStatusManager();
                if (statusManager.getReturnEffectAwaitTicks() > 100) {
                    statusManager.setReturnEffectAwaitTicks(0);
                    player.getFoodData().eat(-6, 0.0F);
                    ((StatAccessor) player).getSanityManager().add(-0.3);
                    ThirstManager thirstManager = ((StatAccessor) player).getThirstManager();
                    thirstManager.setSaturation(0.0F);
                    thirstManager.add(-0.3);
                    player.hurt(player.damageSources().fall(), 5.0f);
                    EntityHelper.teleportPlayerToSpawn(player.level(), player, false);
                }
            }
        }
    });

    public static final RegistryObject<MobEffect> THIRST = Hcs.MOB_EFFECTS.register("thirst", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xb0dff4) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer && !entity.isSpectator())
                ((StatAccessor) entity).getThirstManager().add(-0.00045 * (amplifier + 1));
        }
    });

    public static final RegistryObject<MobEffect> DIARRHEA = Hcs.MOB_EFFECTS.register("diarrhea", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xdbc44c) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && !entity.isSpectator()) {
                player.getFoodData().addExhaustion(0.01F * (amplifier + 1));
                ((StatAccessor) player).getThirstManager().add(-0.00015 * (amplifier + 1));
            }
        }
    });

    public static final RegistryObject<MobEffect> DEHYDRATED = Hcs.MOB_EFFECTS.register("dehydrated", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xe7e7e7) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && !entity.isInvisible()) {
                entity.setSprinting(false);
                ((StatAccessor) player).getSanityManager().add(-0.00001 * (amplifier + 1));
            }
        }
    }.addAttributeModifier(Attributes.MOVEMENT_SPEED, "DFAE009E-7F40-4EC2-BF1F-D6F0B5CA77B5", -0.15f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_DAMAGE, "DF97F5A2-0133-4F21-AED4-D3F51227624C", -0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, "726C2159-5D61-4656-8B1E-A594BC7C3E84", -0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "F03F53E5-DC9E-4716-8248-7B13FCAFE753", -0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> STARVING = Hcs.MOB_EFFECTS.register("starving", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0x646464) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer && !entity.isSpectator()) entity.setSprinting(false);
        }
    }.addAttributeModifier(Attributes.MOVEMENT_SPEED, "14A47B9E-D4A3-4964-BDA2-CAFB774D60D6", -0.15f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_DAMAGE, "008C8E27-DE78-4072-BF58-AC0B3CFBF2AF", -0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, "EEBB2A0F-C4E6-4E60-9BC3-B4D730C1F1F7", -0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "D9F3A91B-915C-4E69-A03E-558D0744C7AA", -0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> EXHAUSTED = Hcs.MOB_EFFECTS.register("exhausted", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xe3e3e3) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer && amplifier > 0) entity.setSprinting(false);
        }
    }.addAttributeModifier(Attributes.ATTACK_DAMAGE, "DC32D347-22EC-4B12-9E50-035302B760F0", -0.5F, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "3FE53989-7FA5-4D88-8060-D774E67796FE", -0.35F, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, "92C8EC57-582C-43C2-A8C7-F164774349D6", -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "6C27DCA8-9388-45EE-B6A8-33197B686DE4", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> HYPOTHERMIA = Hcs.MOB_EFFECTS.register("hypothermia", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0x0658ff) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && !entity.isSpectator() && IS_SURVIVAL_AND_SERVER.test(player)) {
                ((StatAccessor) player).getDiseaseManager().addCold(0.0005);
                if (amplifier > 0) {
                    ((StatAccessor) player).getSanityManager().add(-0.00001 * (amplifier + 1));
                    player.setSprinting(false);
                    player.setTicksFrozen(entity.getTicksRequiredToFreeze() + 3);
                }
            }
        }
    }.addAttributeModifier(Attributes.ATTACK_DAMAGE, "20F0693B-DF7C-4E9F-A970-82F12AE54B01", -0.1f, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "CF7F7560-AE19-4C37-BF17-DB898A9E62ED", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, "52520C27-F947-43A5-9E30-9FEDB3BB44DA", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "8E2A9034-7593-46BD-96B1-139203DEC1A6", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> HEATSTROKE = Hcs.MOB_EFFECTS.register("heatstroke", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xff6113) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && !entity.isInvulnerable()) {
                ((StatAccessor) player).getThirstManager().add(-0.0001 * (amplifier + 1));
                ((StatAccessor) player).getSanityManager().add(-0.00003 * (amplifier + 1));
                if (amplifier > 0) {
                    player.setSprinting(false);
                    //noinspection ConstantValue
                    if (player.level() != null && player.level().getGameTime() % 60 == 0) {
                        DamageSource damageSource = ((IDamageSources) player.level().damageSources()).heatstroke();
                        if (damageSource != null) player.hurt(damageSource, 1.0F);
                    }
                }
            }
        }
    }.addAttributeModifier(Attributes.ATTACK_DAMAGE, "6AEDC8BD-5071-4941-8396-E37CBDD6FF23", -0.1f, AttributeModifier.Operation.ADDITION)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "F98685EE-BA72-47C4-B0F3-B23835FD443D", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, "A8ED4453-B9F0-4BD5-A9E6-52F122FB07CD", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "3BBDACD0-A218-4872-BC9A-C17C90E6B57D", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> STRONG_SUN = Hcs.MOB_EFFECTS.register("strong_sun", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xff6c00) {
    });

    public static final RegistryObject<MobEffect> CHILLY_WIND = Hcs.MOB_EFFECTS.register("chilly_wind", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xf0f0f0) {
    });

    public static final RegistryObject<MobEffect> OVEREATEN = Hcs.MOB_EFFECTS.register("overeaten", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0x90514f) {
    }
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "28AFE91C-13C7-4E2F-BC29-7F747282B53C", -0.07F, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> INSANITY = Hcs.MOB_EFFECTS.register("insanity", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xff6113) {
    });

    public static final RegistryObject<MobEffect> MALNUTRITION = Hcs.MOB_EFFECTS.register("malnutrition", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xe8e5d2) {
    });

    public static final RegistryObject<MobEffect> WET = Hcs.MOB_EFFECTS.register("wet", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0x99a9d7) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && !player.isInWater() && amplifier > 0 && ((StatAccessor) player).getTemperatureManager().getEnvTempCache() < 0.6)
                ((StatAccessor) player).getDiseaseManager().addCold(0.00002 * (amplifier + 1) * net.minecraft.util.Mth.clamp(2 * (1 - ((StatAccessor) player).getTemperatureManager().getEnvTempCache()), 0.01, 2.0));
        }
    });

    public static final RegistryObject<MobEffect> CONSTANT_TEMPERATURE = Hcs.MOB_EFFECTS.register("constant_temperature", () -> new HcsMobEffect(MobEffectCategory.BENEFICIAL, 0x00aa00) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && !player.isSpectator())
                ((StatAccessor) player).getTemperatureManager().reset();
        }
    });

    public static final RegistryObject<MobEffect> SOUL_IMPAIRED = Hcs.MOB_EFFECTS.register("soul_impaired", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xd7e4eb) {
    }
            .addAttributeModifier(Attributes.MAX_HEALTH, "90BF8511-9818-41DC-BE0C-C7262EE79960", -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> INJURY = Hcs.MOB_EFFECTS.register("injury", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0x8c1000) {
        final Multimap<Attribute, AttributeModifier> customAttributeModifiers = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

        @Override
        public void addAttributeModifiers(@Nullable LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
            if (entity == null) return;
            switch (amplifier) {
                default -> {
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getDescriptionId, -0.05F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getDescriptionId, -0.05F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getDescriptionId, -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 1 -> {
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getDescriptionId, -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getDescriptionId, -0.15F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getDescriptionId, -0.2F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("B53DBD42-F249-4BFD-8A41-A9A8A2FB1C1C"), this::getDescriptionId, -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 2 -> {
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getDescriptionId, -0.3F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getDescriptionId, -0.25F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getDescriptionId, -0.3F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("B53DBD42-F249-4BFD-8A41-A9A8A2FB1C1C"), this::getDescriptionId, -0.25F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 3 -> {
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("5A0F06F9-3ECF-4CF4-8367-CF3F541B43E6"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("8F91E8F3-FB69-4105-A427-F1663C7A5B82"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("62BA69E1-BCE8-4E88-A939-AE5C1AF0814A"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("B53DBD42-F249-4BFD-8A41-A9A8A2FB1C1C"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, false, false));
                }
            }
            attributes.addTransientAttributeModifiers(this.customAttributeModifiers);
            super.addAttributeModifiers(entity, attributes, amplifier);
        }

        @Override
        public void removeAttributeModifiers(@Nullable LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
            if (entity == null) return;
            removeTempAttributes(attributes, this.customAttributeModifiers);
            super.removeAttributeModifiers(entity, attributes, amplifier);
        }
    });

    public static final RegistryObject<MobEffect> PAIN = Hcs.MOB_EFFECTS.register("pain", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0x421d0a) {
        final Multimap<Attribute, AttributeModifier> customAttributeModifiers = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

        @Override
        public void addAttributeModifiers(@Nullable LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
            if (entity == null) return;
            switch (amplifier) {
                default -> {
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("873BA6F8-398D-432C-B8EE-2601D0363F8E"), this::getDescriptionId, -0.05F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("FD1185A0-A575-4096-8E17-97A03E2EB922"), this::getDescriptionId, -0.05F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("792D1884-1418-4A06-A03E-756A7B609CD0"), this::getDescriptionId, -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 1 -> {
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("881B4777-7AC6-43F0-9785-FA6C467C0133"), this::getDescriptionId, -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("B1B204EE-92C8-4934-A008-F82077D6CD1B"), this::getDescriptionId, -0.15F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("A72773FC-98E9-4206-B26D-FC6FAEAEB2E4"), this::getDescriptionId, -0.2F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("283A6E73-E2E2-4D42-A262-6CCF459704A2"), this::getDescriptionId, -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 2 -> {
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("292CBB4C-9599-4538-97CF-DC6F924E1BB2"), this::getDescriptionId, -0.25F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("32F29F5C-CD1E-47E2-B42C-D34F3B9FDDC3"), this::getDescriptionId, -0.25F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("DF28B6B7-7175-4D42-856D-D52EF647A056"), this::getDescriptionId, -0.3F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("BC402FEC-1445-4C12-8D10-62D7182B8781"), this::getDescriptionId, -0.25F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                case 3 -> {
                    this.customAttributeModifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(UUID.fromString("8FB78320-AB19-4528-9D46-E7C1DBA7430D"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("5070DD4F-BBA9-4400-B2DF-E0260BA77A98"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(UUID.fromString("A5448EE0-84AB-4BE4-A745-49B8EE8CC6FE"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                    this.customAttributeModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("4DD760AE-07D8-412A-852E-6990AD106D94"), this::getDescriptionId, -0.4F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            }
            attributes.addTransientAttributeModifiers(this.customAttributeModifiers);
            super.addAttributeModifiers(entity, attributes, amplifier);
        }

        @Override
        public void removeAttributeModifiers(@Nullable LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
            if (entity == null) return;
            removeTempAttributes(attributes, this.customAttributeModifiers);
            super.removeAttributeModifiers(entity, attributes, amplifier);
        }
    });

    public static final RegistryObject<MobEffect> PANIC = Hcs.MOB_EFFECTS.register("panic", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xffffff) {
    });

    public static final RegistryObject<MobEffect> BLEEDING = Hcs.MOB_EFFECTS.register("bleeding", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xcf0303) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@Nullable LivingEntity entity, int amplifier) {
            //noinspection ConstantValue
            if (entity != null && entity.level() != null && !entity.isInvulnerable() && amplifier > 0)
                if (entity.level().getGameTime() % (switch (amplifier) {
                    case 1 -> 300L;
                    case 2 -> 75L;
                    default -> 20L;
                } * HcsDifficulty.chooseVal(toPlayer(entity), 2.0F, 1.0F, 0.5F)) == 0)
                    entity.hurt(((IDamageSources) entity.level().damageSources()).bleeding(), 0.5F);
        }
    });

    public static final RegistryObject<MobEffect> DARKNESS_ENVELOPED = Hcs.MOB_EFFECTS.register("darkness_enveloped", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0x000000) {
    }
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "75AD9D60-968B-4788-8B9F-3A545D3534E7", -0.6F, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> FRACTURE = Hcs.MOB_EFFECTS.register("fracture", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xe8e5d2) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && IS_SURVIVAL_LIKE.test(player) && amplifier > 0) {
                entity.setSprinting(false);
                InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
                if (injuryManager.getRawPain() < 3) injuryManager.setRawPain(3);
            }
        }
    }.addAttributeModifier(Attributes.MOVEMENT_SPEED, "FFEFDCF8-49B1-4CC7-B6D7-4E07D7F936CA", -0.7F, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> PARASITE_INFECTION = Hcs.MOB_EFFECTS.register("parasite_infection", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xe2bc8a) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && IS_SURVIVAL_LIKE.test(player)) {
                ((StatAccessor) player).getThirstManager().add(-0.0001 * (amplifier + 1));
                player.getFoodData().addExhaustion(0.007F * (amplifier + 1));
                if (amplifier > 0) {
                    InjuryManager injuryManager = ((StatAccessor) player).getInjuryManager();
                    if (injuryManager.getRawPain() < amplifier) injuryManager.setRawPain(amplifier);
                    if (amplifier > 1) {
                        ((StatAccessor) player).getSanityManager().add(-0.00005);
                        if (player.level().getGameTime() % 100 == 0) {
                            player.hurt(((IDamageSources) player.level().damageSources()).parasiteInfection(), 1.0F);
                            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, false, false));
                        }
                    }
                }
            }
        }
    });

    public static final RegistryObject<MobEffect> UNHAPPY = Hcs.MOB_EFFECTS.register("unhappy", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0x71c5db) {
    });

    public static final RegistryObject<MobEffect> COLD = Hcs.MOB_EFFECTS.register("cold", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xf0c1ba) {
    }
            .addAttributeModifier(Attributes.ATTACK_DAMAGE, "008C8E27-DE78-4072-BF58-AC0B3CFBF2AF", -0.07F, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_SPEED, "EEBB2A0F-C4E6-4E60-9BC3-B4D730C1F1F7", -0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.ATTACK_KNOCKBACK, "D9F3A91B-915C-4E69-A03E-558D0744C7AA", -0.2F, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> HEAVY_LOAD = Hcs.MOB_EFFECTS.register("heavy_load", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xfed93f) {
    });

    public static final RegistryObject<MobEffect> PAIN_KILLING = Hcs.MOB_EFFECTS.register("pain_killing", () -> new HcsMobEffect(MobEffectCategory.BENEFICIAL, 0x858585) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player) ((StatAccessor) player).getInjuryManager().setRawPain(0.0);
        }
    });

    public static final RegistryObject<MobEffect> IRONSKIN = Hcs.MOB_EFFECTS.register("ironskin", () -> new HcsMobEffect(MobEffectCategory.BENEFICIAL, 0xe6de0a) {
    });

    public static final RegistryObject<MobEffect> FOOD_POISONING = Hcs.MOB_EFFECTS.register("food_poisoning", () -> new HcsMobEffect(MobEffectCategory.HARMFUL, 0xb3c17b) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player && IS_SURVIVAL_LIKE.test(player)) {
                ((StatAccessor) player).getThirstManager().add(-0.0003 * (amplifier + 1));
                player.getFoodData().addExhaustion(0.025F * (amplifier + 1));
                ((StatAccessor) player).getSanityManager().add(-0.00002 * (amplifier + 1));
                player.getFoodData().setSaturation(0.0F);
            }
        }
    });

    public static final RegistryObject<MobEffect> FEARLESSNESS = Hcs.MOB_EFFECTS.register("fearlessness", () -> new HcsMobEffect(MobEffectCategory.BENEFICIAL, 0x7e7e7e) {
        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return true;
        }

        @Override
        public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player) ((StatAccessor) player).getMoodManager().setPanic(0.0);
        }
    });

    // Fix variables set for compatibility with RegistryObject
    private static final HashSet<RegistryObject<MobEffect>> VARIABLE_EFFECTS = Util.make(new HashSet<>(), set -> {
        set.add(PAIN);
        set.add(INJURY);
        set.add(PANIC);
        set.add(BLEEDING);
        set.add(WET);
    });

    // Now predicate needs to check the value of get()
    public static final Predicate<MobEffect> IS_EFFECT_NAME_VARIABLE = effect ->
            VARIABLE_EFFECTS.stream().anyMatch(reg -> reg.get() == effect);

    public static String getEffectVarName(String key, int amplifier) {
        return switch (amplifier) {
            case 0, 1, 2, 3 -> key + "." + (amplifier + 1);
            default -> key;
        };
    }

    private static @NotNull String getEffectBaseKey(@NotNull MobEffect effect) {
        ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        if (id != null && Hcs.MOD_ID.equals(id.getNamespace()))
            return "effect." + Hcs.MOD_ID + "." + id.getPath();
        return effect.getDescriptionId();
    }

    private static @Nullable MutableComponent getEffectDescriptionText(@NotNull String nameKey, @NotNull String baseKey) {
        Language language = Language.getInstance();
        String descriptionKey = nameKey + ".description";

        // Try level-specific description first, then fallback to the base description.
        if (!language.has(descriptionKey)) {
            String baseDescriptionKey = baseKey + ".description";
            if (!descriptionKey.equals(baseDescriptionKey) && language.has(baseDescriptionKey))
                descriptionKey = baseDescriptionKey;
        }

        if (!language.has(descriptionKey)) return null;

        // Read raw translated text directly to avoid false fallback caused by translatable parsing.
        return Component.literal(language.getOrDefault(descriptionKey).replace("%%", "%"));
    }

    private static void removeTempAttributes(AttributeMap attributes, @NotNull Multimap<Attribute, AttributeModifier> customAttributeModifiers) {
        for (Map.Entry<Attribute, AttributeModifier> entry : customAttributeModifiers.entries()) {
            AttributeInstance entityAttributeInstance = attributes.getInstance(entry.getKey());
            if (entityAttributeInstance == null) continue;
            AttributeModifier entityAttributeModifier = entry.getValue();
            entityAttributeInstance.removeModifier(entityAttributeModifier);
        }
        customAttributeModifiers.clear();
    }

    private static final Predicate<ServerPlayer> IS_SURVIVAL_AND_SERVER = player -> !player.isSpectator() && !player.isCreative();
    private static final Predicate<ServerPlayer> IS_SURVIVAL_LIKE = player -> !player.isSpectator() && !player.isCreative();

    private static ServerPlayer toPlayer(LivingEntity entity) {
        return entity instanceof ServerPlayer ? (ServerPlayer) entity : null;
    }

    public static void init() {
        // In Java, a class's static fields (such as the various RegistryObject<MobEffect> you define) are initialized only
        // when the class is first actively used (for example, by calling its static methods or accessing its static fields).
    }

    // --- Custom Base Effect Class for HCS to handle client rendering ---
    public static class HcsMobEffect extends MobEffect {
        protected HcsMobEffect(MobEffectCategory category, int color) {
            super(category, color);
        }

        @Override
        public void initializeClient(@NotNull Consumer<IClientMobEffectExtensions> consumer) {
            consumer.accept(new IClientMobEffectExtensions() {

                // 1. Override HUD icon rendering to prevent the flashing animation.
                @Override
                public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphics guiGraphics, int x, int y, float z, float alpha) {
                    net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(instance.getEffect());

                    // Force alpha to 1.0F to completely bypass the vanilla flashing animation (which happens < 200 ticks)
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    guiGraphics.blit(x + 3, y + 3, 0, 18, 18, sprite);
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F); // Reset color to prevent affecting other GUI elements

                    // Return true to indicate we have handled the icon drawing, skipping vanilla's flashing logic
                    return true;
                }

                // 2. Override inventory text rendering to show description instead of duration.
                @Override
                public boolean renderInventoryText(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, GuiGraphics guiGraphics, int x, int y, int blitOffset) {
                    MobEffect effect = instance.getEffect();
                    String baseKey = getEffectBaseKey(effect);

                    String nameKey = baseKey;
                    if (IS_EFFECT_NAME_VARIABLE.test(effect)) {
                        nameKey = getEffectVarName(baseKey, instance.getAmplifier());
                    }

                    MutableComponent nameText = Component.translatable(nameKey);
                    if (!IS_EFFECT_NAME_VARIABLE.test(effect) && instance.getAmplifier() > 0 && instance.getAmplifier() <= 9) {
                        nameText.append(Component.literal(" ")).append(Component.translatable("enchantment.level." + (instance.getAmplifier() + 1)));
                    }
                    guiGraphics.drawString(Minecraft.getInstance().font, nameText, x + 28, y + 6, 0xFFFFFF);

                    MutableComponent description = getEffectDescriptionText(nameKey, baseKey);

                    if (description != null) {
                        var font = Minecraft.getInstance().font;
                        int maxWidth = 88;
                        int textWidth = font.width(description);

                        if (textWidth > maxWidth) {
                            // Enable scissor to limit the visible area for marquee text.
                            guiGraphics.enableScissor(x + 28, y + 16, x + 28 + maxWidth, y + 16 + 10);

                            long time = Util.getMillis();
                            int pauseDuration = 1500;
                            int speed = 30;

                            int maxScroll = textWidth - maxWidth;
                            int cycleTime = pauseDuration * 2 + maxScroll * speed;
                            long currentCycle = time % cycleTime;

                            int offset = 0;
                            if (currentCycle > pauseDuration) {
                                if (currentCycle < pauseDuration + maxScroll * speed) {
                                    offset = (int) ((currentCycle - pauseDuration) / speed);
                                } else {
                                    offset = maxScroll;
                                }
                            }

                            guiGraphics.drawString(font, description, x + 28 - offset, y + 16, 8355711, false);

                            // Disable scissor immediately to avoid affecting subsequent rendering.
                            guiGraphics.disableScissor();
                        } else {
                            guiGraphics.drawString(font, description, x + 28, y + 16, 8355711, false);
                        }
                    } else {
                        guiGraphics.drawString(Minecraft.getInstance().font, getDurationText(instance), x + 28, y + 16, 8355711);
                    }

                    // Return true to fully replace vanilla duration text rendering.
                    return true;
                }

                private static @NotNull String getDurationText(@NotNull MobEffectInstance instance) {
                    int ticks = instance.getDuration();
                    String durationText;
                    if (instance.isInfiniteDuration()) {
                        durationText = "**:**";
                    } else {
                        int seconds = ticks / 20;
                        int minutes = seconds / 60;
                        seconds %= 60;
                        durationText = String.format("%02d:%02d", minutes, seconds);
                    }
                    return durationText;
                }
            });
        }
    }
}