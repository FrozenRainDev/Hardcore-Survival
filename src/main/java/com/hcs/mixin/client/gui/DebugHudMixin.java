package com.hcs.mixin.client.gui;

import com.hcs.status.accessor.StatAccessor;
import com.hcs.status.manager.*;
import com.hcs.util.TemperatureHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    @Final
    @Shadow
    private MinecraftClient client;

    @Inject(method = "getLeftText", at = @At("RETURN"))
    public void getLeftText(@NotNull CallbackInfoReturnable<List<String>> cir) {
        List<String> list = cir.getReturnValue();
        if (!this.client.options.debugProfilerEnabled || this.client.player == null || list == null) return;
        PlayerEntity player = this.client.player;
        HungerManager hungerManager = player.getHungerManager();
        ThirstManager thirstManager = ((StatAccessor) player).getThirstManager();
        StaminaManager staminaManager = ((StatAccessor) player).getStaminaManager();
        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
        SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
        NutritionManager nutritionManager = ((StatAccessor) player).getNutritionManager();
        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
        World world = player.world;
        BlockPos pos = player.getBlockPos();
        RegistryEntry<Biome> biomeEntry = world.getBiome(pos);
        Biome biome = biomeEntry.value();
        String biomeName = TemperatureHelper.getBiomeName(biomeEntry);
        list.add("[ HCS Debug ]");
        list.add("MainHandStackNbt: " + player.getMainHandStack().getOrCreateNbt().toString());
        list.add("Time: tick=" + world.getTime() + ", lunar=of_day=" + world.getLunarTime());
        list.add("Thirst: value=" + thirstManager.get() + ", saturation=" + thirstManager.getSaturation() + ", rate=" + thirstManager.getThirstRateAffectedByTemp());
        list.add("Hunger: level=" + hungerManager.getFoodLevel() + ", saturation=" + hungerManager.getSaturationLevel() + ", exhaustion=" + ((StatAccessor) player).getStatusManager().getExhaustion());
        list.add("Stamina: " + staminaManager.get());
        list.add("Sanity: " + sanityManager.get() + ", difference=" + sanityManager.getDifference());
        list.add("Temperature: biome=" + biome.getTemperature() + ", env=[real: " + String.format("%.5f", TemperatureHelper.getTemp(player)) + " ,feel:" + String.format("%.5f", TemperatureHelper.getFeelingTemp(player, TemperatureHelper.getTemp(player), biomeName, player.world.getLightLevel(LightType.SKY, player.getBlockPos()))) + "]" + ", value=" + String.format("%.5f", temperatureManager.get()) + ", satu=" + String.format("%.5f", temperatureManager.getSaturation()) + ", trend=" + temperatureManager.getTrendType());
        list.add("Nutrition: vegetable=" + nutritionManager.getVegetable());
        list.add("Oxygen: lack=" + statusManager.getOxygenLackLevel() + ", gen=" + statusManager.getOxygenGenLevel());
        list.add("Wetness: "+((StatAccessor)player).getWetnessManager().get());
    }

}
