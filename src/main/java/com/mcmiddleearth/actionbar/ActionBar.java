package com.mcmiddleearth.actionbar;

import com.mcmiddleearth.actionbar.chat.VentureChatChannelProvider;
import com.mcmiddleearth.actionbar.hud.ChatChannelHudElement;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ActionBar extends JavaPlugin {

    private static ActionBar instance;
    private ActionBarManager actionBarManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        actionBarManager = new ActionBarManager();

        if (Bukkit.getPluginManager().getPlugin("VentureChat") != null) {
            actionBarManager.register(new ChatChannelHudElement(
                new VentureChatChannelProvider(),
                getConfig()
            ));
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