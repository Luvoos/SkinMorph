package com.luvoos.skinmorph.event;

import com.luvoos.skinmorph.FavoritesManager;
import com.luvoos.skinmorph.SkinManager;
import com.luvoos.skinmorph.SkinMorph;
import com.luvoos.skinmorph.SkinMorphInputType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {

    private final SkinMorph plugin;
    private final FavoritesManager favoritesManager;
    private final SkinManager skinManager;

    public EventListener(SkinMorph plugin, FavoritesManager favoritesManager, SkinManager skinManager)
    {
        this.plugin = plugin;
        this.favoritesManager = favoritesManager;
        this.skinManager = skinManager;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();

        skinManager.getTexturesAsync(player.getName().toLowerCase(), 3); // Add the player's textures to cache
        favoritesManager.addFavorite(player, "Notch", false);

        skinManager.setCurrentSkin(player, player.getName());
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event)
    {
        final Player player = event.getPlayer();
        if (plugin.menu.awaitingInput.containsKey(player.getUniqueId())) plugin.menu.awaitingInput.remove(player.getUniqueId());
        SkinManager.currentSkins.remove(player.getUniqueId());
    }

    @EventHandler
    public void onChat(final AsyncPlayerChatEvent event)
    {
        final Player player = event.getPlayer();
        final String text = event.getMessage();

        if (plugin.menu.awaitingInput.containsKey(player.getUniqueId()) && plugin.menu.awaitingInput.get(player.getUniqueId()) == SkinMorphInputType.CHANGE_SKIN_SELF) {
            event.setCancelled(true);
            skinManager.setSkinFromName(player, text, true);
            plugin.menu.awaitingInput.remove(player.getUniqueId());
        }

        if (plugin.menu.awaitingInput.containsKey(player.getUniqueId()) && plugin.menu.awaitingInput.get(player.getUniqueId()) == SkinMorphInputType.ADD_FAVORITE) {
            event.setCancelled(true);
            favoritesManager.addFavorite(player, text, true);
            plugin.menu.awaitingInput.remove(player.getUniqueId());
        }
    }
}