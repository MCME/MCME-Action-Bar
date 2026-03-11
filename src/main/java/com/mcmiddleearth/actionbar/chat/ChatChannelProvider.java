package com.mcmiddleearth.actionbar.chat;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface ChatChannelProvider {
    @Nullable String getActiveChannelName(Player player);
}
