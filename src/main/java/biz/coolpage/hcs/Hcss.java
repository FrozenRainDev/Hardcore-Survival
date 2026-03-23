package biz.coolpage.hcs;

import biz.coolpage.hcs.block.BurntCampfireBlock;
import biz.coolpage.hcs.block.DryingRackBlock;
import biz.coolpage.hcs.block.IceboxBlock;
import biz.coolpage.hcs.block.SmolderingCampfireBlock;
import biz.coolpage.hcs.block.torches.*;
import biz.coolpage.hcs.config.Config;
import biz.coolpage.hcs.entity.*;
import biz.coolpage.hcs.event.*;
import biz.coolpage.hcs.item.*;
import biz.coolpage.hcs.item.BottleItem;
import biz.coolpage.hcs.recipe.*;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.ServerC2S;
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
public final class Hcss {
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

    // --- Blocks ---
    public static final RegistryObject<IceboxBlock> _ICEBOX = BLOCKS.register("icebox", () -> new IceboxBlock(BlockBehaviour.Properties.copy(Blocks.COBBLESTONE).mapColor(MapColor.COLOR_LIGHT_GRAY).strength(2.0F, 3.0F).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistryObject<DryingRackBlock> _DRYING_RACK = BLOCKS.register("drying_rack", () -> new DryingRackBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE).mapColor(MapColor.WOOD).strength(1.5F, 2.0F).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistryObject<CrudeTorchBlock> _CRUDE_TORCH_BLOCK = BLOCKS.register("crude_torch", () -> new CrudeTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).noCollision().instabreak().lightLevel(state -> 0).sound(SoundType.WOOD)));
    public static final RegistryObject<WallCrudeTorchBlock> _WALL_CRUDE_TORCH_BLOCK = BLOCKS.register("wall_crude_torch", () -> new WallCrudeTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).noCollision().instabreak().lightLevel(state -> 0).sound(SoundType.WOOD).dropsLike(_CRUDE_TORCH_BLOCK.get())));
    public static final RegistryObject<BurningCrudeTorchBlock> _BURNING_CRUDE_TORCH_BLOCK = BLOCKS.register("burning_crude_torch", () -> new BurningCrudeTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).noCollision().instabreak().lightLevel(state -> 13).sound(SoundType.WOOD)));
    public static final RegistryObject<WallBurningCrudeTorchBlock> _WALL_BURNING_CRUDE_TORCH_BLOCK = BLOCKS.register("wall_burning_crude_torch", () -> new WallBurningCrudeTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).noCollision().instabreak().lightLevel(state -> 13).sound(SoundType.WOOD).dropsLike(_BURNING_CRUDE_TORCH_BLOCK.get())));
    public static final RegistryObject<UnlitTorchBlock> _UNLIT_TORCH_BLOCK = BLOCKS.register("unlit_torch", () -> new UnlitTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).lightLevel(state -> 0).noCollision().instabreak().sound(SoundType.WOOD)));
    public static final RegistryObject<WallUnlitTorchBlock> _WALL_UNLIT_TORCH_BLOCK = BLOCKS.register("wall_unlit_torch", () -> new WallUnlitTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).noCollision().instabreak().sound(SoundType.WOOD).dropsLike(_UNLIT_TORCH_BLOCK.get())));
    public static final RegistryObject<BurntTorchBlock> _BURNT_TORCH_BLOCK = BLOCKS.register("burnt_torch", () -> new BurntTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).lightLevel(state -> 0).noCollision().instabreak().sound(SoundType.WOOD)));
    public static final RegistryObject<WallBurntTorchBlock> _WALL_BURNT_TORCH_BLOCK = BLOCKS.register("wall_burnt_torch", () -> new WallBurntTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).lightLevel(state -> 0).noCollision().instabreak().sound(SoundType.WOOD).dropsLike(_BURNT_TORCH_BLOCK.get())));
    public static final RegistryObject<GlowstoneTorchBlock> _GLOWSTONE_TORCH_BLOCK = BLOCKS.register("glowstone_torch", () -> new GlowstoneTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).noCollision().instabreak().lightLevel(state -> 15).sound(SoundType.WOOD)));
    public static final RegistryObject<WallGlowstoneTorchBlock> _WALL_GLOWSTONE_TORCH_BLOCK = BLOCKS.register("wall_glowstone_torch", () -> new WallGlowstoneTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).noCollision().instabreak().lightLevel(state -> 14).sound(SoundType.WOOD).dropsLike(_GLOWSTONE_TORCH_BLOCK.get())));
    public static final RegistryObject<Block> _SMOLDERING_CAMPFIRE_BLOCK = BLOCKS.register("smoldering_campfire", SmolderingCampfireBlock::new);
    public static final RegistryObject<Block> _BURNT_CAMPFIRE_BLOCK = BLOCKS.register("burnt_campfire", BurntCampfireBlock::new);

    public static final Block
            ICEBOX = _ICEBOX.get(),
            DRYING_RACK = _DRYING_RACK.get(),
            CRUDE_TORCH_BLOCK = _CRUDE_TORCH_BLOCK.get(),
            WALL_CRUDE_TORCH_BLOCK = _WALL_CRUDE_TORCH_BLOCK.get(),
            BURNING_CRUDE_TORCH_BLOCK = _BURNING_CRUDE_TORCH_BLOCK.get(),
            WALL_BURNING_CRUDE_TORCH_BLOCK = _WALL_BURNING_CRUDE_TORCH_BLOCK.get(),
            UNLIT_TORCH_BLOCK = _UNLIT_TORCH_BLOCK.get(),
            WALL_UNLIT_TORCH_BLOCK = _WALL_UNLIT_TORCH_BLOCK.get(),
            BURNT_TORCH_BLOCK = _BURNT_TORCH_BLOCK.get(),
            WALL_BURNT_TORCH_BLOCK = _WALL_BURNT_TORCH_BLOCK.get(),
            GLOWSTONE_TORCH_BLOCK = _GLOWSTONE_TORCH_BLOCK.get(),
            WALL_GLOWSTONE_TORCH_BLOCK = _WALL_GLOWSTONE_TORCH_BLOCK.get(),
            SMOLDERING_CAMPFIRE_BLOCK = _SMOLDERING_CAMPFIRE_BLOCK.get(),
            BURNT_CAMPFIRE_BLOCK = _BURNT_CAMPFIRE_BLOCK.get();

    // --- Items ---
    public static final RegistryObject<Item>
            _FIBER_STRING = ITEMS.register("fiber_string", () -> new Item(new Item.Properties())),
            _GRASS_FIBER = ITEMS.register("grass_fiber", () -> new Item(new Item.Properties())),
            _ROASTED_SEEDS = ITEMS.register("roasted_seeds", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0f).fast().build()))),
            _ROCK = ITEMS.register("rock", () -> new RockItem(new Item.Properties())),
            _SHARP_ROCK = ITEMS.register("sharp_rock", () -> new Item(new Item.Properties())),
            _SHARP_FLINT = ITEMS.register("sharp_flint", () -> new Item(new Item.Properties())),
            _FIREWOOD = ITEMS.register("firewood", () -> new Item(new Item.Properties())),
            _FRIED_EGG = ITEMS.register("fried_egg", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationMod(2f).build()))),
            _EXTINGUISHED_CAMPFIRE = ITEMS.register("extinguished_campfire", () -> new HCSCampfireItem(Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, false).setValue(CombustionHelper.COMBUST_LUMINANCE, 15))),
            _FIRE_BOW = ITEMS.register("fire_bow", () -> new FireBowItem(new Item.Properties().stacksTo(1).durability(96), 1)),
            _FIRE_PLOUGH = ITEMS.register("fire_plough", () -> new FireBowItem(new Item.Properties().stacksTo(1).durability(64), 3)),
            _TINDER = ITEMS.register("tinder", () -> new Item(new Item.Properties())),
            _WORM = ITEMS.register("worm", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200), 1).build()), 0.0F, -0.08)),
            _PUMPKIN_SLICE = ITEMS.register("pumpkin_slice", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(0f).build()))),
            _POTHERB = ITEMS.register("potherb", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(1f).build()))),
            _STONE_KNIFE = ITEMS.register("stone_knife", () -> new KnifeItem(HcsTiers.STONE_WEAPON, 1, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            _STONE_SPEAR = ITEMS.register("stone_spear", () -> new SwordItem(HcsTiers.STONE_WEAPON, 2, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            _STONE_CONE = ITEMS.register("stone_cone", () -> new ShovelItem(HcsTiers.STONE_CONE, 1.0F, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            _FLINT_KNIFE = ITEMS.register("flint_knife", () -> new KnifeItem(HcsTiers.FLINT_WEAPON, 2, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            _FLINT_SPEAR = ITEMS.register("flint_spear", () -> new SwordItem(HcsTiers.FLINT_WEAPON, 3, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            _FLINT_CONE = ITEMS.register("flint_cone", () -> new ShovelItem(HcsTiers.FLINT_CONE, 1.5F, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            _FLINT_HATCHET = ITEMS.register("flint_hatchet", () -> new AxeItem(HcsTiers.FLINT_HATCHET, 6.0F, -3.1F, new Item.Properties())),
            _RAW_COPPER_POWDER = ITEMS.register("raw_copper_powder", () -> new Item(new Item.Properties())),
            _COPPER_SWORD = ITEMS.register("copper_sword", () -> new SwordItem(HcsTiers.COPPER, 5, 1.4F - 4.0F, new Item.Properties().stacksTo(1))),
            _COPPER_AXE = ITEMS.register("copper_axe", () -> new AxeItem(HcsTiers.COPPER, 7, 0.8F - 4.0F, new Item.Properties().stacksTo(1))),
            _COPPER_PICKAXE = ITEMS.register("copper_pickaxe", () -> new PickaxeItem(HcsTiers.COPPER, 3, 1.1F - 4.0F, new Item.Properties().stacksTo(1))),
            _COPPER_HOE = ITEMS.register("copper_hoe", () -> new HoeItem(HcsTiers.COPPER, 0, 2.5F - 4.0F, new Item.Properties().stacksTo(1))),
            _COPPER_SHOVEL = ITEMS.register("copper_shovel", () -> new ShovelItem(HcsTiers.COPPER, 3, 1.0F - 4.0F, new Item.Properties().stacksTo(1))),
            _COPPER_HELMET = ITEMS.register("copper_helmet", () -> new ArmorItem(HcsArmorMaterials.COPPER, ArmorItem.Type.HELMET, new Item.Properties())),
            _COPPER_CHESTPLATE = ITEMS.register("copper_chestplate", () -> new ArmorItem(HcsArmorMaterials.COPPER, ArmorItem.Type.CHESTPLATE, new Item.Properties())),
            _COPPER_LEGGINGS = ITEMS.register("copper_leggings", () -> new ArmorItem(HcsArmorMaterials.COPPER, ArmorItem.Type.LEGGINGS, new Item.Properties())),
            _COPPER_BOOTS = ITEMS.register("copper_boots", () -> new ArmorItem(HcsArmorMaterials.COPPER, ArmorItem.Type.BOOTS, new Item.Properties())),
            _SPIDER_GLAND = ITEMS.register("spider_gland", () -> new SalveItem(8, 0.5)),
            _SELAGINELLA = ITEMS.register("selaginella", () -> new SalveItem(20, 1.5) {
                @Override
                public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity user) {
                    if (user instanceof ServerPlayer player) {
                        ((StatAccessor) player).getStatusManager().setSoulImpairedStat(0);
                        ((StatAccessor) player).getInjuryManager().applyPainkiller();
                    }
                    return super.finishUsingItem(stack, world, user);
                }
            }),
            _RAW_MEAT = ITEMS.register("raw_meat", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(1f).meat().build()))),
            _COOKED_MEAT = ITEMS.register("cooked_meat", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationMod(3f).meat().build()))),
            _CACTUS_FLESH = ITEMS.register("cactus_flesh", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1f).effect(() -> new MobEffectInstance(MobEffects.POISON, 160), 1).effect(() -> new MobEffectInstance(HcsEffects.DIARRHEA, 300), 1).build()))),
            _COOKED_CACTUS_FLESH = ITEMS.register("cooked_cactus_flesh", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(2f).effect(() -> new MobEffectInstance(MobEffects.POISON, 80), 1).build()))),
            _CACTUS_JUICE = ITEMS.register("cactus_juice", () -> new BottleItem(new Item.Properties().stacksTo(16), new MobEffectInstance(MobEffects.POISON, 160))),
            _PURIFIED_WATER_BOTTLE = ITEMS.register("purified_water_bottle", () -> new BottleItem(new Item.Properties().stacksTo(16))),
            _SALTWATER_BOTTLE = ITEMS.register("saltwater_bottle", () -> new BottleItem(new Item.Properties().stacksTo(16), new MobEffectInstance(HcsEffects.THIRST, 1200, 0, false, false, true))),
            _ROASTED_WORM = ITEMS.register("roasted_worm", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(1.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 60), 1).build()), 0.0F, -0.01)),
            _ANIMAL_VISCERA = ITEMS.register("animal_viscera", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationMod(2.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 40), 1).meat().build()))),
            _COOKED_ANIMAL_VISCERA = ITEMS.register("cooked_animal_viscera", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationMod(4F).meat().build()))),
            _SHARP_BROKEN_BONE = ITEMS.register("sharp_broken_bone", () -> new ShovelItem(HcsTiers.SHARP_BROKEN_BONE, 3F, 1.6F - 4.0F, new Item.Properties().stacksTo(1))),
            _BAMBOO_SHOOT = ITEMS.register("bamboo_shoot", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1f).build()))),
            _COOKED_BAMBOO_SHOOT = ITEMS.register("cooked_bamboo_shoot", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(1f).build()))),
            _COOKED_CARROT = ITEMS.register("cooked_carrot", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationMod(2f).build()))),
            _COOKED_PUMPKIN_SLICE = ITEMS.register("cooked_pumpkin_slice", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationMod(0f).build()))),
            _COOKED_SWEET_BERRIES = ITEMS.register("cooked_sweet_berries", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1f).build()))),
            _BERRY_BUSH = ITEMS.register("berry_bush", () -> new Item(new Item.Properties())),
            _PETALS_SALAD = ITEMS.register("petals_salad", () -> new BowlOfFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0f).build()))),
            _ORANGE = ITEMS.register("orange", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationMod(3f).build()))),
            _ROT = ITEMS.register("rot", () -> new BoneMealItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0.0f).build())) {
                @Override
                public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
                    if (context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.GRASS_BLOCK))
                        return InteractionResult.PASS;
                    return super.useOn(context);
                }
            }),
            _ICEBOX_ITEM = ITEMS.register("icebox", () -> new BlockItem(_ICEBOX.get(), new Item.Properties())),
            _DRYING_RACK_ITEM = ITEMS.register("drying_rack", () -> new BlockItem(_DRYING_RACK.get(), new Item.Properties())),
            _SHORT_STICK = ITEMS.register("short_stick", () -> new Item(new Item.Properties())),
            _JERKY = ITEMS.register("jerky", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationMod(8.0F).meat().build()), 3.0F, 0.15)),
            _SMALL_JERKY = ITEMS.register("small_jerky", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationMod(5.0F).meat().build()), 1.5F, 0.08)),
            _RAW_JERKY = ITEMS.register("raw_jerky", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationMod(4.0F).meat().build()), 1.5F, 0.0)),
            _RAW_SMALL_JERKY = ITEMS.register("raw_small_jerky", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationMod(3.0F).meat().build()), 1.0F, 0.0)),
            _SPIKED_CLUB = ITEMS.register("spiked_club", () -> new SwordItem(Tiers.WOOD, 4, -2.4f, new Item.Properties())),
            _COLD_WATER_BOTTLE = ITEMS.register("cold_water_bottle", () -> new BottleItem(new Item.Properties().stacksTo(16)) {
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
            _HOT_WATER_BOTTLE = ITEMS.register("hot_water_bottle", HotWaterBottleItem::new),
            _WOOLEN_HOOD = ITEMS.register("woolen_hood", () -> new ArmorItem(HcsArmorMaterials.WOOL, ArmorItem.Type.HELMET, new Item.Properties())),
            _WOOLEN_COAT = ITEMS.register("woolen_coat", () -> new ArmorItem(HcsArmorMaterials.WOOL, ArmorItem.Type.CHESTPLATE, new Item.Properties())),
            _WOOLEN_TROUSERS = ITEMS.register("woolen_trousers", () -> new ArmorItem(HcsArmorMaterials.WOOL, ArmorItem.Type.LEGGINGS, new Item.Properties())),
            _WOOLEN_BOOTS = ITEMS.register("woolen_boots", () -> new ArmorItem(HcsArmorMaterials.WOOL, ArmorItem.Type.BOOTS, new Item.Properties())),
            _COOKED_KELP = ITEMS.register("cooked_kelp", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0F).build()))),
            _BARK = ITEMS.register("bark", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0.0F).build()))),
            _WILLOW_BARK = ITEMS.register("willow_bark", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0.0F).build()))),
            _FEARLESSNESS_HERB = ITEMS.register("fearlessness_herb", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(0).saturationMod(0.0F).build()))),
            _BANDAGE = ITEMS.register("bandage", () -> new BandageItem(2.0, 40, 200)),
            _IMPROVISED_BANDAGE = ITEMS.register("improvised_bandage", () -> new BandageItem(0.8, 60, 120)),
            _SPLINT = ITEMS.register("splint", () -> new BandageItem(0.5, 140) {
                @Override
                public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity user) {
                    if (user instanceof ServerPlayer player)
                        ((StatAccessor) player).getInjuryManager().setFracture(0.0);
                    return super.finishUsingItem(stack, world, user);
                }
            }),
            _GINGER = ITEMS.register("ginger", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0F).build()))),
            _WOODEN_HELMET = ITEMS.register("wooden_helmet", () -> new ArmorItem(HcsArmorMaterials.WOOD, ArmorItem.Type.HELMET, new Item.Properties())),
            _WOODEN_CHESTPLATE = ITEMS.register("wooden_chestplate", () -> new ArmorItem(HcsArmorMaterials.WOOD, ArmorItem.Type.CHESTPLATE, new Item.Properties())),
            _WOODEN_LEGGINGS = ITEMS.register("wooden_leggings", () -> new ArmorItem(HcsArmorMaterials.WOOD, ArmorItem.Type.LEGGINGS, new Item.Properties())),
            _WOODEN_BOOTS = ITEMS.register("wooden_boots", () -> new ArmorItem(HcsArmorMaterials.WOOD, ArmorItem.Type.BOOTS, new Item.Properties())),
            _IMPROVISED_SHIELD = ITEMS.register("improvised_shield", () -> new ShieldItem(new Item.Properties().durability(48)) {
                @Contract(pure = true)
                @Override
                public @NotNull String getDescriptionId(@NotNull ItemStack stack) {
                    return "item.hcs.improvised_shield";
                }
            }),
            _BAT_WINGS = ITEMS.register("bat_wings", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200), 1).effect(() -> new MobEffectInstance(HcsEffects.DIARRHEA, 200), 0.7F).build()), 0.0F, -0.06)),
            _ROASTED_BAT_WINGS = ITEMS.register("roasted_bat_wings", () -> new EffectiveFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(1).saturationMod(1.0f).effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 60), 1).build()), 0.0F, -0.01)),
            _HEALING_SALVE = ITEMS.register("healing_salve", () -> new SalveItem(14, 1.5, 20) {
                @Override
                public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity user) {
                    if (user instanceof ServerPlayer player) EntityHelper.dropItem(player, Items.BOWL);
                    return super.finishUsingItem(stack, world, user);
                }
            }),
            _ASHES = ITEMS.register("ashes", () -> new SalveItem(0, 0.2, 50)),
            _CRUDE_TORCH_ITEM = ITEMS.register("crude_torch", () -> new StandingAndWallBlockItem(_CRUDE_TORCH_BLOCK.get(), _WALL_CRUDE_TORCH_BLOCK.get(), new Item.Properties(), Direction.DOWN) {
                @Override
                public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
                    InteractionResult performed = CombustionHelper.preLitHoldingTorch(context);
                    return performed == null ? super.useOn(context) : performed;
                }
            }),
            _BURNING_CRUDE_TORCH_ITEM = ITEMS.register("burning_crude_torch", BurningCrudeTorchItem::new),
            _UNLIT_TORCH_ITEM = ITEMS.register("unlit_torch", () -> new StandingAndWallBlockItem(_UNLIT_TORCH_BLOCK.get(), _WALL_UNLIT_TORCH_BLOCK.get(), new Item.Properties(), Direction.DOWN) {
                @Override
                public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
                    InteractionResult performed = CombustionHelper.preLitHoldingTorch(context);
                    return performed == null ? super.useOn(context) : performed;
                }
            }),
            _GLOWSTONE_TORCH_ITEM = ITEMS.register("glowstone_torch", () -> new StandingAndWallBlockItem(_GLOWSTONE_TORCH_BLOCK.get(), _WALL_GLOWSTONE_TORCH_BLOCK.get(), new Item.Properties(), Direction.DOWN)),
            _BOOSTER_SHOT = ITEMS.register("booster_shot", BoosterShotItem::new),
            _SMOLDERING_CAMPFIRE = ITEMS.register("smoldering_campfire", () -> new HCSCampfireItem(_SMOLDERING_CAMPFIRE_BLOCK.get().defaultBlockState())),
            _BURNT_CAMPFIRE = ITEMS.register("burnt_campfire", () -> new HCSCampfireItem(_BURNT_CAMPFIRE_BLOCK.get().defaultBlockState())),
            _GARLAND = ITEMS.register("garland", () -> new ArmorItem(HcsArmorMaterials.GARLAND, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final Item
            FIBER_STRING = _FIBER_STRING.get(),
            GRASS_FIBER = _GRASS_FIBER.get(),
            ROASTED_SEEDS = _ROASTED_SEEDS.get(),
            ROCK = _ROCK.get(),
            SHARP_ROCK = _SHARP_ROCK.get(),
            SHARP_FLINT = _SHARP_FLINT.get(),
            FIREWOOD = _FIREWOOD.get(),
            FRIED_EGG = _FRIED_EGG.get(),
            EXTINGUISHED_CAMPFIRE = _EXTINGUISHED_CAMPFIRE.get(),
            FIRE_BOW = _FIRE_BOW.get(),
            FIRE_PLOUGH = _FIRE_PLOUGH.get(),
            TINDER = _TINDER.get(),
            WORM = _WORM.get(),
            PUMPKIN_SLICE = _PUMPKIN_SLICE.get(),
            POTHERB = _POTHERB.get(),
            STONE_KNIFE = _STONE_KNIFE.get(),
            STONE_SPEAR = _STONE_SPEAR.get(),
            STONE_CONE = _STONE_CONE.get(),
            FLINT_KNIFE = _FLINT_KNIFE.get(),
            FLINT_SPEAR = _FLINT_SPEAR.get(),
            FLINT_CONE = _FLINT_CONE.get(),
            FLINT_HATCHET = _FLINT_HATCHET.get(),
            RAW_COPPER_POWDER = _RAW_COPPER_POWDER.get(),
            COPPER_SWORD = _COPPER_SWORD.get(),
            COPPER_AXE = _COPPER_AXE.get(),
            COPPER_PICKAXE = _COPPER_PICKAXE.get(),
            COPPER_HOE = _COPPER_HOE.get(),
            COPPER_SHOVEL = _COPPER_SHOVEL.get(),
            COPPER_HELMET = _COPPER_HELMET.get(),
            COPPER_CHESTPLATE = _COPPER_CHESTPLATE.get(),
            COPPER_LEGGINGS = _COPPER_LEGGINGS.get(),
            COPPER_BOOTS = _COPPER_BOOTS.get(),
            SPIDER_GLAND = _SPIDER_GLAND.get(),
            SELAGINELLA = _SELAGINELLA.get(),
            RAW_MEAT = _RAW_MEAT.get(),
            COOKED_MEAT = _COOKED_MEAT.get(),
            CACTUS_FLESH = _CACTUS_FLESH.get(),
            COOKED_CACTUS_FLESH = _COOKED_CACTUS_FLESH.get(),
            CACTUS_JUICE = _CACTUS_JUICE.get(),
            PURIFIED_WATER_BOTTLE = _PURIFIED_WATER_BOTTLE.get(),
            SALTWATER_BOTTLE = _SALTWATER_BOTTLE.get(),
            ROASTED_WORM = _ROASTED_WORM.get(),
            ANIMAL_VISCERA = _ANIMAL_VISCERA.get(),
            COOKED_ANIMAL_VISCERA = _COOKED_ANIMAL_VISCERA.get(),
            SHARP_BROKEN_BONE = _SHARP_BROKEN_BONE.get(),
            BAMBOO_SHOOT = _BAMBOO_SHOOT.get(),
            COOKED_BAMBOO_SHOOT = _COOKED_BAMBOO_SHOOT.get(),
            COOKED_CARROT = _COOKED_CARROT.get(),
            COOKED_PUMPKIN_SLICE = _COOKED_PUMPKIN_SLICE.get(),
            COOKED_SWEET_BERRIES = _COOKED_SWEET_BERRIES.get(),
            BERRY_BUSH = _BERRY_BUSH.get(),
            PETALS_SALAD = _PETALS_SALAD.get(),
            ORANGE = _ORANGE.get(),
            ROT = _ROT.get(),
            ICEBOX_ITEM = _ICEBOX_ITEM.get(),
            DRYING_RACK_ITEM = _DRYING_RACK_ITEM.get(),
            SHORT_STICK = _SHORT_STICK.get(),
            JERKY = _JERKY.get(),
            SMALL_JERKY = _SMALL_JERKY.get(),
            RAW_JERKY = _RAW_JERKY.get(),
            RAW_SMALL_JERKY = _RAW_SMALL_JERKY.get(),
            SPIKED_CLUB = _SPIKED_CLUB.get(),
            COLD_WATER_BOTTLE = _COLD_WATER_BOTTLE.get(),
            HOT_WATER_BOTTLE = _HOT_WATER_BOTTLE.get(),
            WOOLEN_HOOD = _WOOLEN_HOOD.get(),
            WOOLEN_COAT = _WOOLEN_COAT.get(),
            WOOLEN_TROUSERS = _WOOLEN_TROUSERS.get(),
            WOOLEN_BOOTS = _WOOLEN_BOOTS.get(),
            COOKED_KELP = _COOKED_KELP.get(),
            BARK = _BARK.get(),
            WILLOW_BARK = _WILLOW_BARK.get(),
            FEARLESSNESS_HERB = _FEARLESSNESS_HERB.get(),
            BANDAGE = _BANDAGE.get(),
            IMPROVISED_BANDAGE = _IMPROVISED_BANDAGE.get(),
            SPLINT = _SPLINT.get(),
            GINGER = _GINGER.get(),
            WOODEN_HELMET = _WOODEN_HELMET.get(),
            WOODEN_CHESTPLATE = _WOODEN_CHESTPLATE.get(),
            WOODEN_LEGGINGS = _WOODEN_LEGGINGS.get(),
            WOODEN_BOOTS = _WOODEN_BOOTS.get(),
            IMPROVISED_SHIELD = _IMPROVISED_SHIELD.get(),
            BAT_WINGS = _BAT_WINGS.get(),
            ROASTED_BAT_WINGS = _ROASTED_BAT_WINGS.get(),
            HEALING_SALVE = _HEALING_SALVE.get(),
            ASHES = _ASHES.get(),
            CRUDE_TORCH_ITEM = _CRUDE_TORCH_ITEM.get(),
            BURNING_CRUDE_TORCH_ITEM = _BURNING_CRUDE_TORCH_ITEM.get(),
            UNLIT_TORCH_ITEM = _UNLIT_TORCH_ITEM.get(),
            GLOWSTONE_TORCH_ITEM = _GLOWSTONE_TORCH_ITEM.get(),
            BOOSTER_SHOT = _BOOSTER_SHOT.get(),
            SMOLDERING_CAMPFIRE = _SMOLDERING_CAMPFIRE.get(),
            BURNT_CAMPFIRE = _BURNT_CAMPFIRE.get(),
            GARLAND = _GARLAND.get();

    // --- Potions ---
    public static final RegistryObject<Potion>
            _IRONSKIN_POTION = POTIONS.register("hcs_ironskin", () -> new Potion(new MobEffectInstance(HcsEffects.IRONSKIN, 3600, 0))),
            _LONG_IRONSKIN_POTION = POTIONS.register("hcs_long_ironskin", () -> new Potion(new MobEffectInstance(HcsEffects.IRONSKIN, 9600, 0))),
            _STRONG_IRONSKIN_POTION = POTIONS.register("hcs_strong_ironskin", () -> new Potion(new MobEffectInstance(HcsEffects.IRONSKIN, 1800, 1))),
            _RETURN_POTION = POTIONS.register("hcs_return", () -> new Potion(new MobEffectInstance(HcsEffects.RETURN, 120, 0, false, true, false))),
            _MINING_POTION = POTIONS.register("hcs_mining", () -> new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 3600))),
            _LONG_MINING_POTION = POTIONS.register("hcs_long_mining", () -> new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 9600))),
            _STRONG_MINING_POTION = POTIONS.register("hcs_strong_mining", () -> new Potion(new MobEffectInstance(MobEffects.DIG_SPEED, 1800, 1))),
            _CONSTANT_TEMPERATURE_POTION = POTIONS.register("hcs_constant_temperature", () -> new Potion(new MobEffectInstance(HcsEffects.CONSTANT_TEMPERATURE, 3600))),
            _LONG_CONSTANT_TEMPERATURE_POTION = POTIONS.register("hcs_long_constant_temperature", () -> new Potion(new MobEffectInstance(HcsEffects.CONSTANT_TEMPERATURE, 9600))),
            _PAIN_KILLING_POTION = POTIONS.register("hcs_pain_killing", () -> new Potion(new MobEffectInstance(HcsEffects.PAIN_KILLING, 3600))),
            _LONG_PAIN_KILLING_POTION = POTIONS.register("hcs_long_pain_killing", () -> new Potion(new MobEffectInstance(HcsEffects.PAIN_KILLING, 9600))),
            _FEARLESSNESS_POTION = POTIONS.register("hcs_fearlessness", () -> new Potion(new MobEffectInstance(HcsEffects.FEARLESSNESS, 3600))),
            _LONG_FEARLESSNESS_POTION = POTIONS.register("hcs_long_fearlessness", () -> new Potion(new MobEffectInstance(HcsEffects.FEARLESSNESS, 9600)));

    public static final Potion
            IRONSKIN_POTION = _IRONSKIN_POTION.get(),
            LONG_IRONSKIN_POTION = _LONG_IRONSKIN_POTION.get(),
            STRONG_IRONSKIN_POTION = _STRONG_IRONSKIN_POTION.get(),
            RETURN_POTION = _RETURN_POTION.get(),
            MINING_POTION = _MINING_POTION.get(),
            LONG_MINING_POTION = _LONG_MINING_POTION.get(),
            STRONG_MINING_POTION = _STRONG_MINING_POTION.get(),
            CONSTANT_TEMPERATURE_POTION = _CONSTANT_TEMPERATURE_POTION.get(),
            LONG_CONSTANT_TEMPERATURE_POTION = _LONG_CONSTANT_TEMPERATURE_POTION.get(),
            PAIN_KILLING_POTION = _PAIN_KILLING_POTION.get(),
            LONG_PAIN_KILLING_POTION = _LONG_PAIN_KILLING_POTION.get(),
            FEARLESSNESS_POTION = _FEARLESSNESS_POTION.get(),
            LONG_FEARLESSNESS_POTION = _LONG_FEARLESSNESS_POTION.get();

    // --- Entity Types ---
    public static final RegistryObject<EntityType<RockProjectileEntity>> _ROCK_PROJECTILE_ENTITY = ENTITIES.register("rock_projectile_entity", () -> EntityType.Builder.<RockProjectileEntity>of(RockProjectileEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).build("rock_projectile_entity"));
    public static final RegistryObject<EntityType<FlintProjectileEntity>> _FLINT_PROJECTILE_ENTITY = ENTITIES.register("flint_projectile_entity", () -> EntityType.Builder.<FlintProjectileEntity>of(FlintProjectileEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).build("flint_projectile_entity"));

    public static final EntityType<RockProjectileEntity> ROCK_PROJECTILE_ENTITY = _ROCK_PROJECTILE_ENTITY.get();
    public static final EntityType<FlintProjectileEntity> FLINT_PROJECTILE_ENTITY = _FLINT_PROJECTILE_ENTITY.get();

    // --- Block Entities ---
    public static final RegistryObject<BlockEntityType<IceboxBlockEntity>> _ICEBOX_BLOCK_ENTITY = BLOCK_ENTITIES.register("icebox_block_entity", () -> BlockEntityType.Builder.of(IceboxBlockEntity::new, _ICEBOX.get()).build(null));
    public static final RegistryObject<BlockEntityType<DryingRackBlockEntity>> _DRYING_RACK_BLOCK_ENTITY = BLOCK_ENTITIES.register("drying_rack_block_entity", () -> BlockEntityType.Builder.of(DryingRackBlockEntity::new, _DRYING_RACK.get()).build(null));
    public static final RegistryObject<BlockEntityType<BurningCrudeTorchBlockEntity>> _BURNING_CRUDE_TORCH_BLOCK_ENTITY = BLOCK_ENTITIES.register("burning_crude_torch", () -> BlockEntityType.Builder.of(BurningCrudeTorchBlockEntity::new, _BURNING_CRUDE_TORCH_BLOCK.get(), _WALL_BURNING_CRUDE_TORCH_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<SmolderingOrBurntCampfireBlockEntity>> _SMOLDERING_OR_BURNT_CAMPFIRE_BLOCK_ENTITY_BLOCK_ENTITY_TYPE = BLOCK_ENTITIES.register("smoldering_or_burnt_campfire", () -> BlockEntityType.Builder.of(SmolderingOrBurntCampfireBlockEntity::new, _SMOLDERING_CAMPFIRE_BLOCK.get(), _BURNT_CAMPFIRE_BLOCK.get()).build(null));

    // --- Recipe Serializers ---
    public static final RegistryObject<RecipeSerializer<?>>
            _EXTRACT_WATER_FROM_BAMBOO_RECIPE = RECIPE_SERIALIZERS.register("hcs_extract_water_from_bamboo", () -> new SimpleCraftingRecipeSerializer<>(ExtractWaterFromBambooRecipe::new)),
            _EXTRACT_WATER_FROM_SNOW_RECIPE = RECIPE_SERIALIZERS.register("hcs_extract_water_from_snow", () -> new SimpleCraftingRecipeSerializer<>(ExtractWaterFromSnowRecipe::new)),
            _PETALS_SALAD_RECIPE = RECIPE_SERIALIZERS.register("hcs_petals_salad", () -> new SimpleCraftingRecipeSerializer<>(PetalsSaladRecipe::new)),
            _SPIKED_CLUB_RECIPE = RECIPE_SERIALIZERS.register("hcs_spiked_club_recipe", () -> new SimpleCraftingRecipeSerializer<>(SpikedClubRecipe::new)),
            _COLD_WATER_BOTTLE_RECIPE = RECIPE_SERIALIZERS.register("hcs_cold_water_bottle_recipe", () -> new SimpleCraftingRecipeSerializer<>(ColdWaterBottleRecipe::new)),
            _HOT_WATER_BOTTLE_RECIPE = RECIPE_SERIALIZERS.register("hcs_hot_water_bottle_recipe", () -> new SimpleCraftingRecipeSerializer<>(HotWaterBottleRecipe::new)),
            _SAPLING_TO_STICK_RECIPE = RECIPE_SERIALIZERS.register("hcs_sapling_to_stick_recipe", () -> new SimpleCraftingRecipeSerializer<>(SaplingToStickRecipe::new)),
            _POUR_OUT_CONTENT_RECIPE = RECIPE_SERIALIZERS.register("hcs_pour_out_content_recipe", () -> new SimpleCraftingRecipeSerializer<>(PourOutContentRecipe::new)),
            _TORCH_IGNITE_RECIPE = RECIPE_SERIALIZERS.register("torch_ignite", () -> new SimpleCraftingRecipeSerializer<>(TorchIgniteRecipe::new));

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
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        context.registerConfig(ModConfig.Type.COMMON, Config.CFG_SPEC);
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

        ServerC2S.init();
        AttackBlockEvent.init();
        AttackEntityEventListener.init();
        BreakBlockEvent.init();
        EntitySleepEvent.init();
        ServerEntityEvent.init();
        ServerPlayerEvent.init();
        UseBlockEvent.init();
        String dummy = FOOD_SPOIL.gameRule.toString();
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
        if (contents == null) return "";
        StringBuilder sb = new StringBuilder();
        for (Object c : contents) sb.append(c).append(" ");
        return sb.toString();
    }

    private static class BetterBrewingRecipe implements IBrewingRecipe {
        private final Potion input, output;
        private final Item ingredient;

        public BetterBrewingRecipe(Potion input, Item ingredient, Potion output) {
            this.input = input;
            this.ingredient = ingredient;
            this.output = output;
        }

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