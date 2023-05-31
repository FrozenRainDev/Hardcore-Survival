package com.hcs.main.event;

import com.hcs.misc.network.ClientC2S;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;

public class ClientPlayConnectionEvent {
    public static void init() {
        //Show welcome message
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            if (client.player == null) return;
            String url = "http://hcs.coolpage.biz";
            if (Locale.getDefault() != null) {
                String lang = Locale.getDefault().getLanguage();
                if (lang != null) {
                    if (!(lang.contains("zh") || lang.contains("hk") || lang.contains("tw"))) url += "/en";
                }
            }
            final String finalUrl = url;
            client.player.sendMessage(Text.translatable("hcs.tip.welcome").append(Text.literal(url).formatted(Formatting.UNDERLINE).formatted(Formatting.AQUA).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, finalUrl)))), false);
            ClientC2S.writeC2SPacketOnPlayerEnter(client.player);
        }));
    }
}
