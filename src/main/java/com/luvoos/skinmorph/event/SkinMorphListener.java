package com.luvoos.skinmorph.event;

import com.luvoos.skinmorph.SkinMorph;
import com.luvoos.skinmorph.manager.FavoritesManager;
import com.luvoos.skinmorph.manager.LanguageManager;
import com.luvoos.skinmorph.manager.SelectedSkinsManager;
import com.luvoos.skinmorph.manager.SkinManager;
import com.luvoos.skinmorph.type.SkinMorphMenuType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SkinMorphListener implements Listener
{
    private final SkinMorph plugin;
    private final FavoritesManager favoritesManager;
    private final SkinManager skinManager;
    private final SelectedSkinsManager selectedSkinsManager;
    private final LanguageManager langManager;

    public SkinMorphListener(SkinMorph plugin, FavoritesManager favoritesManager, SkinManager skinManager, SelectedSkinsManager selectedSkinsManager, LanguageManager languageManager)
    {
        this.plugin = plugin;
        this.favoritesManager = favoritesManager;
        this.skinManager = skinManager;
        this.selectedSkinsManager = selectedSkinsManager;
        this.langManager = languageManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();

        selectedSkinsManager.addPlayer(player);

        skinManager.getSkinAsync(player.getName(), 3); // Add the player's textures to cache

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
        plugin.menu.setCurrentMenuType(player, SkinMorphMenuType.MAIN);
        plugin.menu.setCurrentPage(player, 1);
        SkinManager.currentSkins.remove(player.getUniqueId());
    }
}