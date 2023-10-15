package biz.coolpage.hcs.util;

import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.manager.StatusManager;
import biz.coolpage.hcs.Reg;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

/*
  Get customized temperature value
  < 0.0 too cold to bear
  0.5 optimal
  > 1.0 too hot
*/

public abstract class TemperatureHelper implements WorldView {
    public static final int[][] POS_IN_NEARBY_CHUNKS = {{-16, 0}, {16, 0}, {0, -16}, {0, 16}};
    public static final int[][][] BALL_RAD3 = {{{-3, 0, 0}, {-2, -2, -1}, {-2, -2, 0}, {-2, -2, 1}, {-2, -1, -2}, {-2, -1, -1}, {-2, -1, 0}, {-2, -1, 1}, {-2, -1, 2}, {-2, 0, -2}, {-2, 0, -1}, {-2, 0, 0}}, {{-2, 0, 1}, {-2, 0, 2}, {-2, 1, -2}, {-2, 1, -1}, {-2, 1, 0}, {-2, 1, 1}, {-2, 1, 2}, {-2, 2, -1}, {-2, 2, 0}, {-2, 2, 1}, {-1, -2, -2}, {-1, -2, -1}}, {{-1, -2, 0}, {-1, -2, 1}, {-1, -2, 2}, {-1, -1, -2}, {-1, -1, -1}, {-1, -1, 0}, {-1, -1, 1}, {-1, -1, 2}, {-1, 0, -2}, {-1, 0, -1}, {-1, 0, 0}, {-1, 0, 1}}, {{-1, 0, 2}, {-1, 1, -2}, {-1, 1, -1}, {-1, 1, 0}, {-1, 1, 1}, {-1, 1, 2}, {-1, 2, -2}, {-1, 2, -1}, {-1, 2, 0}, {-1, 2, 1}, {-1, 2, 2}, {0, -3, 0}}, {{0, -2, -2}, {0, -2, -1}, {0, -2, 0}, {0, -2, 1}, {0, -2, 2}, {0, -1, -2}, {0, -1, -1}, {0, -1, 0}, {0, -1, 1}, {0, -1, 2}, {0, 0, -3}, {0, 0, -2}}, {{0, 0, -1}, {0, 0, 0}, {0, 0, 1}, {0, 0, 2}, {0, 0, 3}, {0, 1, -2}, {0, 1, -1}, {0, 1, 0}, {0, 1, 1}, {0, 1, 2}, {0, 2, -2}, {0, 2, -1}}, {{0, 2, 0}, {0, 2, 1}, {0, 2, 2}, {0, 3, 0}, {1, -2, -2}, {1, -2, -1}, {1, -2, 0}, {1, -2, 1}, {1, -2, 2}, {1, -1, -2}, {1, -1, -1}, {1, -1, 0}}, {{1, -1, 1}, {1, -1, 2}, {1, 0, -2}, {1, 0, -1}, {1, 0, 0}, {1, 0, 1}, {1, 0, 2}, {1, 1, -2}, {1, 1, -1}, {1, 1, 0}, {1, 1, 1}, {1, 1, 2}}, {{1, 2, -2}, {1, 2, -1}, {1, 2, 0}, {1, 2, 1}, {1, 2, 2}, {2, -2, -1}, {2, -2, 0}, {2, -2, 1}, {2, -1, -2}, {2, -1, -1}, {2, -1, 0}, {2, -1, 1}}, {{2, -1, 2}, {2, 0, -2}, {2, 0, -1}, {2, 0, 0}, {2, 0, 1}, {2, 0, 2}, {2, 1, -2}, {2, 1, -1}, {2, 1, 0}, {2, 1, 1}, {2, 1, 2}, {2, 2, -1}}, {{2, 2, 0}, {2, 2, 1}, {3, 0, 0}}};

    /*
    To avoid unnecessary calculation, the coordinates have been given
    Original algorithm:
    // √(x^2+y^2+z^2) <=r (Based on 3D space distance formula)
    public static @NotNull String getBall(int r) {
        int x, y, z, l = 0;
        x = y = z = -r;
        StringBuilder result = new StringBuilder("{");
        for (; x <= r; ++x) {
            for (; y <= r; ++y) {
                for (; z <= r; ++z) {
                    if (Math.pow(x * x + y * y + z * z, 1.0 / 2.0) <= r) {
                        if (l % amountPerTick == 0) result.append("{");
                        result.append("{").append(x).append(",").append(y).append(",").append(z).append("}");
                        if (l % amountPerTick == (amountPerTick - 1)) result.append("},");
                        ++l;
                    }
                }
                z = -r;
            }
            y = -r;
        }
        result.append("}}");
        System.out.println("length=" + l);
        String resultString = result.toString();
        resultString = resultString.replace("}\n{", "}{");
        resultString = resultString.replace("}{", "},{");
        return resultString;
    }
    */

