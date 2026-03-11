package com.mcmiddleearth.actionbar;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface HudElement {
    @Nullable Component getElement(Player player);
}
