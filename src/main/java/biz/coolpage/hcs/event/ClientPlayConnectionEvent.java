package biz.coolpage.hcs.event;

import biz.coolpage.hcs.status.network.ClientC2S;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;

@Environment(EnvType.CLIENT)
public class ClientPlayConnectionEvent {
    public static final String SPONSOR_URL = "http://hcs.coolpage.biz/sponsorship.html"; //See at PlayerEntityMixin/readCustomDataFromNbt(); ClientPlayerEntityMixin/tick()

    public static void init() {
        //Show welcome message
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            if (client.player == null) return;
            String url = "http://hcs.coolpage.biz";
            if (Locale.getDefault() != null) {
                String lang = Locale.getDefault().getLanguage();
                if (lang != null && !(lang.contains("zh") || lang.contains("hk") || lang.contains("tw"))) url += "/en";
            }
            final String finalUrl = url;
            client.player.sendMessage(Text.translatable("hcs.tip.welcome").append(Text.literal(url).formatted(Formatting.UNDERLINE).formatted(Formatting.AQUA).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, finalUrl)))), false);
            ClientC2S.writeC2SPacketOnPlayerEnter(client.player);
        }));
    }
}
