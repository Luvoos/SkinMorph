package com.luvoos.skinmorph.event;

import com.luvoos.skinmorph.*;
import com.luvoos.skinmorph.data.ChangeSkinOthersRequest;
import com.luvoos.skinmorph.data.SkinMorphInputType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class SkinMorphListener implements Listener {

    private final SkinMorph plugin;
    private final FavoritesManager favoritesManager;
    private final SkinManager skinManager;
    private final SelectedSkinsManager selectedSkinsManager;
    public static final Map<UUID, ChangeSkinOthersRequest> csoReqs = new ConcurrentHashMap<>();

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

        if (plugin.menu.awaitingInput.containsKey(player.getUniqueId()) && plugin.menu.awaitingInput.get(player.getUniqueId()) == SkinMorphInputType.CHANGE_SKIN_OTHERS) {

        }
    }
}