    public static float getTemp(World world, BlockPos pos, boolean allowRecursion) {
        try {
            float temp = 0.5F;
            if (world == null || pos == null) {
                Reg.LOGGER.error("TemperatureHelper/getTemp;world or pos is null");
                return temp;
            }
            RegistryEntry<Biome> biomeEntry = world.getBiome(pos);
            if (biomeEntry == null || biomeEntry.value() == null) {
                Reg.LOGGER.error("TemperatureHelper/getTemp;biomeEntry is empty");
                return temp;
            }
            Biome biome = biomeEntry.value();
            String biomeName = getBiomeName(biomeEntry);
            float biomeTemp = biome.computeTemperature(pos);
            temp = transferToApparentTemp(biomeTemp);
            if (world.getRegistryKey() == World.OVERWORLD) {
                float dailyTempAmplitude = getBasicDailyTempAmplitude(biomeName, biome.weather.downfall());
                //If biome is normal beach or river, getRealPain basic temperature nearby
                if ((biomeName.contains("river") || biomeName.contains("beach")) && allowRecursion) {
                    List<Float> list = new ArrayList<>();
                    for (int[] bp : POS_IN_NEARBY_CHUNKS) {
                        list.add(getTemp(world, pos.add(bp[0], 0, bp[1]), false));
                    }
                    float tempSum = 0.0F;
                    for (float temperature : list) {
                        tempSum += temperature;
                    }
                    return tempSum / (float) list.size();
                }
                //Customize some biomes' temp
                if (biomeName.contains("desert") || biomeName.contains("badlands")) temp -= 1.2F;
                else if (biomeName.contains("savanna")) temp -= 1.05F;
                else if (biomeName.contains("dark_forest") || biomeName.contains("cherry_grove") || biomeName.contains("lukewarm_ocean"))
                    temp += 0.1F;
                else if (biomeName.contains("warm_ocean") || biomeName.contains("lush_cave")) temp += 0.23F;
                else if (biomeName.contains("stony_shore") || biomeName.contains("snowy_slope")) temp += 0.4F;
                else if (biomeName.contains("snowy_taiga")) temp += 0.5F;
                else if (biomeName.contains("meadow") || biomeName.contains("birch")) temp -= 0.132F;
                else if (biomeName.contains("cold_ocean")) temp -= 0.27F;
                else if (biomeName.contains("deep_frozen_ocean")) temp = 0.0F;
                else if (biomeName.contains("stony_peak")) temp -= 0.3F;
                else if (biomeName.contains("forest")) temp -= 0.05F;
                //Height Factor
                float y = (float) pos.getY();
                float surfaceWeight = 1.0F;
                if (WorldHelper.isDeepInCave(world, pos) && !biomeName.contains("ocean")) {
                    //Hotter in deep layer
                    if (y < 44) {
                        surfaceWeight = 0.0F;
                        temp = Math.min(2.0F, 0.4F + Math.abs(44 - y) / 90.0F);
                        dailyTempAmplitude = 0.0F;
                    } else if (y < 64) {
                        //The shallower the layer is, the closer it is to the surface temperature
                        surfaceWeight = (y - 44.0F) / 20.0F;
                        temp = 0.4F * (1 - surfaceWeight) + temp * surfaceWeight;
                        dailyTempAmplitude *= surfaceWeight;
                    }
                }
                //Colder in high place
                if (y > 80) temp = Math.max(-2.0F, temp - 0.00125F * (y - 80.0F));
                //Effect of weather on temp
                if (world.isRaining() && biome.weather.downfall() > 0.0F) {
                    dailyTempAmplitude *= 0.5F;
                    if (biomeTemp >= 0.9F || (biomeTemp > 0.0F && biomeTemp < 0.3F)) temp -= 0.06F * surfaceWeight;
                    else temp -= 0.1F * surfaceWeight;
                }
                //Effect of time on temp
                return getDailyTemp(world.getLunarTime(), temp, dailyTempAmplitude);
            } else return temp;
        } catch (StackOverflowError error) {
            Reg.LOGGER.error("TemperatureHelper/getTemp(): StackOverflowError");
            return 0.5F;
        }
    }

    public static float getTemp(Object playerObj) {
        if (playerObj instanceof ServerPlayerEntity player)
            return updateEnvTempCache(player, getTemp(player.world, player.getBlockPos(), true));
        else if (playerObj instanceof PlayerEntity player) //ClientPlayerEntity
            return ((StatAccessor) player).getTemperatureManager().getEnvTempCache(); //Get C2S packet data for server-client sync
        return 0.5F;
    }

