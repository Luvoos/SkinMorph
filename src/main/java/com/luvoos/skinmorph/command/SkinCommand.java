package com.luvoos.skinmorph.command;

import com.luvoos.skinmorph.SkinMorph;
import com.luvoos.skinmorph.manager.FavoritesManager;
import com.luvoos.skinmorph.manager.LanguageManager;
import com.luvoos.skinmorph.manager.SkinManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SkinCommand implements CommandExecutor
{
    private final SkinMorph plugin;
    private final FavoritesManager favoritesManager;
    private final SkinManager skinManager;
    private final LanguageManager langManager;

    private final HelpMessage helpMsg;

    public SkinCommand(SkinMorph plugin, FavoritesManager favoritesManager, SkinManager skinManager, LanguageManager languageManager)
    {
        this.plugin = plugin;
        this.favoritesManager = favoritesManager;
        this.skinManager = skinManager;
        this.langManager = languageManager;
        helpMsg = new HelpMessage(languageManager);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args)
    {
        final int len = args.length;
        if (!sender.hasPermission("skinmorph.command.skin")) {
            sender.sendMessage(langManager.translate("messages.no-permission"));
            return true;
        }

        if (!isValidUsage(args)) {
            sender.sendMessage(langManager.translate("messages.invalid-usage"));
            return true;
        }

        if (len == 0) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage(langManager.translate("messages.player-only"));
                return true;
            }
            plugin.menu.open(player, plugin.menu.getCurrentPage(player));
            return true;
        }

        switch (args[0]) {
            case "set" -> {
                if (len == 2) {
                    if (!(sender instanceof final Player player)) {
                        sender.sendMessage(langManager.translate("messages.player-only"));
                        return true;
                    }
                    String skinNameInput = args[1];
                    skinManager.setSkinFromName(player, skinNameInput, true);
                } else if (len == 3) {
                    if (Bukkit.getPlayer(args[2]) == null) {
                        sender.sendMessage(langManager.translate("messages.player-not-found", Map.of(
                                "ph1", args[2]
                        )));
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
                        sender.sendMessage(langManager.translate("messages.player-only"));
                        return true;
                    }
                    skinManager.applyRandomSkin(player, true);
                } else if (len == 2) {
                    if (Bukkit.getPlayer(args[1]) == null) {
                        sender.sendMessage(langManager.translate("messages.player-not-found", Map.of(
                                "ph1", args[1]
                        )));
                        return true;
                    }

                    Player target = Bukkit.getPlayer(args[1]);

                    skinManager.applyRandomSkin(sender, target, true);
                }
            }
            case "help" -> {
                int page = 1;

                if (len == 2) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(langManager.translate("messages.invalid-help-page"));
                        return true;
                    }
                }

                helpMsg.sendHelp(sender, page);
            }
            case "default" -> {
                if (len == 1) {
                    if (sender instanceof final Player player) {
                        skinManager.resetSkin(player, true);
                    }
                } else if (len == 2) {
                    if (Bukkit.getPlayer(args[1]) == null) {
                        sender.sendMessage(langManager.translate("messages.player-not-found", Map.of(
                                "ph1", args[1]
                        )));
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
                            sender.sendMessage(langManager.translate("messages.player-only"));
                            return true;
                        }
                        favoritesManager.addFavorite(player, args[2], true);
                    } else if (args[1].equalsIgnoreCase("remove")) {
                        if (!(sender instanceof final Player player)) {
                            sender.sendMessage(langManager.translate("messages.player-only"));
                            return true;
                        }
                        favoritesManager.removeFavorite(player, args[2], true);
                    }
                }
            }
            case "settings" -> {
                if (len == 1) {
                    if (!(sender instanceof final Player player)) {
                        sender.sendMessage(langManager.translate("messages.player-only"));
                        return true;
                    }

                    plugin.menu.openSettings(player);
                }
            }
            case "cache" -> {
                if (len == 2) {
                    if (args[1].equalsIgnoreCase("clear")) {
                        plugin.cacheManager.clearCache();
                        sender.sendMessage(langManager.translate("messages.cache-cleared"));
                    } else if (args[1].equalsIgnoreCase("info")) {
                        sender.sendMessage(langManager.translate("messages.cache-info-header"));
                        sender.sendMessage(langManager.translate("messages.total-skins-cached", Map.of(
                                "ph1", String.valueOf(plugin.cacheManager.getTotalSkinsCachedAmount())
                        )));
                        sender.sendMessage(langManager.translate("messages.cache-size", Map.of(
                                "ph1", plugin.cacheManager.getCacheSize()
                        )));
                    }
                } else if (len == 3) {
                    if (args[1].equalsIgnoreCase("remove")) {
                        String skinNameInput = args[2];

                        boolean wasCached = plugin.cacheManager.removeSkin(skinNameInput);

                        if (wasCached)
                            sender.sendMessage(langManager.translate("messages.skin-removed-from-cache", Map.of(
                                    "ph1", skinNameInput.toLowerCase()
                            )));
                        else
                            sender.sendMessage(langManager.translate("messages.not-cached", Map.of(
                                    "ph1", skinNameInput.toLowerCase()
                            )));
                    }
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

        String action = "";
        if (len > 1)
            action = args[1].toLowerCase();

        switch (sub) {
            case "set":
                // /skin set <skinName>
                // /skin set <skinName> <targetName>
                return len == 2 || len == 3;

            case "default":
                // /skin default
                // /skin default <targetName>
                return len == 1 || len == 2;

            case "favorites":
                if (len < 3) return false;
                return switch (action) {
                    // /skin favorites add <skinName>
                    // /skin favorites remove <skinName>
                    case "add", "remove" -> len == 3;
                    default -> false;
                };

            case "random":
                // /skin random
                // /skin random <targetName>
                return len == 1 || len == 2;

            case "help":
                // /skin help
                // /skin help <page>
                return len == 1 || len == 2;

            case "settings":
                // /skin settings
                return len == 1;

            case "cache":
                if (len < 2) return false;
                return switch (action) {
                    case "clear", "info" -> len == 2;
                    case "remove" -> len == 3;
                    default -> false;
                };

            default:
                return false;
        }
    }
}
