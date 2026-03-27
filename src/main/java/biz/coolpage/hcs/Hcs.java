package biz.coolpage.hcs;

import biz.coolpage.hcs.block.BurntCampfireBlock;
import biz.coolpage.hcs.block.DryingRackBlock;
import biz.coolpage.hcs.block.IceboxBlock;
import biz.coolpage.hcs.block.SmolderingCampfireBlock;
import biz.coolpage.hcs.block.torches.*;
import biz.coolpage.hcs.config.Config__;
import biz.coolpage.hcs.entity.*;
import biz.coolpage.hcs.item.*;
import biz.coolpage.hcs.item.BottleItem;
import biz.coolpage.hcs.recipe.*;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import biz.coolpage.hcs.util.CombustionHelper;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.WorldHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.function.Predicate;

import static biz.coolpage.hcs.config.Configs.FOOD_SPOIL;
import static net.minecraft.commands.Commands.literal;

@Mod(Hcs.MOD_ID)
public final class Hcs {
    public static final String MOD_ID = "hcsurvival", MOD_NAME = "Hardcore Survival";
    private static final Logger LOGGER = LogUtils.getLogger();

    // Registers
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    private static <T> T register(String name, T registry) {
        if (registry instanceof Item item) {
            ITEMS.register(name, () -> item);
        } else if (registry instanceof Block block) {
            BLOCKS.register(name, () -> block);
        } else if (registry instanceof Potion potion) {
            POTIONS.register(name, () -> potion);
        } else if (registry instanceof BlockEntityType<?> blockEntityType) {
            BLOCK_ENTITIES.register(name, () -> blockEntityType);
        } else if (registry instanceof EntityType<?> entityType) {
            ENTITIES.register(name, () -> entityType);
        } else if (registry instanceof RecipeSerializer<?> recipeSerializer) {
            RECIPE_SERIALIZERS.register(name, () -> recipeSerializer);
        } else {
            error("Cannot find type " + registry + " to register " + name);
        }
        return registry;
    }

