package com.mcmiddleearth.actionbar.chat;

import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class VentureChatChannelProvider implements ChatChannelProvider {

    @Override
    public @Nullable String getActiveChannelName(Player player) {
        MineverseChatPlayer cp = MineverseChatAPI.getOnlineMineverseChatPlayer(player.getUniqueId());
        if (cp == null) return null;
        return cp.getCurrentChannel().getName();
    }
}
