package com.mcmiddleearth.actionbar.hud;

import com.mcmiddleearth.actionbar.HudElement;
import com.mcmiddleearth.actionbar.chat.ChatChannelProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ChatChannelHudElement implements HudElement {

    private static final String DEFAULT_SYMBOL = "\uE200";

    // Maps VentureChat channel names to their resource pack glyphs.
    // Empty string means no indicator is shown for that channel.
    // Each glyph is a bitmap font character defined in the resource pack's font/default.json.
    private static final Map<String, String> CHANNEL_SYMBOLS = Map.of(
        "global",      "",
        "local",       "\uE201",
        "tour",        "\uE202",
        "job-general", "\uE203",
        "staff",       "\uE204"
    );

    private final ChatChannelProvider provider;

    public ChatChannelHudElement(ChatChannelProvider provider) {
        this.provider = provider;
    }

    @Override
    public @Nullable Component getElement(Player player) {
        String channelName = provider.getActiveChannelName(player);
        if (channelName == null) return null;

        String symbol = CHANNEL_SYMBOLS.getOrDefault(channelName.toLowerCase(), DEFAULT_SYMBOL);
        if (symbol.isEmpty()) return null;

        return Component.text(symbol);
    }
}
