package com.mcmiddleearth.actionbar.command;

import com.mcmiddleearth.actionbar.ActionBarManager;
import com.mcmiddleearth.actionbar.preferences.HudPreferences;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

/** /hud <show|hide> <element> */
public final class HudCommand {

    private enum Action { SHOW, HIDE }

    private HudCommand() {}

    public static LiteralCommandNode<CommandSourceStack> create(ActionBarManager manager, HudPreferences preferences) {
        var root = Commands.literal("hud");

        for (Action action : Action.values()) {
            root.then(Commands.literal(action.name().toLowerCase())
                .then(Commands.argument("element", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        manager.getElementIds().stream()
                            .filter(id -> id.startsWith(builder.getRemainingLowerCase()))
                            .forEach(builder::suggest);
                        return builder.buildFuture();
                    })
                    .executes(ctx -> execute(ctx, action, manager, preferences))));
        }

        return root.build();
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, Action action,
                               ActionBarManager manager, HudPreferences preferences) {
        // The executor is who the command runs as, so `/execute as <player> run hud ...` changes that player's HUD
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage(Component.text("Only players have a HUD.", NamedTextColor.RED));
            return 0;
        }

        String elementId = StringArgumentType.getString(ctx, "element");

        if (!manager.getElementIds().contains(elementId)) {
            player.sendMessage(Component.text("Unknown HUD element: " + elementId, NamedTextColor.RED));
            return 0;
        }

        boolean hide = action == Action.HIDE;
        preferences.setHidden(player, elementId, hide);

        player.sendMessage(Component.text(
            elementId + (hide ? " hidden." : " shown."),
            hide ? NamedTextColor.YELLOW : NamedTextColor.GREEN
        ));
        return Command.SINGLE_SUCCESS;
    }
}
