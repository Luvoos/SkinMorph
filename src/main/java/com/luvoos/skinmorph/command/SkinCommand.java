package com.luvoos.skinmorph.command;

import com.luvoos.skinmorph.FavoritesManager;
import com.luvoos.skinmorph.SkinManager;
import com.luvoos.skinmorph.SkinMorph;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SkinCommand implements CommandExecutor
{
    private final SkinMorph plugin;
    private final FavoritesManager favoritesManager;
    private final SkinManager skinManager;

    private static final String MSG_NO_PERMISSION = "§cYou don't have permission to do that!";
    private static final String MSG_INVALID_USAGE = "§cInvalid usage! Run §e/skin help§c for help.";
    private static final String MSG_PLAYER_NOT_FOUND = "§cCouldn't find online player §e%s§c!";
    private static final String MSG_PLAYER_ONLY = "§cYou may only do this as a player!";

    public SkinCommand(SkinMorph plugin, FavoritesManager favoritesManager, SkinManager skinManager)
    {
        this.plugin = plugin;
        this.favoritesManager = favoritesManager;
        this.skinManager = skinManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args)
    {
        final int len = args.length;
        if (!sender.hasPermission("skinmorph.command.skin")) {
            sender.sendMessage(MSG_NO_PERMISSION);
            return true;
        }

        if (!isValidUsage(args)) {
            sender.sendMessage(MSG_INVALID_USAGE);
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage(MSG_PLAYER_ONLY);
                return true;
            }
            plugin.menu.open(player, plugin.menu.getCurrentPage(player));
            return true;
        }

        switch (args[0]) {
            case "set" -> {
                if (len == 2) {
                    if (!(sender instanceof final Player player)) {
                        sender.sendMessage(MSG_PLAYER_ONLY);
                        return true;
                    }
                    String skinNameInput = args[1];
                    skinManager.setSkinFromName(player, skinNameInput, true);
                } else if (len == 3) {
                    if (Bukkit.getPlayer(args[2]) == null) {
                        sender.sendMessage(String.format(MSG_PLAYER_NOT_FOUND, args[2]));
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[2]);
                    String skinName = args[1];
                    skinManager.setSkinFromName(sender, target, skinName, true);
                }
                return true;
            }
            case "random" -> {
                if (len == 1) {
                    if (!(sender instanceof final Player player)) {
                        sender.sendMessage(MSG_PLAYER_ONLY);
                        return true;
                    }
                    skinManager.applyRandomSkin(player, true);
                } else if (len == 2) {
                    if (Bukkit.getPlayer(args[1]) == null) {
                        sender.sendMessage(String.format(MSG_PLAYER_NOT_FOUND, args[1]));
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[1]);
                    skinManager.applyRandomSkin(sender, target, true);
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
                        skinManager.resetSkin(player, true);
                    }
                } else if (len == 2) {
                    if (Bukkit.getPlayer(args[1]) == null) {
                        sender.sendMessage(String.format(MSG_PLAYER_NOT_FOUND, args[1]));
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[1]);
                    skinManager.resetSkin(sender, target, true);
                }
            }
            case "favorites" -> {
                if (len == 3) {
                    if (args[1].equalsIgnoreCase("add")) {
                        if (!(sender instanceof final Player player)) {
                            sender.sendMessage(MSG_PLAYER_ONLY);
                            return true;
                        }
                        favoritesManager.addFavorite(player, args[2], true);
                    } else if (args[1].equalsIgnoreCase("remove")) {
                        if (!(sender instanceof final Player player)) {
                            sender.sendMessage(MSG_PLAYER_ONLY);
                            return true;
                        }
                        favoritesManager.removeFavorite(player, args[2], true);
                    }
                }
            }
            case "settings" -> {
                if (len == 1) {
                    if (!(sender instanceof final Player player)) {
                        sender.sendMessage(MSG_PLAYER_ONLY);
                        return true;
                    }
                    plugin.menu.openSettings(player);
                }
            }
        }
        return true;
    }

    /**
     * Check if the /skin command was used correctly
     * @param args
     * @return Valid usage?
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

            case "settings":
                // /skin settings
                return len == 1;

            default:
                return false;
        }
    }
}
