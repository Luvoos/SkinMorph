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
            sender.sendMessage("§cYou don't have permission to do that!");
            return true;
        }

        if (args.length > 3) {
            sender.sendMessage("Usage: /skin (<skinName>) (<targetName>)");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage("§cYou can only do this as a player!");
                return true;
            }
            try {
                plugin.menu.open(player, 1);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("set")) {
            sender.sendMessage("§cYou need to enter a skin name!");
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("random")) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage(ChatColor.RED + "You can only do this as a player!");
                return true;
            }
            plugin.skinManager.applyRandomSkin(player, true);
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
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

        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            if (sender instanceof final Player player) {
                plugin.skinManager.resetSkin(player, true);
            }
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            if (Bukkit.getPlayer(args[1]) == null) {
                sender.sendMessage("§cCouldn't find online player '" + args[1] + "'!");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            plugin.skinManager.resetSkin(sender, target, true);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("set") && sender.hasPermission("skinmorph.changeskin.self")) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage(ChatColor.RED + "You can only do this as a player!");
                return true;
            }
            String skinNameInput = args[1];
            plugin.skinManager.setSkinFromName(player, skinNameInput, true);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set") && sender.hasPermission("skinmorph.changeskin.others")) {
            if (Bukkit.getPlayer(args[2]) == null) {
                sender.sendMessage("§cCouldn't find online player '" + args[2] + "'!");
                return true;
            }

            Player target = Bukkit.getPlayer(args[2]);
            String skinName = args[1];
            plugin.skinManager.setSkinFromName(sender, target, skinName, true);
        }

        if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("favorites")) {

        }

        if (args.length == 3 && args[0].equalsIgnoreCase("favorites") && args[1].equalsIgnoreCase("add")) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage(ChatColor.RED + "You can only do this as a player!");
                return true;
            }
            plugin.favoritesManager.addFavorite(player, args[2], true);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("favorites") && args[1].equalsIgnoreCase("remove")) {
            if (!(sender instanceof final Player player)) {
                sender.sendMessage(ChatColor.RED + "You can only do this as a player!");
                return true;
            }
            plugin.favoritesManager.removeFavorite(player, args[2], true);
        }

        return true;
        }
}
