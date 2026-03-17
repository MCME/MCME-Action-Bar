package com.mcmiddleearth.actionbar.hud;

import com.mcmiddleearth.actionbar.HudElement;
import com.mcmiddleearth.actionbar.chat.ChatChannelProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ChatChannelHudElement implements HudElement {

    private final ChatChannelProvider provider;
    private final Configuration config;

    public ChatChannelHudElement(ChatChannelProvider provider, Configuration config) {
        this.provider = provider;
        this.config = config;
    }

    @Override
    public @Nullable Component getElement(Player player) {
        String channelName = provider.getActiveChannelName(player);
        if (channelName == null) return null;

        String symbol = resolveSymbol(channelName);
        if (symbol.isEmpty()) return null;

        return Component.text(symbol);
    }

    // Returns empty string if the symbol should not be displayed.
    private String resolveSymbol(String channelName) {
        String configured = config.getString("chat-indicator.channels." + channelName.toLowerCase());
        return configured != null ? configured : config.getString("chat-indicator.default", "");
    }
}
