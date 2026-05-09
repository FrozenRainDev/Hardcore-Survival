package biz.coolpage.hcs.client;

import biz.coolpage.hcs.Hcs;
import biz.coolpage.hcs.util.UpdateHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class ClientPlayConnectionEvent {
    public static final String UPDATE_URL = "https://modrinth.com/mod/hardcore-survival/versions";
    public static final String WIKI_URL = "https://www.mcmod.cn/class/12595.html";
    public static String OFFICIAL_URL = "https://hcs.coolpage.biz/";

    public static @NotNull String combineText(@Nullable String... texts) {
        if (texts == null || texts.length == 0) return "";
        StringBuilder builder = new StringBuilder();
        for (String text : texts) {
            if (text != null) {
                // 在1.20.1 Mojang映射中，Text.translatable改为Component.translatable
                builder.append(Component.translatable(text).getString());
            }
        }
        return builder.toString();
    }

    public static void init() {
        // Forge 使用事件总线注册
        MinecraftForge.EVENT_BUS.register(new ClientPlayConnectionEvent());
    }

    @SubscribeEvent
    public void onPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        // Show welcome message
        if (Locale.getDefault() != null) {
            String lang = Locale.getDefault().getLanguage();
            // 尊重原代码结构，此处逻辑保留
            if (lang != null && !(lang.contains("zh") || lang.contains("hk") || lang.contains("tw"))) {
                if (!OFFICIAL_URL.endsWith("en/")) {
                    OFFICIAL_URL += "en/";
                }
            }
        }

        final String finalUrl = OFFICIAL_URL;

        // Fabric: sendMessage -> Forge/Mojang: displayClientMessage (带overlay参数) 或 sendSystemMessage
        client.player.displayClientMessage(Component.translatable(combineText("itemGroup.hcsurvival.main", " Beta %1$s "), UpdateHelper.MOD_VER)
                        .append(Component.translatable("tip.hcsurvival.official_link").withStyle(s -> s.withColor(ChatFormatting.AQUA).withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, finalUrl))))
                        .append(Component.literal(" "))
                        .append(Component.translatable("tip.hcsurvival.wiki_link").withStyle(s -> s.withColor(ChatFormatting.AQUA).withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, WIKI_URL))))
                        .append(Component.literal(" "))
                        .append(Component.translatable("tip.hcsurvival.config").withStyle(s -> s.withColor(ChatFormatting.AQUA).withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, OFFICIAL_URL + "config.html"))))
                , false);

        // Check and show update message
        new Thread(() -> {
            try {
                final String latestVersion = UpdateHelper.fetchLatestVersion();
                if (!latestVersion.isEmpty() && UpdateHelper.compareVersions(UpdateHelper.MOD_VER, latestVersion) < 0) {
                    client.player.displayClientMessage(Component.translatable("tip.hcsurvival.update", latestVersion)
                            .append(" ")
                            .append(Component.translatable("tip.hcsurvival.download_link").withStyle(s -> s.withColor(ChatFormatting.AQUA).withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, UPDATE_URL)))), false);
                }
            } catch (Exception e) {
                Hcs.error("Failed to check for updates: " + e.getMessage());
            }
        }).start();

        // Sync C2S
        ClientC2S.writeC2SPacketOnPlayerEnter(client.player);
    }
}