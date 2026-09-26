package com.mcmiddleearth.actionbar.preferences;

import org.bukkit.entity.Player;

public interface HudPreferences {

    /** Used when no storage backend is available: every element is shown. */
    HudPreferences ALWAYS_SHOWN = new HudPreferences() {
        @Override
        public boolean isHidden(Player player, String elementId) {
            return false;
        }

        @Override
        public void setHidden(Player player, String elementId, boolean hidden) {
            throw new UnsupportedOperationException("No HUD preference storage available");
        }
    };

    boolean isHidden(Player player, String elementId);

    void setHidden(Player player, String elementId, boolean hidden);
}