    public static String getBiomeName(RegistryEntry<Biome> biomeEntry) {
        if (biomeEntry == null) return "null";
        return biomeEntry.getKeyOrValue().map(biomeKey -> biomeKey.getValue().toString(), biomeName -> "[unregistered " + biomeName + "]");
    }

    public static float transferToApparentTemp(float defaultTemp) {
        if (defaultTemp < 0.15F) return defaultTemp - 0.15F;
        if (defaultTemp < 0.2F) return defaultTemp - 0.125F;
        if (defaultTemp < 0.35F) return defaultTemp - 0.1F;
        if (defaultTemp < 0.4F) return defaultTemp - 0.075F;
        if (defaultTemp < 0.45F) return defaultTemp - 0.05F;
        if (defaultTemp < 0.5F) return defaultTemp - 0.025F;
        if (defaultTemp < 0.55F) return defaultTemp - 0.0F;
        if (defaultTemp < 0.575F) return defaultTemp - 0.025F;
        if (defaultTemp < 0.6F) return defaultTemp - 0.05F;
        if (defaultTemp < 0.625F) return defaultTemp - 0.075F;
        if (defaultTemp < 0.65F) return defaultTemp - 0.1F;
        if (defaultTemp < 0.7F) return defaultTemp - 0.125F;
        if (defaultTemp < 0.75F) return defaultTemp - 0.15F;
        if (defaultTemp < 0.8F) return defaultTemp - 0.175F;
        if (defaultTemp < 0.85F) return defaultTemp - 0.2F;
        if (defaultTemp < 0.8625F) return defaultTemp - 0.175F;
        if (defaultTemp < 0.875F) return defaultTemp - 0.15F;
        if (defaultTemp < 0.9F) return defaultTemp - 0.125F;
        return defaultTemp - 0.1F;
    }

    public static float getDailyTemp(long time, float avgTemp, float amplitude) {
        while (time >= 24000L) time -= 24000L;
        /*
          A piecewise function simulating daily variation of temperature
          x = time (transferred to the first period)
          0 = 6:00, min temp
          8000 = 14:00, max temp
          current temp = f(x) = { avg - A*cos(π/8000 * x), x<8000
                                { avg + A*cos[π/16000 * (x-8000)], x>=8000
        */
        if (time < 8000L) return (float) (avgTemp + (-amplitude * Math.cos(Math.PI / 8000.0D * (double) time)));
        else return (float) (avgTemp + (amplitude * Math.cos(Math.PI / 16000.0D * ((double) time - 8000.0D))));
    }

    public static float getBasicDailyTempAmplitude(String biomeName, float downfall) {
        if (biomeName != null) {
            if (biomeName.contains("ocean") || biomeName.contains("jungle")) return 0.05F;
            if (biomeName.contains("desert") || biomeName.contains("badlands")) return 0.8F;
            if (biomeName.contains("snowy_beach")) return 0.07F;
            if (biomeName.contains("stony_shore")) return 0.1F;
        }
        if (downfall < 0.3F) return 0.3F;
        if (downfall < 0.35F) return 0.25F;
        if (downfall < 0.4F) return 0.2F;
        if (downfall < 0.5F) return 0.15F;
        if (downfall < 0.85F) return 0.1F;
        return 0.05F;
    }

