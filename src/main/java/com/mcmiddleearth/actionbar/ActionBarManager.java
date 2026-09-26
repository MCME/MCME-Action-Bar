package com.mcmiddleearth.actionbar;

import com.mcmiddleearth.actionbar.preferences.HudPreferences;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.ShadowColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionBarManager implements Runnable {

    private final List<HudElement> elements = new ArrayList<>();
    private final HudPreferences preferences;

    public ActionBarManager(HudPreferences preferences) {
        this.preferences = preferences;
    }

    public void register(HudElement element) {
        elements.add(element);
    }

    public List<String> getElementIds() {
        return elements.stream().map(HudElement::id).toList();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            List<Component> parts = new ArrayList<>();
            for (HudElement element : elements) {
                if (preferences.isHidden(player, element.id())) continue;
                Component c = element.getElement(player);
                if (c != null) parts.add(c);
            }

            if (!parts.isEmpty()) {
                player.sendActionBar(
                        Component.join(JoinConfiguration.noSeparators(), parts).shadowColor(ShadowColor.none()));
            }
        }
    }
}