    // --- Blocks ---
    public static final Block
            ICEBOX = register("icebox", new IceboxBlock(BlockBehaviour.Properties.copy(Blocks.COBBLESTONE).mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0F).requiresCorrectToolForDrops().noOcclusion())),
            DRYING_RACK = register("drying_rack", new DryingRackBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE).mapColor(MapColor.WOOD).strength(1.5F, 2.0F).requiresCorrectToolForDrops().noOcclusion())),
            CRUDE_TORCH_BLOCK = register("crude_torch", new CrudeTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).noCollission().instabreak().lightLevel(state -> 0).sound(SoundType.WOOD))),
            WALL_CRUDE_TORCH_BLOCK = register("wall_crude_torch", new WallCrudeTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).noCollission().instabreak().lightLevel(state -> 0).sound(SoundType.WOOD).dropsLike(CRUDE_TORCH_BLOCK))),
            BURNING_CRUDE_TORCH_BLOCK = register("burning_crude_torch", new BurningCrudeTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).noCollission().instabreak().lightLevel(state -> 13).sound(SoundType.WOOD))),
            WALL_BURNING_CRUDE_TORCH_BLOCK = register("wall_burning_crude_torch", new WallBurningCrudeTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).noCollission().instabreak().lightLevel(state -> 13).sound(SoundType.WOOD).dropsLike(BURNING_CRUDE_TORCH_BLOCK))),
            UNLIT_TORCH_BLOCK = register("unlit_torch", new UnlitTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).lightLevel(state -> 0).noCollission().instabreak().sound(SoundType.WOOD))),
            WALL_UNLIT_TORCH_BLOCK = register("wall_unlit_torch", new WallUnlitTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).noCollission().instabreak().sound(SoundType.WOOD).dropsLike(UNLIT_TORCH_BLOCK))),
            BURNT_TORCH_BLOCK = register("burnt_torch", new BurntTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).lightLevel(state -> 0).noCollission().instabreak().sound(SoundType.WOOD))),
            WALL_BURNT_TORCH_BLOCK = register("wall_burnt_torch", new WallBurntTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).lightLevel(state -> 0).noCollission().instabreak().sound(SoundType.WOOD).dropsLike(BURNT_TORCH_BLOCK))),
            GLOWSTONE_TORCH_BLOCK = register("glowstone_torch", new GlowstoneTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).noCollission().instabreak().lightLevel(state -> 15).sound(SoundType.WOOD))),
            WALL_GLOWSTONE_TORCH_BLOCK = register("wall_glowstone_torch", new WallGlowstoneTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).noCollission().instabreak().lightLevel(state -> 14).sound(SoundType.WOOD).dropsLike(GLOWSTONE_TORCH_BLOCK))),
            SMOLDERING_CAMPFIRE_BLOCK = register("smoldering_campfire", new SmolderingCampfireBlock()),
            BURNT_CAMPFIRE_BLOCK = register("burnt_campfire", new BurntCampfireBlock());

    // --- Items ---
    public static final Item
            FIBER_STRING = register("fiber_string", new Item(new Item.Properties())),
            GRASS_FIBER = register("grass_fiber", new Item(new Item.Properties())),
            ROASTED_SEEDS = register("roasted_seeds", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0f).fast().build()))),
            ROCK = register("rock", new RockItem(new Item.Properties())),
            SHARP_ROCK = register("sharp_rock", new Item(new Item.Properties())),
            SHARP_FLINT = register("sharp_flint", new Item(new Item.Properties())),
            FIREWOOD = register("firewood", new Item(new Item.Properties())),
            FRIED_EGG = register("fried_egg", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationMod(2f).build()))),
            EXTINGUISHED_CAMPFIRE = register("extinguished_campfire", new HCSCampfireItem(Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, false).setValue(CombustionHelper.COMBUST_LUMINANCE, 15))),
            FIRE_BOW = register("fire_bow", new FireBowItem(new Item.Properties().stacksTo(1).durability(96), 1)),
            FIRE_PLOUGH = register("fire_plough", new FireBowItem(new Item.Properties().stacksTo(1).durability(64), 3)),
            TINDER = register("tinder", new Item(new Item.Properties())),
            WORM = register("worm", new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200), 1).build()), 0.0F, -0.08)),
            PUMPKIN_SLICE = register("pumpkin_slice", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(0f).build()))),
            POTHERB = register("potherb", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(1f).build()))),
            STONE_KNIFE = register("stone_knife", new KnifeItem(HcsTiers.STONE_WEAPON, 1, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            STONE_SPEAR = register("stone_spear", new SwordItem(HcsTiers.STONE_WEAPON, 2, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            STONE_CONE = register("stone_cone", new ShovelItem(HcsTiers.STONE_CONE, 1.0F, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            FLINT_KNIFE = register("flint_knife", new KnifeItem(HcsTiers.FLINT_WEAPON, 2, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            FLINT_SPEAR = register("flint_spear", new SwordItem(HcsTiers.FLINT_WEAPON, 3, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            FLINT_CONE = register("flint_cone", new ShovelItem(HcsTiers.FLINT_CONE, 1.5F, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            FLINT_HATCHET = register("flint_hatchet", new AxeItem(HcsTiers.FLINT_HATCHET, 6.0F, -3.1F, new Item.Properties())),
            RAW_COPPER_POWDER = register("raw_copper_powder", new Item(new Item.Properties())),
            COPPER_SWORD = register("copper_sword", new SwordItem(HcsTiers.COPPER, 5, 1.4F - 4.0F, new Item.Properties().stacksTo(1))),
            COPPER_AXE = register("copper_axe", new AxeItem(HcsTiers.COPPER, 7, 0.8F - 4.0F, new Item.Properties().stacksTo(1))),
            COPPER_PICKAXE = register("copper_pickaxe", new PickaxeItem(HcsTiers.COPPER, 3, 1.1F - 4.0F, new Item.Properties().stacksTo(1))),
            COPPER_HOE = register("copper_hoe", new HoeItem(HcsTiers.COPPER, 0, 2.5F - 4.0F, new Item.Properties().stacksTo(1))),
            COPPER_SHOVEL = register("copper_shovel", new ShovelItem(HcsTiers.COPPER, 3, 1.0F - 4.0F, new Item.Properties().stacksTo(1))),
            COPPER_HELMET = register("copper_helmet", new ArmorItem(HcsArmorMaterials.COPPER, ArmorItem.Type.HELMET, new Item.Properties())),
            COPPER_CHESTPLATE = register("copper_chestplate", new ArmorItem(HcsArmorMaterials.COPPER, ArmorItem.Type.CHESTPLATE, new Item.Properties())),
            COPPER_LEGGINGS = register("copper_leggings", new ArmorItem(HcsArmorMaterials.COPPER, ArmorItem.Type.LEGGINGS, new Item.Properties())),
            COPPER_BOOTS = register("copper_boots", new ArmorItem(HcsArmorMaterials.COPPER, ArmorItem.Type.BOOTS, new Item.Properties())),
            SPIDER_GLAND = register("spider_gland", new SalveItem(8, 0.5)),
            SELAGINELLA = register("selaginella", new SalveItem(20, 1.5) {
                @Override
                public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity user) {
                    if (user instanceof ServerPlayer player) {
                        ((StatAccessor) player).getStatusManager().setSoulImpairedStat(0);
                        ((StatAccessor) player).getInjuryManager().applyPainkiller();
                    }
                    return super.finishUsingItem(stack, world, user);
                }
            }),
            RAW_MEAT = register("raw_meat", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(1f).meat().build()))),
            COOKED_MEAT = register("cooked_meat", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationMod(3f).meat().build()))),
            CACTUS_FLESH = register("cactus_flesh", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1f).effect(() -> new MobEffectInstance(MobEffects.POISON, 160), 1).effect(() -> new MobEffectInstance(HcsEffects.DIARRHEA, 300), 1).build()))),
            COOKED_CACTUS_FLESH = register("cooked_cactus_flesh", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(2f).effect(() -> new MobEffectInstance(MobEffects.POISON, 80), 1).build()))),
            CACTUS_JUICE = register("cactus_juice", new BottleItem(new Item.Properties().stacksTo(16), new MobEffectInstance(MobEffects.POISON, 160))),
            PURIFIED_WATER_BOTTLE = register("purified_water_bottle", new BottleItem(new Item.Properties().stacksTo(16))),
            SALTWATER_BOTTLE = register("saltwater_bottle", new BottleItem(new Item.Properties().stacksTo(16), new MobEffectInstance(HcsEffects.THIRST, 1200, 0, false, false, true))),
            ROASTED_WORM = register("roasted_worm", new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(1.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 60), 1).build()), 0.0F, -0.01)),
            ANIMAL_VISCERA = register("animal_viscera", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationMod(2.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 40), 1).meat().build()))),
            COOKED_ANIMAL_VISCERA = register("cooked_animal_viscera", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationMod(4F).meat().build()))),
            SHARP_BROKEN_BONE = register("sharp_broken_bone", new ShovelItem(HcsTiers.SHARP_BROKEN_BONE, 3F, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            BAMBOO_SHOOT = register("bamboo_shoot", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1f).build()))),
            COOKED_BAMBOO_SHOOT = register("cooked_bamboo_shoot", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(1f).build()))),
            COOKED_CARROT = register("cooked_carrot", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationMod(2f).build()))),
            COOKED_PUMPKIN_SLICE = register("cooked_pumpkin_slice", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(0f).build()))),
            COOKED_SWEET_BERRIES = register("cooked_sweet_berries", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1f).build()))),
            BERRY_BUSH = register("berry_bush", new Item(new Item.Properties())),
            PETALS_SALAD = register("petals_salad", new BowlOfFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0f).build()))),
            ORANGE = register("orange", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationMod(3f).build()))),
            ROT = register("rot", new BoneMealItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0.0f).build())) {
                @Override
                public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
                    if (context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.GRASS_BLOCK))
                        return InteractionResult.PASS;
                    return super.useOn(context);
                }
            }),
            ICEBOX_ITEM = register("icebox", new BlockItem(ICEBOX, new Item.Properties())),
            DRYING_RACK_ITEM = register("drying_rack", new BlockItem(DRYING_RACK, new Item.Properties())),
            SHORT_STICK = register("short_stick", new Item(new Item.Properties())),
            JERKY = register("jerky", new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationMod(8.0F).meat().build()), 3.0F, 0.15)),
            SMALL_JERKY = register("small_jerky", new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationMod(5.0F).meat().build()), 1.5F, 0.08)),
            RAW_JERKY = register("raw_jerky", new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationMod(4.0F).meat().build()), 1.5F, 0.0)),
            RAW_SMALL_JERKY = register("raw_small_jerky", new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationMod(3.0F).meat().build()), 1.0F, 0.0)),
            SPIKED_CLUB = register("spiked_club", new SwordItem(Tiers.WOOD, 4, -2.4f, new Item.Properties())),
            COLD_WATER_BOTTLE = register("cold_water_bottle", new BottleItem(new Item.Properties().stacksTo(16)) {
                @Override
                public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity user) {
                    if (user instanceof ServerPlayer player) {
                        TemperatureManager tm = ((StatAccessor) player).getTemperatureManager();
                        if (tm.get() > 0.4) tm.add(-0.3);
                        player.addEffect(new MobEffectInstance(HcsEffects.DIARRHEA, 600, 0, false, false, true));
                    }
                    return super.finishUsingItem(stack, world, user);
                }
            }),
            HOT_WATER_BOTTLE = register("hot_water_bottle", new HotWaterBottleItem()),
            WOOLEN_HOOD = register("woolen_hood", new ArmorItem(HcsArmorMaterials.WOOL, ArmorItem.Type.HELMET, new Item.Properties())),
            WOOLEN_COAT = register("woolen_coat", new ArmorItem(HcsArmorMaterials.WOOL, ArmorItem.Type.CHESTPLATE, new Item.Properties())),
            WOOLEN_TROUSERS = register("woolen_trousers", new ArmorItem(HcsArmorMaterials.WOOL, ArmorItem.Type.LEGGINGS, new Item.Properties())),
            WOOLEN_BOOTS = register("woolen_boots", new ArmorItem(HcsArmorMaterials.WOOL, ArmorItem.Type.BOOTS, new Item.Properties())),
            COOKED_KELP = register("cooked_kelp", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0F).build()))),
            BARK = register("bark", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0.0F).build()))),
            WILLOW_BARK = register("willow_bark", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0.0F).build()))),
            FEARLESSNESS_HERB = register("fearlessness_herb", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0.0F).build()))),
            BANDAGE = register("bandage", new BandageItem(2.0, 40, 200)),
            IMPROVISED_BANDAGE = register("improvised_bandage", new BandageItem(0.8, 60, 120)),
            SPLINT = register("splint", new BandageItem(0.5, 140) {
                @Override
                public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity user) {
                    if (user instanceof ServerPlayer player)
                        ((StatAccessor) player).getInjuryManager().setFracture(0.0);
                    return super.finishUsingItem(stack, world, user);
                }
            }),
            GINGER = register("ginger", new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0F).build()))),
            WOODEN_HELMET = register("wooden_helmet", new ArmorItem(HcsArmorMaterials.WOOD, ArmorItem.Type.HELMET, new Item.Properties())),
            WOODEN_CHESTPLATE = register("wooden_chestplate", new ArmorItem(HcsArmorMaterials.WOOD, ArmorItem.Type.CHESTPLATE, new Item.Properties())),
            WOODEN_LEGGINGS = register("wooden_leggings", new ArmorItem(HcsArmorMaterials.WOOD, ArmorItem.Type.LEGGINGS, new Item.Properties())),
            WOODEN_BOOTS = register("wooden_boots", new ArmorItem(HcsArmorMaterials.WOOD, ArmorItem.Type.BOOTS, new Item.Properties())),
            IMPROVISED_SHIELD = register("improvised_shield", new ShieldItem(new Item.Properties().durability(48)) {
                @Contract(pure = true)
                @Override
                public @NotNull String getDescriptionId(@NotNull ItemStack stack) {
                    return "item.hcs.improvised_shield";
                }
            }),
            BAT_WINGS = register("bat_wings", new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200), 1).effect(() -> new MobEffectInstance(HcsEffects.DIARRHEA, 200), 0.7F).build()), 0.0F, -0.06)),
            ROASTED_BAT_WINGS = register("roasted_bat_wings", new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 60), 1).build()), 0.0F, -0.01)),
            HEALING_SALVE = register("healing_salve", new SalveItem(14, 1.5, 20) {
                @Override
                public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity user) {
                    if (user instanceof ServerPlayer player) EntityHelper.dropItem(player, Items.BOWL);
                    return super.finishUsingItem(stack, world, user);
                }
            }),
            ASHES = register("ashes", new SalveItem(0, 0.2, 50)),
            CRUDE_TORCH_ITEM = register("crude_torch", new StandingAndWallBlockItem(CRUDE_TORCH_BLOCK, WALL_CRUDE_TORCH_BLOCK, new Item.Properties(), Direction.DOWN) {
                @Override
                public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
                    InteractionResult performed = CombustionHelper.preLitHoldingTorch(context);
                    return performed == null ? super.useOn(context) : performed;
                }
            }),
            BURNING_CRUDE_TORCH_ITEM = register("burning_crude_torch", new BurningCrudeTorchItem()),
            UNLIT_TORCH_ITEM = register("unlit_torch", new StandingAndWallBlockItem(UNLIT_TORCH_BLOCK, WALL_UNLIT_TORCH_BLOCK, new Item.Properties(), Direction.DOWN) {
                @Override
                public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
                    InteractionResult performed = CombustionHelper.preLitHoldingTorch(context);
                    return performed == null ? super.useOn(context) : performed;
                }
            }),
            GLOWSTONE_TORCH_ITEM = register("glowstone_torch", new StandingAndWallBlockItem(GLOWSTONE_TORCH_BLOCK, WALL_GLOWSTONE_TORCH_BLOCK, new Item.Properties(), Direction.DOWN)),
            BOOSTER_SHOT = register("booster_shot", new BoosterShotItem()),
            SMOLDERING_CAMPFIRE = register("smoldering_campfire", new HCSCampfireItem(SMOLDERING_CAMPFIRE_BLOCK.defaultBlockState())),
            BURNT_CAMPFIRE = register("burnt_campfire", new HCSCampfireItem(BURNT_CAMPFIRE_BLOCK.defaultBlockState())),
            GARLAND = register("garland", new ArmorItem(HcsArmorMaterials.GARLAND, ArmorItem.Type.HELMET, new Item.Properties()));

    // --- Potions ---
    public static final Potion
            IRONSKIN_POTION = register("hcs_ironskin", new Potion(new MobEffectInstance(HcsEffects.IRONSKIN, 3600, 0))),
            LONG_IRONSKIN_POTION = register("hcs_long_ironskin", new Potion(new MobEffectInstance(HcsEffects.IRONSKIN, 9600, 0))),
            STRONG_IRONSKIN_POTION = register("hcs_strong_ironskin", new Potion(new MobEffectInstance(HcsEffects.IRONSKIN, 1800, 1))),
            RETURN_POTION = register("hcs_return", new Potion(new MobEffectInstance(HcsEffects.RETURN, 120, 0, false, true, false))),
            MINING_POTION = register("hcs_mining", new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 3600))),
            LONG_MINING_POTION = register("hcs_long_mining", new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 9600))),
            STRONG_MINING_POTION = register("hcs_strong_mining", new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 1800, 1))),
            CONSTANT_TEMPERATURE_POTION = register("hcs_constant_temperature", new Potion(new MobEffectInstance(HcsEffects.CONSTANT_TEMPERATURE, 3600))),
            LONG_CONSTANT_TEMPERATURE_POTION = register("hcs_long_constant_temperature", new Potion(new MobEffectInstance(HcsEffects.CONSTANT_TEMPERATURE, 9600))),
            PAIN_KILLING_POTION = register("hcs_pain_killing", new Potion(new MobEffectInstance(HcsEffects.PAIN_KILLING, 3600))),
            LONG_PAIN_KILLING_POTION = register("hcs_long_pain_killing", new Potion(new MobEffectInstance(HcsEffects.PAIN_KILLING, 9600))),
            FEARLESSNESS_POTION = register("hcs_fearlessness", new Potion(new MobEffectInstance(HcsEffects.FEARLESSNESS, 3600))),
            LONG_FEARLESSNESS_POTION = register("hcs_long_fearlessness", new Potion(new MobEffectInstance(HcsEffects.FEARLESSNESS, 9600)));

    // --- Entity Types ---
    public static final EntityType<RockProjectileEntity> ROCK_PROJECTILE_ENTITY = register("rock_projectile_entity", EntityType.Builder.<RockProjectileEntity>of(RockProjectileEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).build("rock_projectile_entity"));
    public static final EntityType<FlintProjectileEntity> FLINT_PROJECTILE_ENTITY = register("flint_projectile_entity", EntityType.Builder.<FlintProjectileEntity>of(FlintProjectileEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).build("flint_projectile_entity"));

    // --- Block Entities ---
    public static final BlockEntityType<IceboxBlockEntity> ICEBOX_BLOCK_ENTITY = register("icebox_block_entity", BlockEntityType.Builder.of(IceboxBlockEntity::new, ICEBOX).build(null));
    public static final BlockEntityType<DryingRackBlockEntity> DRYING_RACK_BLOCK_ENTITY = register("drying_rack_block_entity", BlockEntityType.Builder.of(DryingRackBlockEntity::new, DRYING_RACK).build(null));
    public static final BlockEntityType<BurningCrudeTorchBlockEntity> BURNING_CRUDE_TORCH_BLOCK_ENTITY = register("burning_crude_torch", BlockEntityType.Builder.of(BurningCrudeTorchBlockEntity::new, BURNING_CRUDE_TORCH_BLOCK, WALL_BURNING_CRUDE_TORCH_BLOCK).build(null));
    public static final BlockEntityType<SmolderingOrBurntCampfireBlockEntity> SMOLDERING_OR_BURNT_CAMPFIRE_BLOCK_ENTITY_BLOCK_ENTITY_TYPE = register("smoldering_or_burnt_campfire", BlockEntityType.Builder.of(SmolderingOrBurntCampfireBlockEntity::new, SMOLDERING_CAMPFIRE_BLOCK, BURNT_CAMPFIRE_BLOCK).build(null));

    // --- Recipe Serializers ---
    public static final RecipeSerializer<?>
            EXTRACT_WATER_FROM_BAMBOO_RECIPE = register("hcs_extract_water_from_bamboo", new SimpleCraftingRecipeSerializer<>(ExtractWaterFromBambooRecipe::new)),
            EXTRACT_WATER_FROM_SNOW_RECIPE = register("hcs_extract_water_from_snow", new SimpleCraftingRecipeSerializer<>(ExtractWaterFromSnowRecipe::new)),
            PETALS_SALAD_RECIPE = register("hcs_petals_salad", new SimpleCraftingRecipeSerializer<>(PetalsSaladRecipe::new)),
            SPIKED_CLUB_RECIPE = register("hcs_spiked_club_recipe", new SimpleCraftingRecipeSerializer<>(SpikedClubRecipe::new)),
            COLD_WATER_BOTTLE_RECIPE = register("hcs_cold_water_bottle_recipe", new SimpleCraftingRecipeSerializer<>(ColdWaterBottleRecipe::new)),
            HOT_WATER_BOTTLE_RECIPE = register("hcs_hot_water_bottle_recipe", new SimpleCraftingRecipeSerializer<>(HotWaterBottleRecipe::new)),
            SAPLING_TO_STICK_RECIPE = register("hcs_sapling_to_stick_recipe", new SimpleCraftingRecipeSerializer<>(SaplingToStickRecipe::new)),
            POUR_OUT_CONTENT_RECIPE = register("hcs_pour_out_content_recipe", new SimpleCraftingRecipeSerializer<>(PourOutContentRecipe::new)),
            TORCH_IGNITE_RECIPE = register("torch_ignite", new SimpleCraftingRecipeSerializer<>(TorchIgniteRecipe::new));

    // GameRules
    public static final GameRules.Key<GameRules.IntegerValue> HCS_DIFFICULTY = GameRules.register("hcsDifficulty", GameRules.Category.PLAYER, GameRules.IntegerValue.create(1));

    public static final Predicate<Item> IS_BARK = item -> item == BARK || item == WILLOW_BARK;

    // Creative Tab
    public static final RegistryObject<CreativeModeTab> HCS_TAB = CREATIVE_MODE_TABS.register(MOD_ID, () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("itemGroup.hcsurvival.main"))
            .icon(FLINT_HATCHET::getDefaultInstance)
            .displayItems((parameters, output) -> {
                output.accept(GRASS_FIBER);
                output.accept(FIBER_STRING);
                output.accept(SHORT_STICK);
                output.accept(TINDER);
                output.accept(FIREWOOD);
                output.accept(FIRE_PLOUGH);
                output.accept(FIRE_BOW);
                output.accept(EXTINGUISHED_CAMPFIRE);
                output.accept(SMOLDERING_CAMPFIRE);
                output.accept(BURNT_CAMPFIRE);
                output.accept(ASHES);
                output.accept(ROCK);
                output.accept(SHARP_ROCK);
                output.accept(SHARP_FLINT);
                output.accept(RAW_COPPER_POWDER);
                output.accept(Items.WOODEN_SWORD);
                output.accept(SPIKED_CLUB);
                output.accept(SHARP_BROKEN_BONE);
                output.accept(STONE_CONE);
                output.accept(STONE_KNIFE);
                output.accept(STONE_SPEAR);
                output.accept(FLINT_CONE);
                output.accept(FLINT_KNIFE);
                output.accept(FLINT_SPEAR);
                output.accept(FLINT_HATCHET);
                output.accept(COPPER_SWORD);
                output.accept(COPPER_AXE);
                output.accept(COPPER_PICKAXE);
                output.accept(COPPER_HOE);
                output.accept(COPPER_SHOVEL);
                output.accept(COPPER_HELMET);
                output.accept(COPPER_CHESTPLATE);
                output.accept(COPPER_LEGGINGS);
                output.accept(COPPER_BOOTS);
                output.accept(WOOLEN_HOOD);
                output.accept(WOOLEN_COAT);
                output.accept(WOOLEN_TROUSERS);
                output.accept(WOOLEN_BOOTS);
                output.accept(WOODEN_HELMET);
                output.accept(WOODEN_CHESTPLATE);
                output.accept(WOODEN_LEGGINGS);
                output.accept(WOODEN_BOOTS);
                output.accept(GARLAND);
                output.accept(IMPROVISED_SHIELD);
                output.accept(HOT_WATER_BOTTLE.getDefaultInstance());
                output.accept(WORM);
                output.accept(ROASTED_WORM);
                output.accept(BAT_WINGS);
                output.accept(ROASTED_BAT_WINGS);
                output.accept(RAW_MEAT);
                output.accept(COOKED_MEAT);
                output.accept(ANIMAL_VISCERA);
                output.accept(COOKED_ANIMAL_VISCERA);
                output.accept(CACTUS_FLESH);
                output.accept(COOKED_CACTUS_FLESH);
                output.accept(BAMBOO_SHOOT);
                output.accept(COOKED_BAMBOO_SHOOT);
                output.accept(PUMPKIN_SLICE);
                output.accept(COOKED_PUMPKIN_SLICE);
                output.accept(ROASTED_SEEDS);
                output.accept(COOKED_CARROT);
                output.accept(COOKED_SWEET_BERRIES);
                output.accept(FRIED_EGG);
                output.accept(COOKED_KELP);
                output.accept(POTHERB);
                output.accept(ORANGE);
                output.accept(GINGER);
                output.accept(PETALS_SALAD);
                output.accept(JERKY);
                output.accept(SMALL_JERKY);
                output.accept(RAW_JERKY);
                output.accept(RAW_SMALL_JERKY);
                output.accept(ROT);
                output.accept(SELAGINELLA);
                output.accept(SPIDER_GLAND);
                output.accept(BERRY_BUSH);
                output.accept(BARK);
                output.accept(WILLOW_BARK);
                output.accept(FEARLESSNESS_HERB);
                output.accept(IMPROVISED_BANDAGE);
                output.accept(BANDAGE);
                output.accept(HEALING_SALVE);
                output.accept(SPLINT);
                output.accept(BOOSTER_SHOT);
                output.accept(PURIFIED_WATER_BOTTLE);
                output.accept(SALTWATER_BOTTLE);
                output.accept(COLD_WATER_BOTTLE);
                output.accept(CACTUS_JUICE);
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), IRONSKIN_POTION));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), RETURN_POTION));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), MINING_POTION));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), CONSTANT_TEMPERATURE_POTION));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), PAIN_KILLING_POTION));
                output.accept(PotionUtils.setPotion(new ItemStack(Items.POTION), FEARLESSNESS_POTION));
                output.accept(ICEBOX_ITEM);
                output.accept(DRYING_RACK_ITEM);
                output.accept(CRUDE_TORCH_ITEM);
                output.accept(BURNING_CRUDE_TORCH_ITEM);
                output.accept(UNLIT_TORCH_ITEM);
                output.accept(GLOWSTONE_TORCH_ITEM);
            }).build());

    public Hcs(@NotNull FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        // Add listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        // Call sub-registers(facade)
        register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        // Config
        context.registerConfig(ModConfig.Type.COMMON, Config__.CFG_SPEC);
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        POTIONS.register(bus);
        BLOCK_ENTITIES.register(bus);
        ENTITIES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        CREATIVE_MODE_TABS.register(bus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            registerBrewing(Potions.AWKWARD, Items.IRON_NUGGET, IRONSKIN_POTION);
            registerBrewing(Potions.AWKWARD, Items.SALMON, RETURN_POTION);
            registerBrewing(Potions.AWKWARD, Items.MANGROVE_PROPAGULE, MINING_POTION);
            registerBrewing(Potions.AWKWARD, Items.QUARTZ, CONSTANT_TEMPERATURE_POTION);
            registerBrewing(Potions.AWKWARD, WILLOW_BARK, PAIN_KILLING_POTION);
            registerBrewing(Potions.AWKWARD, FEARLESSNESS_HERB, FEARLESSNESS_POTION);
            registerBrewing(IRONSKIN_POTION, Items.GLOWSTONE_DUST, STRONG_IRONSKIN_POTION);
            registerBrewing(IRONSKIN_POTION, Items.REDSTONE, LONG_IRONSKIN_POTION);
            registerBrewing(MINING_POTION, Items.GLOWSTONE_DUST, STRONG_MINING_POTION);
            registerBrewing(CONSTANT_TEMPERATURE_POTION, Items.REDSTONE, LONG_CONSTANT_TEMPERATURE_POTION);
            registerBrewing(PAIN_KILLING_POTION, Items.REDSTONE, LONG_PAIN_KILLING_POTION);
            registerBrewing(FEARLESSNESS_POTION, Items.REDSTONE, LONG_FEARLESSNESS_POTION);

            registerCompostables();
        });

