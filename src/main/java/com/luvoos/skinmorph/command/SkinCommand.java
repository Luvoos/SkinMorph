package com.luvoos.skinmorph.command;

import com.luvoos.skinmorph.SkinMorph;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;

public class SkinCommand implements CommandExecutor {

    private final SkinMorph plugin = SkinMorph.instance;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args)
    {
        int len = args.length;
        if (!sender.hasPermission("skinmorph.command.skin")) {
            sender.sendMessage("§cYou don't have permission to do that!");
            return true;
        }

        if (!isValidUsage(args)) {
            sender.sendMessage("§cInvalid usage! Run §e/skin help§c for help.");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage("§cYou can only do this as a player!");
                return true;
            }
            plugin.menu.open(player, 1);
            return true;
        }

        switch (args[0]) {
            case "set" -> {
                if (len == 1) {
                    sender.sendMessage("§cYou need to enter a skin name!");
                } else if (len == 2) {
                    if (!(sender instanceof final Player player)) {
                        sender.sendMessage(ChatColor.RED + "You can only do this as a player!");
                        return true;
                    }
                    String skinNameInput = args[1];
                    plugin.skinManager.setSkinFromName(player, skinNameInput, true);
                } else if (len == 3) {
                    if (Bukkit.getPlayer(args[2]) == null) {
                        sender.sendMessage("§cCouldn't find online player '§e" + args[2] + "§c'!");
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[2]);
                    String skinName = args[1];
                    plugin.skinManager.setSkinFromName(sender, target, skinName, true);
                }
                return true;
            }
            case "random" -> {
                if (len == 1) {
                    if (!(sender instanceof final Player player)) {
                        sender.sendMessage(ChatColor.RED + "You can only do this as a player!");
                        return true;
                    }
                    plugin.skinManager.applyRandomSkin(player, true);
                } else if (len == 2) {
                    if (Bukkit.getPlayer(args[1]) == null) {
                        sender.sendMessage("§cCouldn't find online player '§e" + args[1] + "§c'!");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[1]);
                    plugin.skinManager.applyRandomSkin(sender, target, true);
                }
            }
            case "help" -> {
                if (len == 1) {
                    sender.sendMessage(
                            "§b§lSkin Morph Help",
                            "/skin - Open the Skin Menu",
                            "/skin set <skinName> - Set your own skin",
                            "/skin set <skinName> <targetName> - Set another player's skin",
                            "/skin reset - Reset your skin",
                            "/skin reset <targetName> - Reset another player's skin",
                            "/skin favorites add <skinName> - Add a favorite",
                            "/skin favorites remove <skinName> - Remove a favorite",
                            "/skin random - Apply a random skin from your favorites",
                            "/skin help - Displays this help message"
                    );
                }
            }
            case "reset" -> {
                if (len == 1) {
                    if (sender instanceof final Player player) {
                        plugin.skinManager.resetSkin(player, true);
                    }
                } else if (len == 2) {
                    if (Bukkit.getPlayer(args[1]) == null) {
                        sender.sendMessage("§cCouldn't find online player '§e" + args[1] + "§c'!");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[1]);
                    plugin.skinManager.resetSkin(sender, target, true);
                }
            }
            case "favorites" -> {
                if (len == 3) {
                    if (args[1].equalsIgnoreCase("add")) {
                        if (!(sender instanceof final Player player)) {
                            sender.sendMessage(ChatColor.RED + "You can only do this as a player!");
                            return true;
                        }
                        try {
                            plugin.favoritesManager.addFavorite(player, args[2], true);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (args[1].equalsIgnoreCase("remove")) {
                        if (!(sender instanceof final Player player)) {
                            sender.sendMessage(ChatColor.RED + "You can only do this as a player!");
                            return true;
                        }
                        plugin.favoritesManager.removeFavorite(player, args[2], true);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Check if the /skin command was used correctly
     * @param args
     * @return Correct usage?
     */
    private boolean isValidUsage(final @NotNull String @NotNull [] args)
    {
        final int len = args.length;

        // /skin
        if (len == 0) return true;

        // too many arguments
        if (len > 3) return false;

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "set":
                // /skin set <skinName>
                // /skin set <skinName> <targetName>
                return len == 2 || len == 3;

            case "reset":
                // /skin reset
                // /skin reset <targetName>
                return len == 1 || len == 2;

            case "favorites":
                if (len < 3) return false;
                String action = args[1].toLowerCase();
                switch (action) {
                    case "add":
                    case "remove":
                        // /skin favorites add <skinName>
                        // /skin favorites remove <skinName>
                        return len == 3;
                    default:
                        return false;
                }

            case "random":
                // /skin random
                // /skin random <targetName>
                return len == 1 || len == 2;

            case "help":
                // /skin help
                return len == 1;

            default:
                return false;
        }
    }
}
