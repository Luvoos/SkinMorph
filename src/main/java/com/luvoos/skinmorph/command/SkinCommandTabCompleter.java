package com.luvoos.skinmorph.command;

import com.luvoos.skinmorph.SkinMorph;
import com.luvoos.skinmorph.manager.FavoritesManager;
import com.luvoos.skinmorph.manager.SkinManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SkinCommandTabCompleter implements TabCompleter
{
    private final SkinMorph plugin;
    private final FavoritesManager favoritesManager;
    private final SkinManager skinManager;

    public SkinCommandTabCompleter(SkinMorph plugin, FavoritesManager favoritesManager, SkinManager skinManager)
    {
        this.plugin = plugin;
        this.favoritesManager = favoritesManager;
        this.skinManager = skinManager;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args)
    {
        List<String> completions = new ArrayList<>();
        final int len = args.length;

        if (len == 1) {
            completions.addAll(List.of("set", "default", "help", "favorites", "random", "settings", "cache", "update"));
        } else if (len == 2) {
            switch (args[0].toLowerCase()) {
                case "set" -> {
                    if (sender instanceof final Player player) {
                        completions.addAll(getOnlinePlayerNames());
                        completions.addAll(favoritesManager.getFavorites(player));
                    }
                }
                case "random", "default", "update" -> completions.addAll(getOnlinePlayerNames());
                case "favorites" -> completions.addAll(List.of("add", "remove"));
                case "help" -> completions.addAll(List.of(String.valueOf(1), String.valueOf(2), String.valueOf(3)));
                case "cache" -> completions.addAll(List.of("clear", "remove", "info"));
            }
        } else if (len == 3 && args[0].equalsIgnoreCase("set")) {
            completions.addAll(getOnlinePlayerNames());
        } else if (len == 3 && args[0].equalsIgnoreCase("favorites")) {
            if (args[1].equalsIgnoreCase("add")) {
                completions.addAll(getOnlinePlayerNames());
                if (sender instanceof final Player player) {
                    String currentSkin = skinManager.getCurrentSkin(player);
                    if (!favoritesManager.isFavorite(player, currentSkin)) {
                        completions.add(currentSkin);
                    }
                }
            } else if (args[1].equalsIgnoreCase("remove") && sender instanceof final Player player) {
                completions.addAll(favoritesManager.getFavorites(player));
            }
        } else if (len == 3 && args[0].equalsIgnoreCase("cache") && args[1].equalsIgnoreCase("remove")) {
            if (sender instanceof final Player player) {
                completions.addAll(favoritesManager.getFavorites(player));
            }
        }

        return StringUtil.copyPartialMatches(
                args[len - 1],
                completions,
                new ArrayList<>()
        );
    }

    private List<String> getOnlinePlayerNames()
    {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toList();
    }
}