// todo        String dummy = FOOD_SPOIL.gameRule.toString();
    }

    private void registerBrewing(Potion input, Item ingredient, Potion output) {
        BrewingRecipeRegistry.addRecipe(new BetterBrewingRecipe(input, ingredient, output));
    }

    private void registerCompostables() {
        ComposterBlock.add(0.3F, BERRY_BUSH);
        ComposterBlock.add(0.3F, ROASTED_SEEDS);
        ComposterBlock.add(0.3F, POTHERB);
        ComposterBlock.add(0.5F, PUMPKIN_SLICE);
        ComposterBlock.add(0.5F, COOKED_PUMPKIN_SLICE);
        ComposterBlock.add(0.5F, CACTUS_FLESH);
        ComposterBlock.add(0.5F, COOKED_CACTUS_FLESH);
        ComposterBlock.add(0.5F, BAMBOO_SHOOT);
        ComposterBlock.add(0.5F, COOKED_BAMBOO_SHOOT);
        ComposterBlock.add(0.5F, COOKED_CARROT);
        ComposterBlock.add(0.5F, COOKED_SWEET_BERRIES);
        ComposterBlock.add(0.65F, ORANGE);
        ComposterBlock.add(1.0F, ROT);
        ComposterBlock.add(0.5F, BARK);
        ComposterBlock.add(0.5F, WILLOW_BARK);
        ComposterBlock.add(0.5F, FEARLESSNESS_HERB);
    }

    @SubscribeEvent
    public void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        Item item = event.getItemStack().getItem();
        if (item == GRASS_FIBER) event.setBurnTime(50);
        else if (item == FIBER_STRING) event.setBurnTime(100);
        else if (item == SHORT_STICK) event.setBurnTime(80);
        else if (item == FIREWOOD) event.setBurnTime(300);
        else if (item == TINDER) event.setBurnTime(30);
        else if (item == ROT) event.setBurnTime(100);
        else if (item == BARK) event.setBurnTime(100);
        else if (item == WILLOW_BARK) event.setBurnTime(100);
        else if (item == BANDAGE) event.setBurnTime(80);
        else if (item == IMPROVISED_BANDAGE) event.setBurnTime(60);
        else if (item == SPLINT) event.setBurnTime(100);
        else if (item == SPIKED_CLUB) event.setBurnTime(160);
        else if (item == WOOLEN_HOOD || item == WOODEN_HELMET) event.setBurnTime(150);
        else if (item == WOOLEN_COAT || item == WOODEN_CHESTPLATE) event.setBurnTime(240);
        else if (item == WOOLEN_TROUSERS || item == WOODEN_LEGGINGS) event.setBurnTime(210);
        else if (item == WOOLEN_BOOTS || item == WOODEN_BOOTS) event.setBurnTime(120);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("village").executes(context -> {
            context.getSource().sendSuccess(() -> Component.translatable(WorldHelper.shouldGenerateVillages() ? "hcs.tip.can_gen_village" : "hcs.tip.cant_gen_village"), false);
            return 1;
        }));
    }

    private void addCreative(@NotNull BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }

    // Loggers
    public static void info(Object... contents) {
        LOGGER.info(getInfoString(contents));
    }

    public static void warn(Object... contents) {
        LOGGER.warn(getInfoString(contents));
    }

    public static void error(Object... contents) {
        LOGGER.error(getInfoString(contents));
    }

    private static @NotNull String getInfoString(@Nullable Object... contents) {
        if (contents == null) {
            LOGGER.warn("Printed content is null");
            return "";
        }
        StringBuilder outputBuilder = new StringBuilder();
        for (Object content : contents) {
            outputBuilder.append(content);
            outputBuilder.append(" ");
        }
        return outputBuilder.toString();
    }

    private record BetterBrewingRecipe(Potion input, Item ingredient, Potion output) implements IBrewingRecipe {

        @Override
        public boolean isInput(@NotNull ItemStack stack) {
            return PotionUtils.getPotion(stack) == input;
        }

        @Override
        public boolean isIngredient(@NotNull ItemStack stack) {
            return stack.getItem() == ingredient;
        }

        @Override
        public @NotNull ItemStack getOutput(@NotNull ItemStack input, @NotNull ItemStack ingredient) {
            if (!isInput(input) || !isIngredient(ingredient)) return ItemStack.EMPTY;
            ItemStack res = input.copy();
            PotionUtils.setPotion(res, output);
            return res;
        }
    }
}