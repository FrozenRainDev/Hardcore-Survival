package biz.coolpage.hcs.client.gui;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.config.Configs;
import biz.coolpage.hcs.config.HcsDifficulty;
import biz.coolpage.hcs.status.HcsEffects;
import biz.coolpage.hcs.status.accessor.StatAccessor;
import biz.coolpage.hcs.status.manager.SanityManager;
import biz.coolpage.hcs.status.manager.StatusManager;
import biz.coolpage.hcs.status.manager.TemperatureManager;
import biz.coolpage.hcs.util.ArmorHelper;
import biz.coolpage.hcs.util.EntityHelper;
import biz.coolpage.hcs.util.HcsFactory;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static biz.coolpage.hcs.config.Configs.*;
import static biz.coolpage.hcs.util.CommUtil.applyNullable;
import static biz.coolpage.hcs.util.CommUtil.numFormat;

public final class HcsGuiOverlays {
    public static final HcsGuiOverlays INSTANCE = new HcsGuiOverlays();

    public static final IGuiOverlay HCS_SCREEN_EFFECTS = INSTANCE::renderScreenEffects;

    public static final IGuiOverlay HCS_STATUS_BARS = INSTANCE::renderStatusBars;

    private float heaLast = 0.0F;
    // For Status Icons Automatic Centering
    private final Map<String, Boolean> renderEntries = new HashMap<>();
    private int heaTwinkleCoolDown = 0, sanTwinkleCoolDown = 0;

    private ItemStack lastToolHighlight = ItemStack.EMPTY;
    private int toolHighlightTimer = 0;

    private static final ResourceLocation HCS_ICONS_TEXTURE = HcsFactory.createResourceLocation("textures/gui/hcs_stat.png");
    private static final ResourceLocation HEATSTROKE_BLUR = HcsFactory.createResourceLocation("textures/misc/heatstroke_blur.png");
    private static final ResourceLocation INSANITY_OUTLINE = HcsFactory.createResourceLocation("textures/misc/insanity_outline.png");
    private static final ResourceLocation DARKNESS = HcsFactory.createResourceLocation("textures/misc/darkness.png");
    private static final ResourceLocation DARKNESS_JUMP_SCARE = HcsFactory.createResourceLocation("textures/misc/darkness_jump_scare.png");
    private static final ResourceLocation HURT_BLUR = HcsFactory.createResourceLocation("textures/misc/hurt_blur.png");
    private static final ResourceLocation FAINT_BLUR = HcsFactory.createResourceLocation("textures/misc/panic_blur.png");

    private HcsGuiOverlays() {
    }

