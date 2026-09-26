package com.mcmiddleearth.actionbar;

import com.mcmiddleearth.actionbar.chat.VentureChatChannelProvider;
import com.mcmiddleearth.actionbar.command.HudCommand;
import com.mcmiddleearth.actionbar.hud.ChatChannelHudElement;
import com.mcmiddleearth.actionbar.preferences.HudPreferences;
import com.mcmiddleearth.actionbar.preferences.LuckPermsHudPreferences;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ActionBar extends JavaPlugin {

    private static ActionBar instance;
    private ActionBarManager actionBarManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // LuckPerms stores preferences in its shared database so they follow players across backends.
        // Without it every element is always shown and /hud is not registered.
        boolean hasLuckPerms = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
        HudPreferences preferences = hasLuckPerms ? new LuckPermsHudPreferences() : HudPreferences.ALWAYS_SHOWN;
        actionBarManager = new ActionBarManager(preferences);

        if (Bukkit.getPluginManager().isPluginEnabled("VentureChat")) {
            actionBarManager.register(new ChatChannelHudElement(
                new VentureChatChannelProvider()
            ));
        }

        // Nothing to show, so skip the update loop and /hud entirely
        if (actionBarManager.getElementIds().isEmpty()) {
            getLogger().info("No HUD elements registered, nothing to display");
            return;
        }

        if (hasLuckPerms) {
            getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(
                    HudCommand.create(actionBarManager, preferences),
                    "Show or hide HUD elements"
                )
            );
        }

        int tickInterval = getConfig().getInt("update-interval-ticks", 40);
        getServer().getScheduler().runTaskTimer(this, actionBarManager, 0, tickInterval);
    }

    public static ActionBar getInstance() {
        return instance;
    }
    public ActionBarManager getActionBarManager() {
        return actionBarManager;
    }
}