    public static void updateAmbientBlocks(Entity playerObj) {
        if (playerObj instanceof ServerPlayerEntity player) {
            int i = (int) (player.world.getTime() % (BALL_RAD3.length));
            TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
            StatusManager statusManager = ((StatAccessor) player).getStatusManager();
            BlockPos playerPos = player.getBlockPos();
            if (i == 0) {//Do not put in for-loop
                temperatureManager.updateAmbient();
                statusManager.updateOxygenGen();
            }
            int[][] bp = BALL_RAD3[i];
            for (int[] bpp : bp) {
                BlockPos checkPos = playerPos.add(bpp[0], bpp[1], bpp[2]);
                BlockState state = player.world.getBlockState(checkPos);
                Block block = state.getBlock();
//                if (player.getMainHandStack().isOf(Items.DEBUG_STICK)) player.world.setBlockState(player.getBlockPos().addRawPain(bpp[0], bpp[1], bpp[2]), Blocks.COBBLESTONE.getDefaultState());
                RegistryEntry<Biome> biomeEntry = player.world.getBiome(playerPos);
                Biome biome = biomeEntry.value();
                if (biome != null) {
                    if (!biome.isCold(playerPos) && !getBiomeName(biomeEntry).contains("frozen")) {
                        if (block == Blocks.ICE || block == Blocks.BLUE_ICE || block == Blocks.FROSTED_ICE || block == Blocks.PACKED_ICE || block == Blocks.SOUL_FIRE || block == Blocks.SOUL_CAMPFIRE)
                            temperatureManager.addAmbient(-0.1F);
                        else if (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK || block == Blocks.POWDER_SNOW || block == Blocks.POWDER_SNOW_CAULDRON)
                            temperatureManager.addAmbient(-0.06F);
                    }
                }
                if (block == Blocks.FIRE || block == Blocks.CAMPFIRE) temperatureManager.addAmbient(0.1F);
                else if (block instanceof AbstractFurnaceBlock && state.get(AbstractFurnaceBlock.LIT))
                    temperatureManager.addAmbient(0.3F);
                else if (block == Blocks.MAGMA_BLOCK) temperatureManager.addAmbient(0.5F);
                else if (block == Blocks.LAVA || block == Blocks.LAVA_CAULDRON) temperatureManager.addAmbient(2.0F);
                else if (block == Blocks.TORCH) temperatureManager.addAmbient(0.01F);
                //Addition: check oxygen generation
                if ((block instanceof LeavesBlock || (block instanceof PlantBlock && !(block instanceof RootsBlock) && block != Blocks.DEAD_BUSH) || block == Blocks.GRASS_BLOCK)/* && player.world.raycast(new RaycastContext(player.getPos(), new Vec3d(checkPos.getX(), checkPos.getY(), checkPos.getZ()), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player)).getType() != HitResult.Type.MISS*/) {
                    for (BlockPos immediatePos : new BlockPos[]{checkPos.up(), checkPos.down(), checkPos.east(), checkPos.south(), checkPos.west(), checkPos.north()}) {
                        if (player.world.getLightLevel(LightType.BLOCK, immediatePos) > 5) statusManager.addOxygenGen();
                        break;
                    }
                }
            }
        }
    }

    public static float updateEnvTempCache(ServerPlayerEntity player, float val) {
        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
        if (temperatureManager == null) return val;
        //Ambient blocks factor
        float ambientCache = temperatureManager.getAmbientCache();
        val += ambientCache;
        //Make a fire to prevent freeze to death
        if (ambientCache >= 0.1F && val <= 0.0F) val = 0.01F;
        else if (ambientCache <= -0.1F && val >= 1.0F && val <= 2.0F) val = 0.99F;
        temperatureManager.setEnvTempCache(val);
        return val;
    }

    public static float getFeelingTemp(Object playerObj, float envTemp, String biomeName, int skyLightLevel) {
        float x = envTemp;
        if (playerObj instanceof ServerPlayerEntity player) {
            StatusEffectInstance chillyWindEffect = player.getStatusEffect(HcsEffects.CHILLY_WIND), strongSunEffect = player.getStatusEffect(HcsEffects.STRONG_SUN);
            StatusManager statusManager = ((StatAccessor) player).getStatusManager();
            float insulationLevel = (float) getClothingInsulationLevel(player), insulation = insulationLevel * 0.025F;
            if (strongSunEffect != null) x += (strongSunEffect.getAmplifier() + 1) * 0.05F;
            else if (chillyWindEffect != null) x -= (chillyWindEffect.getAmplifier() + 1) * 0.05F;
            else if (skyLightLevel < 15) {
                //Cooler when in the shade
                if (biomeName.contains("savanna") && x >= 1.0F && x <= 1.14F)
                    x = 0.99F; //Can't hurt by heatstroke in the shade
                else if (biomeName.contains("desert") || biomeName.contains("badlands")) {
                    float tempInFullShade = x;
                    int outdoorsWeight = Math.max(0, skyLightLevel - 9);
                    if (x >= 0.0F && x < 1.0F) tempInFullShade = x * 0.7F;
                    else if (x > 1.0F && x < 2.0F) tempInFullShade = (float) (0.25 * Math.pow(x - 0.51, 2) + 0.64);
                    x = (outdoorsWeight * x + (5 - outdoorsWeight) * tempInFullShade) / 5.0F;
                }
            }
            //Feel much colder when wet
            float x1 = x;
            if (x < 0.0F) x1 = 0.0F;
            else if (x > 1.05F && x <= 1.5F) x1 = 1.05F;
            float x2 = x1 * x1 - 0.18F;
            double wet = ((StatAccessor) player).getWetnessManager().get();
            float wetFactor = (float) Math.pow(wet, 1.4);
            x = wetFactor * x2 + (1 - wetFactor) * x;
            if (wet > 0.3 && x >= 0.95F && x <= 1.48F) x = 0.95F;
            x += insulation * (1 - wetFactor);
            if (insulationLevel * (1 - wetFactor) >= 19.0F) {
                //Fully wear woolen suit will gain an award of protection from bitter cold
                if (x + insulation <= 0.0F) {
                    if (x + insulation >= -0.7F) x = 0.01F;
                    else x += 0.7F;
                }
                if (envTemp < -0.2F) x += 0.15F;
                else if (envTemp < -0.1F) x += 0.1F;
                else if (envTemp < 0.0F) x += 0.05F;
            }
            if (player.isSprinting() || statusManager.getRecentAttackTicks() > 0) { //Heat from doing sport
                if (x <= 0.0F) x = 0.7F * x + 0.2F;
                else if (x <= 1.0F)
                    x = (float) (0.83 * Math.pow(x, 0.7) + 0.2);
                else x += 0.03F;
            }
            if (statusManager.getRecentHasHotWaterBagTicks() > 0) x = Math.max(x + 0.2F, 0.15F);
            if (statusManager.getRecentHasColdWaterBagTicks() > 0) x = Math.min(x - 0.2F, 0.85F);
            ((StatAccessor) player).getTemperatureManager().setFeelTempCache(x);
        } else if (playerObj instanceof PlayerEntity player) {
            //ClientPlayerEntity
            return ((StatAccessor) player).getTemperatureManager().getFeelTempCache();
        }
        return x;
    }