    public static void registerGuiOverlays(@NotNull RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.FROSTBITE.id(), "hcs_screen_effects", HCS_SCREEN_EFFECTS);
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "hcs_status_bars", HCS_STATUS_BARS);
    }

    public void onRenderGuiOverlayPre(RenderGuiOverlayEvent.@NotNull Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !EntityHelper.IS_SURVIVAL_LIKE.test(minecraft.player)) return;

        ResourceLocation id = event.getOverlay().id();

        if (id.equals(VanillaGuiOverlay.PLAYER_HEALTH.id())
                || id.equals(VanillaGuiOverlay.ARMOR_LEVEL.id())
                || id.equals(VanillaGuiOverlay.FOOD_LEVEL.id())
                || id.equals(VanillaGuiOverlay.AIR_LEVEL.id())
                || id.equals(VanillaGuiOverlay.MOUNT_HEALTH.id())
                || id.equals(VanillaGuiOverlay.JUMP_BAR.id())
                || id.equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) {
            event.setCanceled(true);
            return;
        }

        if (id.equals(VanillaGuiOverlay.ITEM_NAME.id())) {
            event.setCanceled(true);
            if (minecraft.gui instanceof ForgeGui forgeGui) {
                this.renderSelectedItemName(forgeGui, event.getGuiGraphics());
            }
        }
    }

    public void onClientTick(TickEvent.@NotNull ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            this.lastToolHighlight = ItemStack.EMPTY;
            this.toolHighlightTimer = 0;
            return;
        }

        ItemStack selected = minecraft.player.getInventory().getSelected();
        if (selected.isEmpty()) {
            this.lastToolHighlight = ItemStack.EMPTY;
            this.toolHighlightTimer = 0;
            return;
        }

        if (ItemStack.isSameItemSameTags(this.lastToolHighlight, selected)) {
            if (this.toolHighlightTimer > 0) --this.toolHighlightTimer;
        } else {
            this.lastToolHighlight = selected.copy();
            this.toolHighlightTimer = 40;
        }
    }

    public void drawHCSTexture(@NotNull GuiGraphics ctx, int x, int y, int u, int v, int width, int height) {
        // (u, v) is the coordinate of texture
        RenderSystem.setShaderTexture(0, HCS_ICONS_TEXTURE);
        ctx.blit(HCS_ICONS_TEXTURE, x, y, 0, u, v, width, height, 256, 256);
        RenderSystem.setShaderTexture(0, Gui.GUI_ICONS_LOCATION);
    }

    @Deprecated
    public void drawHCSTexture(@NotNull GuiGraphics ctx, int x, int y, int u, int v, int width, int height, float scale) {
        float descale = 1 / scale;
        x = (int) (x * descale);
        y = (int) (y * descale);
        ctx.pose().scale(scale, scale, scale);
        this.drawHCSTexture(ctx, x, y, u, v, width, height);
        ctx.pose().scale(descale, descale, descale);//reset to default scale
    }

    public void drawTextWithThickShadow(@NotNull GuiGraphics ctx, String text, int x, int y, int color, float scale) {
        Minecraft minecraft = Minecraft.getInstance();
        Font renderer = minecraft.font;
        float descale = 1 / scale;
        ctx.pose().scale(scale, scale, scale);
        x = (int) (x * descale);
        y = (int) (y * descale);
        // TODO test here if redundant or unsuitable
        ctx.drawString(renderer, text, x + 1, y, 0, false);
        ctx.drawString(renderer, text, x - 1, y, 0, false);
        ctx.drawString(renderer, text, x, y + 1, 0, false);
        ctx.drawString(renderer, text, x, y - 1, 0, false);
        ctx.drawString(renderer, text, x, y, color, false);
        ctx.pose().scale(descale, descale, descale);
    }

    public int getDrawIconHeight(float val) {
        int result = Math.round(val * 14) + 1;//+2
        if ((val <= 0 && result >= 2) || result < 0) result = 0;
        else if (result > 16) result = 16;
        return result;
    }

    public int getDrawIconHeight(double val, int initAdd, int maxCut) {
        int result = Math.round((float) val * ((14 - maxCut) - initAdd)) + initAdd;
        if ((val <= 0 && result >= 2) || result < 0) result = 0;
        else if (result > 16) result = 16;
        return result;
    }

    public int getColorByPercentage(double val) {
        int r, g;
        String R, G;
        if (val > 1) val = 1;
        else if (val < 0) val = 0;
        if (val > 0.5) {
            r = Mth.clamp((int) (((1 - val) * 2) * 255), 0, 255);
            g = 255;
        } else {
            r = 255;
            g = Mth.clamp((int) ((val * 2) * 255), 0, 255);
        }
        R = Integer.toHexString(r);
        G = Integer.toHexString(g);
        if (R.length() < 2) R = "0" + R;
        if (G.length() < 2) G = "0" + G;
        return Integer.parseInt(R + G + "00", 16);
    }

    public int getTemperatureColor(double val) {
        int r = 0, g = 0, b = 0;
        String R, G, B;

        // 限制取值范围在 0 到 1 之间
        if (val > 1) val = 1;
        else if (val < 0) val = 0;

        if (val >= 0.5) {
            // 后半段：绿 (0.5) -> 黄 (0.75) -> 红 (1.0)
            double t = (val - 0.5) * 2;
            if (t > 0.5) {
                r = 255;
                g = Mth.clamp((int) (((1 - t) * 2) * 255), 0, 255);
            } else {
                r = Mth.clamp((int) ((t * 2) * 255), 0, 255);
                g = 255;
            }
        } else {
            // 前半段：天蓝 (0.0) -> 青 (0.25) -> 绿 (0.5)
            double t = val * 2;
            if (t > 0.5) {
                g = 255;
                b = Mth.clamp((int) (((1 - t) * 2) * 255), 0, 255);
            } else {
                // 修改此处：最低温度时不再是纯蓝，而是给绿色打底 (128 起步)
                // 这样 0.0 时为天蓝色 (0, 128, 255)，极其明亮清晰
                g = Mth.clamp((int) ((t * 2) * 127) + 128, 0, 255);
                b = 255;
            }
        }

        // 转换为 16 进制字符串，不足两位补 0
        R = Integer.toHexString(r);
        G = Integer.toHexString(g);
        B = Integer.toHexString(b);
        if (R.length() < 2) R = "0" + R;
        if (G.length() < 2) G = "0" + G;
        if (B.length() < 2) B = "0" + B;

        return Integer.parseInt(R + G + B, 16);
    }

    public double getTempForDisplay(double x) {
        if (x <= 0.5F) return 0.5 - Math.pow(0.5 - x, 1.6) * 1.5;
        return Math.pow(x - 0.5, 1.6) * 1.5 + 0.5;
    }

    private void renderScreenEffects(@NotNull ForgeGui gui, GuiGraphics context, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = gui.getMinecraft();
        if (minecraft == null || minecraft.player == null || minecraft.level == null) return;

        if (!EntityHelper.IS_SURVIVAL_LIKE.test(minecraft.player)) return;

        Player player = minecraft.player;
        int tickCount = player.tickCount;

        double panic = ((StatAccessor) player).getMoodManager().getRealPanic();
        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
        SanityManager sanityManager = ((StatAccessor) player).getSanityManager();

        if (player.hasEffect(HcsEffects.INSANITY.get())) {
            double san = ((StatAccessor) player).getSanityManager().get();
            this.renderTextureOverlay(context, INSANITY_OUTLINE,
                    Math.min(1.0F, Math.max(0.3F,
                            1.0F - (float) san / 0.3F
                                    + 0.3F * (1.0F - (float) san / 0.6F) * Mth.sin((float) tickCount * (float) Math.PI / 15.0F))),
                    screenWidth, screenHeight);
        }

        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
        double temp = temperatureManager.get();
        float tempOpacity = Math.min(1.0F, 0.2F + temperatureManager.getSaturationPercentage());

        if (temp >= 1.0F && player.hasEffect(HcsEffects.HEATSTROKE.get())) {
            this.renderTextureOverlay(context, HEATSTROKE_BLUR, tempOpacity, screenWidth, screenHeight);
        } else if (temp <= 0.0F && player.getTicksFrozen() <= 0 && player.hasEffect(HcsEffects.HYPOTHERMIA.get())) {
            this.renderTextureOverlay(context, Gui.POWDER_SNOW_OUTLINE_LOCATION, tempOpacity, screenWidth, screenHeight);
        }

        int inDarkTicks = statusManager.getInDarknessTicks();
        if (inDarkTicks > 720 && inDarkTicks < 740 && HcsDifficulty.isOf(player, HcsDifficulty.HcsDifficultyEnum.challenging)) {
            this.renderTextureOverlay(context, DARKNESS_JUMP_SCARE, 0.9F, screenWidth, screenHeight);
        } else if (inDarkTicks > 60) {
            this.renderTextureOverlay(context, DARKNESS, Mth.clamp((inDarkTicks - 60) / 550.0F, 0.0F, 1.0F), screenWidth, screenHeight);
        }

        if (sanityManager.get() < 0.05 && player.level().getGameTime() % 800 < 8 && HcsDifficulty.isOf(player, HcsDifficulty.HcsDifficultyEnum.challenging))
            this.renderTextureOverlay(context, DARKNESS_JUMP_SCARE, 0.9F, screenWidth, screenHeight);

        if (statusManager.getRecentHurtTicks() > 0) {
            int x = Mth.clamp(20 - statusManager.getRecentHurtTicks(), 0, 20);
            float opa = Mth.clamp((float) (x < 5 ? Math.sin(Math.PI / 10 * x) : Math.sin(Math.PI / 30 * (x + 10))), 0.0F, 1.0F);
            opa *= Mth.clamp(statusManager.getRecentFeelingDamage() / 12.0F, 0.0F, 0.8F);
            this.renderTextureOverlay(context, HURT_BLUR, opa, screenWidth, screenHeight);
        }

        if (EntityHelper.getEffectAmplifier(player, HcsEffects.DEHYDRATED.get()) >= 0
                || EntityHelper.getEffectAmplifier(player, HcsEffects.STARVING.get()) >= 0
                || EntityHelper.getEffectAmplifier(player, HcsEffects.PAIN.get()) > 1) {
            float opacity = (float) (0.5 * (Mth.sin((float) Math.PI / 40 * tickCount) + 1));
            double hunger = applyNullable(player.getFoodData(), hm -> hm.getFoodLevel() / 20.0, 1.0);
            double thirst = ((StatAccessor) player).getThirstManager().get();
            double pain = ((StatAccessor) player).getInjuryManager().getRealPain();
            float multiplier = (float) Mth.clamp((0.3 - Mth.clamp(Math.min(hunger, thirst), 0.0, 0.3)) / 0.3 + (pain - 1.0) / 7, 0.0, 0.5);
            if (thirst < 0.2 || hunger < 0.2 || pain > 2.0) {
                this.renderTextureOverlay(context, DARKNESS, opacity * multiplier, screenWidth, screenHeight);
            }
        }

        if (EntityHelper.getEffectAmplifier(player, HcsEffects.PANIC.get()) >= 1) {
            this.renderTextureOverlay(context, FAINT_BLUR, Mth.clamp((float) ((panic - 2.0) / 5.0), 0.0F, 1.0F), screenWidth, screenHeight);
        }
    }

    private void renderStatusBars(@NotNull ForgeGui forgeGui, GuiGraphics ctx, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = forgeGui.getMinecraft();
        if (minecraft == null || minecraft.options.hideGui) return;

        Player player = Minecraft.getInstance().player;
        if (player == null || minecraft.player == null) return;
        if (!EntityHelper.IS_SURVIVAL_LIKE.test(player)) return;

        int tickCount = player.tickCount;
        var config = ((StatAccessor) player).getConfigManager();
        LivingEntity livingEntity = this.getPlayerVehicleWithHealth(player);
        PlayerRideableJumping jumpingMount = minecraft.player.jumpableVehicle();

        // Fix: Hide health bar for untamed horses to follow vanilla behavior and prevent displaying bugged double health values
        boolean isUntamedHorse = livingEntity instanceof AbstractHorse horse && !horse.isTamed();
        boolean shouldRenderMountHealth = livingEntity != null && livingEntity.getMaxHealth() > 0.0F && !isUntamedHorse;

        boolean shouldRenderMountJumpBar = jumpingMount != null;
        int renderExperienceBarX = screenWidth / 2 - 91;

        StatusManager statusManager = ((StatAccessor) player).getStatusManager();
        SanityManager sanityManager = ((StatAccessor) player).getSanityManager();
        TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();

        double san = sanityManager.get();
        boolean hasInsanityHide = san < 0.05 && Configs.isEnabled(SANITY);
        double temp = temperatureManager.get();
        float hea = player.getHealth();
        float arm = ArmorHelper.getFinalProtection(player);
        int air = player.getAirSupply();
        if (air < 0) air = 0;
        int airMax = player.getMaxAirSupply();

        // 1. 预先判断当前帧究竟需要显示哪些图标
        boolean showArmor = arm > 0 && !hasInsanityHide;
        boolean showHealth = !hasInsanityHide;
        boolean showStamina = config.get(STAMINA) && !hasInsanityHide;
        boolean showThirst = config.get(THIRST) && !hasInsanityHide;
        boolean showHunger = !hasInsanityHide;
        boolean showSanity = config.get(SANITY);
        boolean showTemp = config.get(TEMPERATURE) && !hasInsanityHide;
        boolean showAir = (player.isEyeInFluid(FluidTags.WATER) || air < airMax) && !hasInsanityHide;
        boolean showMount = shouldRenderMountHealth && livingEntity != null && !hasInsanityHide;

        // 2. 统计激活的图标总数
        int activeIcons = 0;
        if (showArmor) activeIcons++;
        if (showHealth) activeIcons++;
        if (showStamina) activeIcons++;
        if (showThirst) activeIcons++;
        if (showHunger) activeIcons++;
        if (showSanity) activeIcons++;
        if (showTemp) activeIcons++;
        if (showAir) activeIcons++;
        if (showMount) activeIcons++;

        // 3. 计算完美的居中起始 X 坐标
        // 每个图标占 20px (16px图标 + 4px间距)。向左偏移 activeIcons * 10 即可整体居中
        // +2 是为了抵消 16px 图标自身内部的中心点误差，使其与快捷栏完美对齐
        int xx = screenWidth / 2 - (activeIcons * 10) + 2;
        int yy = screenHeight - 46;
        int yyy = yy + 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (!hasInsanityHide) {
            // EXP BAR / JUMP BAR
            if (shouldRenderMountJumpBar) {
                float f = minecraft.player.getJumpRidingScale();
                int j = (int) (f * 183.0F);
                int k = screenHeight - 32 + 3;
                ctx.blit(Gui.GUI_ICONS_LOCATION, renderExperienceBarX, k, 0, 84, 182, 5);
                if (j > 0) {
                    ctx.blit(Gui.GUI_ICONS_LOCATION, renderExperienceBarX, k, 0, 89, j, 5);
                }
            } else {
                int l;
                int k;
                int i = minecraft.player.getXpNeededForNextLevel();
                if (i > 0) {
                    k = (int) (minecraft.player.experienceProgress * 183.0F);
                    l = screenHeight - 32 + 3;
                    ctx.blit(Gui.GUI_ICONS_LOCATION, renderExperienceBarX, l, 0, 64, 182, 5);
                    if (k > 0) {
                        ctx.blit(Gui.GUI_ICONS_LOCATION, renderExperienceBarX, l, 0, 69, k, 5);
                    }
                }
                if (minecraft.player.experienceLevel > 0) {
                    String string = String.valueOf(minecraft.player.experienceLevel);
                    k = (screenWidth - minecraft.font.width(string)) / 2;
                    l = screenHeight - 31 + 4;
                    this.drawTextWithThickShadow(ctx, string, k, l, 8453920, 1F);
                }
            }
        }

        // ==========================================
        // 图标依序渲染 (永远在渲染完毕后给 xx += 20)
        // ==========================================

        // ARMOR
        if (showArmor) {
            float armPercentage = arm / 20;
            int armHeight = this.getDrawIconHeight(armPercentage);
            this.drawHCSTexture(ctx, xx, yy, 0, 32, 16, 16);
            this.drawHCSTexture(ctx, xx, yy + 16 - armHeight, 16, 48 - armHeight, 16, armHeight);
            this.drawTextWithThickShadow(ctx, String.format("%.1f", arm), arm < 10.0F ? (xx + 4) : (xx + 1), yyy + 11, getColorByPercentage(armPercentage), 0.75F);
            xx += 20; // 渲染完后，坐标向右推
        }

        // HEALTH
        if (showHealth) {
            float heaMax = player.getMaxHealth();
            float heaPercentage = hea / heaMax;
            int heaHeight = this.getDrawIconHeight((float) Math.pow(heaPercentage, 0.8D));
            int heaDeviation = 0;
            int heaShake = 0;
            float heaAbsorption = player.getAbsorptionAmount();

            if (Mth.floor(hea) - Mth.floor(heaLast) != 0 || heaLast > hea) heaTwinkleCoolDown = 5;
            boolean heaTwinkle = heaTwinkleCoolDown > 0;
            if (hea <= 4 && tickCount % 3 == 0) heaShake = Math.round((float) Math.random() * 2) - 1;

            if (player.hasEffect(MobEffects.POISON)) {
                heaDeviation = 16;
                if (hea > 0 && hea < 1) heaHeight = 2;
            } else if (player.hasEffect(MobEffects.WITHER)) heaDeviation = 32;
            else if (player.hasEffect(MobEffects.ABSORPTION)) heaDeviation = 48;
            else if (player.isFullyFrozen()) heaDeviation = 80;
            else if (temp >= 1.0F) heaDeviation = 64;

            this.drawHCSTexture(ctx, xx, yy + heaShake, heaTwinkle ? 16 : 0, 0, 16, 16);
            if (heaTwinkle) --heaTwinkleCoolDown;
            this.drawHCSTexture(ctx, xx, yy + (16 - heaHeight) + heaShake, 32 + heaDeviation, 16 - heaHeight, 16, heaHeight);
            if (player.level().getLevelData().isHardcore()) {
                this.drawHCSTexture(ctx, xx, yy + (16 - heaHeight) + heaShake, heaDeviation > 0 ? 144 : 128, 16 - heaHeight, 16, heaHeight);
            }
            this.drawTextWithThickShadow(ctx, String.format("%.1f", hea > 0 ? Math.max(hea, 0.1F) : Math.max(hea, 0.0F)), hea < 10.0F ? xx + 2 : xx, yyy + 11, getColorByPercentage(heaPercentage), 0.75F);
            this.drawTextWithThickShadow(ctx, (heaAbsorption >= 1.0F ? "+" + String.format("%.1f", heaAbsorption) : "") + "/" + String.format("%.1f", heaMax), xx, yyy + 17, getColorByPercentage(heaPercentage), 0.5F);
            xx += 20;
        }

        // STAMINA
        if (showStamina) {
            double stam = ((StatAccessor) player).getStaminaManager().get();
            int strHeight = this.getDrawIconHeight((float) Math.pow(stam, 0.8D));
            int strDeviation = 0, strShake = 0;
            if (tickCount % (Math.round((float) stam * 20) + 1) == 0 && stam < 0.3F)
                strShake = Math.round((float) Math.random() * 2) - 1;
            this.drawHCSTexture(ctx, xx, yy + strShake, 0, 112, 16, 16);
            this.drawHCSTexture(ctx, xx, yy + (16 - strHeight) + strShake, 16 + strDeviation, 128 - strHeight, 16, strHeight);
            this.drawTextWithThickShadow(ctx, numFormat(stam < 0.1 ? " #%" : "##%", stam), xx + 2, yyy + 11, getColorByPercentage(stam), 0.75F);
            xx += 20;
        }

        // THIRST
        if (showThirst) {
            double thi = ((StatAccessor) player).getThirstManager().get();
            int thiHeight = this.getDrawIconHeight(thi, 1, 1);
            if (thiHeight < 0) thiHeight = 0;
            else if (thi > 0.05F && thiHeight <= 1) thiHeight = 2;
            int thiDeviation = 0, thiShake = 0;
            if (tickCount % (Math.round((float) thi * 20) * 3 + 1) == 0 && thi < 0.3F)
                thiShake = Math.round((float) Math.random() * 2) - 1;
            if (player.hasEffect(HcsEffects.THIRST.get()) || player.hasEffect(HcsEffects.DIARRHEA.get()) || player.hasEffect(HcsEffects.PARASITE_INFECTION.get()) || player.hasEffect(HcsEffects.FOOD_POISONING.get()))
                thiDeviation = 16;
            this.drawHCSTexture(ctx, xx, yy + thiShake, 0, 48, 16, 16);
            this.drawHCSTexture(ctx, xx, yy + (16 - thiHeight) + thiShake, 16 + thiDeviation, 64 - thiHeight, 16, thiHeight);
            this.drawTextWithThickShadow(ctx, numFormat(thi < 0.1 ? " #%" : "##%", thi), xx + 2, yyy + 11, getColorByPercentage(thi), 0.75F);
            xx += 20;
        }

        // HUNGER
        if (showHunger) {
            FoodData hunManager = player.getFoodData();
            float hun = (float) hunManager.getFoodLevel();
            float hunSaturation = hunManager.getSaturationLevel();
            float hunExhaustion = (hunSaturation > 0 || statusManager.hasDecimalFoodLevel()) ? 0.0F : statusManager.getExhaustion();
            float hunPercentage = (hun - hunExhaustion / 4.0F) / 20.0F;
            if (hunPercentage < 0.0F) hunPercentage = 0.0F;
            else if (hunPercentage > 1.0F) hunPercentage = 1.0F;
            int hunHeight = this.getDrawIconHeight(hunPercentage * 1.1F, 4, 0);
            int hunDeviation = 0, hunShake = 0;
            if (hunSaturation <= 0.0F && tickCount % (hun * 3 + 1) == 0)
                hunShake = Math.round((float) Math.random() * 2) - 1;
            if (player.hasEffect(MobEffects.HUNGER) || player.hasEffect(HcsEffects.DIARRHEA.get()) || player.hasEffect(HcsEffects.PARASITE_INFECTION.get()) || player.hasEffect(HcsEffects.FOOD_POISONING.get()))
                hunDeviation = 16;
            this.drawHCSTexture(ctx, xx, yy + hunShake, 0, 16, 16, 16);
            this.drawHCSTexture(ctx, xx, yy + (16 - hunHeight) + hunShake, 16 + hunDeviation, 32 - hunHeight, 16, hunHeight);
            this.drawTextWithThickShadow(ctx, numFormat(hunPercentage < 0.1 ? " #%" : "##%", hunPercentage), xx + 2, yyy + 11, getColorByPercentage(hunPercentage), 0.75F);
            xx += 20;
        }

        // SANITY
        if (showSanity) {
            double sanDifference = sanityManager.getDifference(), sanDifferenceAbs = Math.abs(sanDifference);
            if (san > 1.0F) san = 1.0F;
            else if (san < 0.0F) san = 0.0F;
            int sanHeight = this.getDrawIconHeight((float) Math.pow(san, 0.6D));
            int sanDeviation = 0, sanShake = 0;
            if (tickCount % (Math.round((float) san * 20) * 3 + 1) == 0 && san < 0.3F)
                sanShake = Math.round((float) Math.random() * 2) - 1;
            if (sanDifferenceAbs > 0.0049F) sanTwinkleCoolDown = 5;
            boolean sanTwinkle = sanTwinkleCoolDown > 0;
            if (sanTwinkle) --sanTwinkleCoolDown;
            this.drawHCSTexture(ctx, xx, yy + sanShake, sanTwinkle ? 16 : 0, 80, 16, 16);
            this.drawHCSTexture(ctx, xx, yy + (16 - sanHeight) + sanShake, 32 + sanDeviation, 96 - sanHeight, 16, sanHeight);
            if (sanDifferenceAbs > 0.0F && sanTwinkleCoolDown < 5) {
                int devi, shakeInterval = 24;
                if (sanDifference < -0.000079F) {
                    devi = 96;
                    shakeInterval = 6;
                } else if (sanDifference < -0.000039F) {
                    devi = 80;
                    shakeInterval = 12;
                } else if (sanDifference < 0.0F) devi = 64;
                else if (sanDifference < 0.000039F) devi = 112;
                else if (sanDifference < 0.000079F) {
                    devi = 128;
                    shakeInterval = 12;
                } else {
                    devi = 144;
                    shakeInterval = 6;
                }
                this.drawHCSTexture(ctx, xx, yy + (((tickCount % (shakeInterval * 2)) < shakeInterval) ? 1 : 0), devi, 80, 16, 16);
            }
            this.drawTextWithThickShadow(ctx, numFormat(san < 0.1F ? " #%" : "##%", san), xx + 2, yyy + 11, getColorByPercentage(san), 0.75F);
            xx += 20;
        }

        // TEMPERATURE
        if (showTemp) {
            float temSaturationPercentage = temperatureManager.getSaturationPercentage();
            int temShake = 0;
            if (tickCount % 3 == 0) temShake = Math.round((float) Math.random() * 2) - 1;
            int temHeight = this.getDrawIconHeight(temSaturationPercentage);
            if (temp <= 0.0F) {
                this.drawHCSTexture(ctx, xx, yy + temShake, 0, 64, 16, 16);
                this.drawHCSTexture(ctx, xx, yy + (16 - temHeight) + temShake, 16, 80 - temHeight, 16, temHeight);
            } else if (temp >= 1.0F) {
                this.drawHCSTexture(ctx, xx + temShake, yy, 208, 64, 16, 16);
                this.drawHCSTexture(ctx, xx, yy + (16 - temHeight) + temShake, 224, 80 - temHeight, 16, temHeight);
            } else {
                int temDeviation = (int) Math.floor(this.getTempForDisplay(temp) * 12) * 16;
                if (temDeviation <= 0) temDeviation = 16;
                else if (temDeviation > 176) temDeviation = 176;
                this.drawHCSTexture(ctx, xx, yy, 16 + temDeviation, 64, 16, 16);
            }

            // --- ADDED FOR TEMPERATURE NUMERICAL DISPLAY ---
            // Map [0.0, 1.0] to [-10.0, +10.0]
            double displayTemp = (temp - 0.5) * 20.0; // 这里改成了 * 20.0
            long roundedTemp = Math.round(displayTemp);
            // Format text with a '+' prefix for positive values
            String tempText = (roundedTemp > 0 ? "+" : "") + roundedTemp;

            // Adjust X offset based on text length for automatic centering
            int textOffset = 2;  // "+10", "-10"
            if (tempText.length() == 1) textOffset = 7;      // "0"
            else if (tempText.length() == 2) textOffset = 4; // "+5", "-6"

            // Draw text with calculated color gradient
            this.drawTextWithThickShadow(ctx, tempText, xx + textOffset, yyy + 11, getTemperatureColor(temp), 0.75F);
            // --- END ---

            xx += 20;
        }

        // AIR
        if (showAir) {
            int airShake = 0;
            float airPercentage = (float) air / airMax;
            if (air <= (airMax / 3) && tickCount % 3 == 0) airShake = Math.round((float) Math.random() * 2) - 1;
            this.drawHCSTexture(ctx, xx, yy + airShake, 32, 32, 16, 16);
            this.drawTextWithThickShadow(ctx, numFormat(airPercentage < 0.1 ? " #%" : "##%", airPercentage), xx + 2, yyy + 11, getColorByPercentage(airPercentage), 0.75F);
            xx += 20;
        }

        // MOUNT HEALTH
        if (showMount) {
            float mou = livingEntity.getHealth();
            float mouMax = livingEntity.getMaxHealth();
            float mouPercentage = mou / mouMax;
            int mouHeight = getDrawIconHeight(mouPercentage);
            this.drawHCSTexture(ctx, xx, yy, 0, 0, 16, 16);
            this.drawHCSTexture(ctx, xx, yy + (16 - mouHeight), 48, 48 - mouHeight, 16, mouHeight);
            this.drawTextWithThickShadow(ctx, String.format("%.1f", mou > 0 ? Math.max(mou, 0.1F) : Math.max(mou, 0.0F)), xx, yyy + 11, getColorByPercentage(mouPercentage), 0.75F);
            this.drawTextWithThickShadow(ctx, "/" + String.format("%.1f", mouMax), xx, yyy + 17, getColorByPercentage(mouPercentage), 0.5F);
            xx += 20;
        }

        this.heaLast = hea;

        ctx.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private void renderSelectedItemName(@NotNull ForgeGui forgeGui, GuiGraphics context) {
        Minecraft minecraft = forgeGui.getMinecraft();
        if (minecraft == null || minecraft.options.hideGui) return;
        if (minecraft.gameMode == null) return;
        if (this.toolHighlightTimer <= 0 || this.lastToolHighlight.isEmpty()) return;

        MutableComponent text = Component.empty().append(this.lastToolHighlight.getHoverName()).withStyle(this.lastToolHighlight.getRarity().color);
        if (this.lastToolHighlight.hasCustomHoverName()) {
            text.withStyle(ChatFormatting.ITALIC);
        }

        if (minecraft.player != null
                && EntityHelper.IS_SURVIVAL_LIKE.test(minecraft.player)
                && ((StatAccessor) minecraft.player).getSanityManager().get() < 0.1
                && minecraft.player.hasEffect(HcsEffects.INSANITY.get())) {
            text = MutableComponent.create(text.getContents()).withStyle(ChatFormatting.OBFUSCATED);
        }

        int opacity = (int) ((float) this.toolHighlightTimer * 256.0F / 10.0F);
        if (opacity > 255) opacity = 255;
        if (opacity <= 0) return;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int yShift = Math.max(forgeGui.leftHeight, forgeGui.rightHeight);

        int x = (screenWidth - minecraft.font.width(text)) / 2;
        int y = screenHeight - Math.max(yShift, 59);
        if (!minecraft.gameMode.canHurtPlayer()) y += 14;

        context.pose().pushPose();
        context.pose().translate(0.0F, 0.0F, -50.0F);
        this.drawItemNameBackdrop(context, x, y, minecraft.font.width(text), opacity);
        context.drawString(minecraft.font, text, x, y, 16777215 | opacity << 24);
        context.pose().popPose();
    }

    private void drawItemNameBackdrop(GuiGraphics context, int x, int y, int width, int opacity) {
        Minecraft minecraft = Minecraft.getInstance();
        int background = minecraft.options.getBackgroundColor(0.0F);
        if (background == 0) return;

        int alpha = (background >> 24) & 255;
        alpha = alpha * opacity / 255;
        background = (background & 16777215) | (alpha << 24);

        context.fill(x - 2, y - 2, x + width + 2, y + 9 + 2, background);
    }

    private void renderTextureOverlay(GuiGraphics context, ResourceLocation texture, float opacity, int screenWidth, int screenHeight) {
        if (opacity <= 0.0F) return;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        context.setColor(1.0F, 1.0F, 1.0F, opacity);
        context.blit(texture, 0, 0, -90, 0.0F, 0.0F, screenWidth, screenHeight, screenWidth, screenHeight);
        context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @Deprecated
    private @Nullable Player getCameraPlayer(@NotNull Minecraft minecraft) {
        return minecraft.getCameraEntity() instanceof Player player ? player : null;
    }

    private @Nullable LivingEntity getPlayerVehicleWithHealth(@NotNull Player player) {
        Entity entity = player.getVehicle();
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }
}