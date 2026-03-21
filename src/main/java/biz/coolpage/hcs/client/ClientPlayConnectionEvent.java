package biz.coolpage.hcs.client;

import biz.coolpage.hcs.util.UpdateHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@Environment(EnvType.CLIENT)
public class ClientPlayConnectionEvent {
    public static final String UPDATE_URL = "https://modrinth.com/mod/hardcore-survival/versions";
    public static final String WIKI_URL = "https://www.mcmod.cn/class/12595.html";
    public static String OFFICIAL_URL = "https://hcs.coolpage.biz/";

    public static @NotNull String combineText(@Nullable String... texts) {
        if (texts == null || texts.length == 0) return "";
        StringBuilder builder = new StringBuilder();
        for (String text : texts) if (text != null) builder.append(Text.translatable(text).getString());
        return builder.toString();
    }

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            if (client.player == null) return;
            //Show welcome message
            if (Locale.getDefault() != null) {
                String lang = Locale.getDefault().getLanguage();
                if (lang != null && !(lang.contains("zh") || lang.contains("hk") || lang.contains("tw")))
                    OFFICIAL_URL += "en/";
            }

            final String finalUrl = OFFICIAL_URL;
            client.player.sendMessage(Text.translatable(combineText("itemGroup.hcs.main", " Beta %1$s "), UpdateHelper.MOD_VER)
                            .append(Text.translatable("hcs.tip.official_link").formatted(Formatting.UNDERLINE, Formatting.AQUA).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, finalUrl))))
                            .append(Text.literal(" ")).append(Text.translatable("hcs.tip.wiki_link").formatted(Formatting.UNDERLINE, Formatting.AQUA).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, WIKI_URL))))
                            .append(Text.literal(" ")).append(Text.translatable("hcs.tip.config").formatted(Formatting.UNDERLINE, Formatting.AQUA).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, OFFICIAL_URL + "config.html"))))
                    , false);

            // Check and show update message
            new Thread(() -> {
                final String latestVersion = UpdateHelper.fetchLatestVersion();
                if (!latestVersion.isEmpty() && UpdateHelper.compareVersions(UpdateHelper.MOD_VER, latestVersion) < 0)
                    client.player.sendMessage(Text.translatable("hcs.tip.update", latestVersion).append(" ").append(Text.translatable("hcs.tip.download_link").formatted(Formatting.UNDERLINE, Formatting.AQUA).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, UPDATE_URL)))), false);
            }).start();
            // Sync C2S
            ClientC2S.writeC2SPacketOnPlayerEnter(client.player);
        }));
    }
}