    public static boolean isSpecialSunshineArea(String biomeName) {
        if (biomeName == null) return false;
        return biomeName.contains("jungle") || biomeName.contains("peak") || biomeName.contains("slope");
    }

    public static int getSunshineIntensityLevel(long time, boolean isRaining, String biomeName) {
        //max:4 min:-3
        if (isRaining) return 0;
        while (time >= 24000L) time -= 24000L;
        float intensity = Math.max(0.0F, (float) Math.sin(Math.PI / 12000 * time));
        int level = Math.abs((int) Math.ceil(4 * intensity));
        if (isSpecialSunshineArea(biomeName) && level > 1) level = 1;
        return level;
    }

    public static int getWindchillLevel(World world, BlockPos pos, float envTempReal, RegistryEntry<Biome> biomeEntry) {
        int result = 0;
        if (world == null || pos == null || biomeEntry == null) {
            Reg.LOGGER.error("TemperatureHelper/getWindchillLevel; world, pos, biomeEntry is null");
            return result;
        }
        Biome biome = biomeEntry.value();
        if (envTempReal > 0.0F || !biome.isCold(pos) || world.getLightLevel(LightType.SKY, pos) < 1 || (!world.isSkyVisible(pos) && world.getLightLevel(LightType.BLOCK, pos) > 7))
            return result;
        String biomeName = getBiomeName(biomeEntry);
        boolean isInForest = biomeName.contains("taiga") || biomeName.contains("forest");
        //Calculate windchill level according to time and isInForest
        long time = world.getLunarTime();
        while (time >= 24000) time -= 24000L;
        float basicWindchillLevel;
        if (time < 4000) basicWindchillLevel = -0.001F * time + 4.0F;
        else if (time < 10000) basicWindchillLevel = 0.0F;
        else basicWindchillLevel = 4.0F * (float) Math.sin(Math.PI / 28000.0D * (time + 46000.0D));
        result = Math.min(6, Math.abs(Math.round(basicWindchillLevel + (world.isRaining() ? (isInForest ? 1.0F : 3.0F) : 0.0F))));
        if (isInForest) {
            if (!world.isRaining() && result > 1) result = 1;
            else if (result > 2) result = 2;
        }
        return result;
    }

    public static int getClothingInsulationLevel(Object playerObj) {
        int level = 0;
        if (playerObj instanceof ServerPlayerEntity player) {
            for (ItemStack stack : player.getArmorItems()) {
                if (stack.isOf(Items.LEATHER_BOOTS)) level += 1;
                else if (stack.isOf(Items.LEATHER_HELMET) || stack.isOf(Items.LEATHER_LEGGINGS) || stack.isOf(Reg.WOOLEN_BOOTS))
                    level += 2;
                else if (stack.isOf(Items.LEATHER_CHESTPLATE)) level += 3;
                else if (stack.isOf(Reg.WOOLEN_HOOD) || stack.isOf(Reg.WOOLEN_TROUSERS)) level += 5;
                else if (stack.isOf(Reg.WOOLEN_COAT)) level += 7;
            }
        }
        return level;
    }


}
