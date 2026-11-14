package com.motol.dialogsplus;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DialogCommand implements TabExecutor {

    private static final String OTHERS_PERMISSION = "dialogsplus.open.others";
    private static final List<String> SELECTORS = List.of("@a", "@p", "@s", "@r");

    private final MenuOpener menuOpener;

    public DialogCommand(MenuOpener menuOpener) {
        this.menuOpener = menuOpener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String menuId = args.length > 0 ? args[0] : "main";

        if (args.length >= 2) {
            List<Player> targets = resolveTargets(sender, args[1]);
            if (targets == null) {
                return true;
            }
            if (targets.isEmpty()) {
                sender.sendMessage("No matching players found for '" + args[1] + "'.");
                return true;
            }

            boolean onlySelf = targets.size() == 1 && sender instanceof Player self && targets.get(0).equals(self);
            if (!onlySelf && !sender.hasPermission(OTHERS_PERMISSION)) {
                sender.sendMessage("You don't have permission to open menus for other players.");
                return true;
            }

            for (Player target : targets) {
                menuOpener.open(target, menuId);
            }
            sender.sendMessage("Opened '" + menuId + "' for " + targets.size() + (targets.size() == 1 ? " player." : " players."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Usage from console: /dialogs <menu-id> <player|selector>");
            return true;
        }

        menuOpener.open(player, menuId);
        return true;
    }

    private List<Player> resolveTargets(CommandSender sender, String input) {
        List<Player> result = new ArrayList<>();

        if (input.startsWith("@")) {
            try {
                for (Entity entity : Bukkit.selectEntities(sender, input)) {
                    if (entity instanceof Player player) {
                        result.add(player);
                    }
                }
            } catch (IllegalArgumentException e) {
                sender.sendMessage("Invalid selector: " + input);
                return null;
            }
            return result;
        }

        Player target = Bukkit.getPlayerExact(input);
        if (target != null) {
            result.add(target);
        }
        return result;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 2) {
            return List.of();
        }

        String typed = args[1].toLowerCase(Locale.ROOT);
        List<String> options = new ArrayList<>(SELECTORS);
        for (Player online : Bukkit.getOnlinePlayers()) {
            options.add(online.getName());
        }
        options.removeIf(option -> !option.toLowerCase(Locale.ROOT).startsWith(typed));
        return options;
    }
}