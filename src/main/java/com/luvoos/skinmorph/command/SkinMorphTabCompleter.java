package com.luvoos.skinmorph.command;

import com.luvoos.skinmorph.SkinMorph;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SkinMorphTabCompleter implements TabCompleter {

    private final SkinMorph plugin;

    public SkinMorphTabCompleter(SkinMorph plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args)
    {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("set"); // /skin set
            completions.add("reset"); // /skin reset
            completions.add("help"); // /skin help
            completions.add("favorites"); // /skin favorites add/remove
            completions.add("random"); // /skin random
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) { // /skin set SELF
            if (sender instanceof final Player player) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    completions.add(onlinePlayer.getName());
                }
                List<String> favorites = plugin.favoritesManager.getFavorites(player);
                if (favorites != null && !favorites.isEmpty()) completions.addAll(favorites);
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("random")) {

        } else if (args.length == 2 && args[0].equalsIgnoreCase("favorites")) {
            completions.add("add");
            completions.add("remove");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("favorites")) {
            if (args[1].equalsIgnoreCase("add")) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) completions.add(onlinePlayer.getName());
                if (sender instanceof final Player player) {
                    boolean containsCurrentSkin = completions.stream().anyMatch(s -> s.equalsIgnoreCase(plugin.skinManager.getCurrentSkin(player)));
                    if (!plugin.favoritesManager.isFavorite(player, plugin.skinManager.getCurrentSkin(player)) && !containsCurrentSkin) {
                        completions.add(plugin.skinManager.getCurrentSkin(player));
                    }
                }
            }
            if (args[1].equalsIgnoreCase("remove")) {
                if (sender instanceof final Player player) {
                    List<String> favorites = plugin.favoritesManager.getFavorites(player);
                    if (favorites != null && !favorites.isEmpty()) completions.addAll(favorites);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) completions.add(onlinePlayer.getName());
        }

        return completions;
    }
}
