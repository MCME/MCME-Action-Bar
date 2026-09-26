package com.mcmiddleearth.actionbar.preferences;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.entity.Player;

/**
 * Stores hidden elements as global LuckPerms meta (e.g. mcme-actionbar-hidden-chat-channel=true).
 * Meta is used instead of permission nodes so ops and wildcard permissions don't hide everything.
 * Nodes have no server context, so with a shared LuckPerms database they apply on every backend.
 */
public class LuckPermsHudPreferences implements HudPreferences {

    private static final String KEY_PREFIX = "mcme-actionbar-hidden-";

    private final LuckPerms luckPerms = LuckPermsProvider.get();

    @Override
    public boolean isHidden(Player player, String elementId) {
        // Reads LuckPerms' in-memory cache, cheap enough to call every update
        String value = luckPerms.getPlayerAdapter(Player.class).getMetaData(player).getMetaValue(KEY_PREFIX + elementId);
        return Boolean.parseBoolean(value);
    }

    @Override
    public void setHidden(Player player, String elementId, boolean hidden) {
        String key = KEY_PREFIX + elementId;
        luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
            user.data().clear(NodeType.META.predicate(node -> node.getMetaKey().equals(key)));
            if (hidden) {
                user.data().add(MetaNode.builder(key, "true").build());
            }
        });
    }
}
