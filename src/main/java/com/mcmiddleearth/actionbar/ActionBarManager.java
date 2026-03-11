package com.mcmiddleearth.actionbar;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.ShadowColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class ActionBarManager implements Runnable {

    private final List<HudElement> elements = new ArrayList<>();

    public void register(HudElement element) {
        elements.add(element);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            List<Component> parts = new ArrayList<>();
            for (HudElement element : elements) {
                Component c = element.getElement(player);
                if (c != null) parts.add(c);
            }

            if (!parts.isEmpty()) {
                player.sendActionBar(
                    Component
                        .join(JoinConfiguration.noSeparators(), parts)
                        .shadowColor(ShadowColor.none())
                );
            }
        }
    }
}
