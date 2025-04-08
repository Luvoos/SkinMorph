package com.luvoos.skinmorph.event;

import com.luvoos.skinmorph.*;
import com.luvoos.skinmorph.type.SkinMorphInputType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ExecutionException;

public class SkinMorphListener implements Listener {

    private final SkinMorph plugin;
    private final FavoritesManager favoritesManager;
    private final SkinManager skinManager;
    private final SelectedSkinsManager selectedSkinsManager;

    public SkinMorphListener(SkinMorph plugin, FavoritesManager favoritesManager, SkinManager skinManager, SelectedSkinsManager selectedSkinsManager)
    {
        this.plugin = plugin;
        this.favoritesManager = favoritesManager;
        this.skinManager = skinManager;
        this.selectedSkinsManager = selectedSkinsManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();

        selectedSkinsManager.addPlayer(player);

        skinManager.getTexturesAsync(player.getName(), 3); // Add the player's textures to cache

        favoritesManager.preloadFavorites(player);

        if (plugin.SESSION_AGNOSTIC_SKINS) {
            skinManager.setSkinFromName(player, selectedSkinsManager.getSelectedSkin(player), false);
        } else {
            skinManager.setCurrentSkin(player, player.getName());
        }
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event)
    {
        final Player player = event.getPlayer();
        if (plugin.menu.awaitingInput.containsKey(player.getUniqueId())) plugin.menu.awaitingInput.remove(player.getUniqueId());
        SkinManager.currentSkins.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(final AsyncPlayerChatEvent event) throws ExecutionException, InterruptedException
    {
        final Player player = event.getPlayer();
        final String text = event.getMessage();

        if (plugin.menu.awaitingInput.containsKey(player.getUniqueId()) && plugin.menu.awaitingInput.get(player.getUniqueId()) == SkinMorphInputType.CHANGE_SKIN_SELF) {
            event.setCancelled(true);
            skinManager.setSkinFromName(player, text, true);
            plugin.menu.awaitingInput.remove(player.getUniqueId());
        }

        if (plugin.menu.awaitingInput.containsKey(player.getUniqueId()) && plugin.menu.awaitingInput.get(player.getUniqueId()) == SkinMorphInputType.CHANGE_SKIN_OTHERS) {
            event.setCancelled(true);
            String[] parts = text.split(";", 2);
            String first = parts[0];
            String second = parts.length > 1 ? parts[1] : "";
            if (Bukkit.getPlayer(second.replace(" ", "").toLowerCase()) == null) {
                player.sendMessage("§cCouldn't find online player '" + second + "'!");
                plugin.menu.awaitingInput.remove(player.getUniqueId());
                return;
            }
            skinManager.setSkinFromName(player, Bukkit.getPlayer(second.replace(" ", "").toLowerCase()), first, true);
            plugin.menu.awaitingInput.remove(player.getUniqueId());
        }

        if (plugin.menu.awaitingInput.containsKey(player.getUniqueId()) && plugin.menu.awaitingInput.get(player.getUniqueId()) == SkinMorphInputType.ADD_FAVORITE) {
            event.setCancelled(true);
            favoritesManager.addFavorite(player, text, true);
            plugin.menu.awaitingInput.remove(player.getUniqueId());
        }

        if (plugin.menu.awaitingInput.containsKey(player.getUniqueId()) && plugin.menu.awaitingInput.get(player.getUniqueId()) == SkinMorphInputType.REMOVE_FAVORITE) {
            event.setCancelled(true);
            favoritesManager.removeFavorite(player, text, true);
            plugin.menu.awaitingInput.remove(player.getUniqueId());
        }

        if (plugin.menu.awaitingInput.containsKey(player.getUniqueId()) && plugin.menu.awaitingInput.get(player.getUniqueId()) == SkinMorphInputType.RESET_SKIN_OTHERS) {
            event.setCancelled(true);
            String normalizedText = text.replace(" ", "").toLowerCase();
            if (Bukkit.getPlayer(normalizedText) == null) {
                player.sendMessage("§cCouldn't find online player '" + normalizedText + "'!");
                plugin.menu.awaitingInput.remove(player.getUniqueId());
                return;
            }
            skinManager.resetSkin(player, Bukkit.getPlayer(normalizedText), true);
            plugin.menu.awaitingInput.remove(player.getUniqueId());
        }

        if (plugin.menu.awaitingInput.containsKey(player.getUniqueId()) && plugin.menu.awaitingInput.get(player.getUniqueId()) == SkinMorphInputType.RANDOM_SKIN_OTHERS) {
            event.setCancelled(true);
            String normalizedText = text.replace(" ", "").toLowerCase();
            if (Bukkit.getPlayer(normalizedText) == null) {
                player.sendMessage("§cCouldn't find online player '" + normalizedText + "'!");
                plugin.menu.awaitingInput.remove(player.getUniqueId());
                return;
            }
            skinManager.applyRandomSkin(player, Bukkit.getPlayer(normalizedText), true);
            plugin.menu.awaitingInput.remove(player.getUniqueId());
        }
    }
}