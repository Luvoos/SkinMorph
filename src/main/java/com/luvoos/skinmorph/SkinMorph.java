package com.luvoos.skinmorph;

import com.luvoos.skinmorph.command.SkinCommand;
import com.luvoos.skinmorph.command.SkinMorphTabCompleter;
import com.luvoos.skinmorph.event.EventListener;
import com.luvoos.skinmorph.menu.SkinMorphMenu;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkinMorph extends JavaPlugin {

    public static SkinMorph instance;
    public SkinManager skinManager;
    public FavoritesManager favoritesManager;
    public SelectedSkinsManager selectedSkinsManager;
    public SkinMorphMenu menu;
    public boolean SEND_RESPONSES;
    public boolean DEBUG;
    public boolean SESSION_AGNOSTIC_SKINS;

    @Override
    public void onEnable()
    {
        try {
            init();
        } catch (Exception e) {
            getLogger().warning("Something went wrong: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("SkinMorph was enabled!");
    }

    @Override
    public void onDisable()
    {
        getLogger().info("SkinMorph was disabled! Goodbye!");
    }

    private void init()
    {
        instance = this;
        saveDefaultConfig();
        skinManager = new SkinManager();
        selectedSkinsManager = new SelectedSkinsManager(this, skinManager);
        selectedSkinsManager.preloadSelectedSkins();
        favoritesManager = new FavoritesManager(this, skinManager);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            favoritesManager.preloadFavorites();
        }, 60L); // load favorites 3 seconds later to make sure selected skins are fully loaded first
        menu = new SkinMorphMenu(this, favoritesManager, skinManager);
        SEND_RESPONSES = getConfig().getBoolean("sendResponses");
        DEBUG = getConfig().getBoolean("debug");
        SESSION_AGNOSTIC_SKINS = getConfig().getBoolean("sessionAgnosticSkins");
        getCommand("skin").setExecutor(new SkinCommand());
        getCommand("skin").setTabCompleter(new SkinMorphTabCompleter(this));
        getServer().getPluginManager().registerEvents(menu, this);
        getServer().getPluginManager().registerEvents(new EventListener(this, favoritesManager, skinManager, selectedSkinsManager), this);
    }
}
