package com.luvoos.skinmorph.command;

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

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args)
    {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("set"); // /skin set
            completions.add("reset"); // /skin reset
            completions.add("help"); // /skin help
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) { // /skin set SELF
            if (sender instanceof final Player player) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    completions.add(onlinePlayer.getName());
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("help")) { // /skin help

        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) { // /skin set OTHER
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                completions.add(onlinePlayer.getName());
            }
        }

        return completions;
    }
}
