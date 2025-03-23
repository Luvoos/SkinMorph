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
        if (!sender.hasPermission("skinmorph.command.skin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
            return true;
        }

        if (args.length > 3) {
            sender.sendMessage("Usage: /skin (<skinName>) (<targetName>)");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage(ChatColor.RED + "You can only do this as a player!");
                return true;
            }
            try {
                plugin.menu.open(player, 1);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("set")) {
            sender.sendMessage("You need to enter a skin name");
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("Skin Help Message");
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            if (sender instanceof final Player player) {
                plugin.skinManager.resetSkin(player, true);
            }
        }

        if (args.length == 2 && sender.hasPermission("skinmorph.changeskin.self") && args[0].equalsIgnoreCase("set")) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage(ChatColor.RED + "You can only do this as a player!");
                return true;
            }
            String skinNameInput = args[1];
            plugin.skinManager.setSkinFromName(player, skinNameInput, true);
        }

        if (args.length == 3 && sender.hasPermission("skinmorph.changeskin.others")) {
            if (Bukkit.getPlayer(args[2]) == null) {
                sender.sendMessage("§cCouldn't find online player '" + args[2] + "'!");
                return true;
            }

            Player target = Bukkit.getPlayer(args[2]);
            String skinName = args[1];
            plugin.skinManager.setSkinFromName(sender, target, skinName, true);
        }
        return true;
    }
}
