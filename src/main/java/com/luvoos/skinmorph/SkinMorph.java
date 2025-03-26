package com.luvoos.skinmorph;

import com.luvoos.skinmorph.command.SkinCommand;
import com.luvoos.skinmorph.command.SkinMorphTabCompleter;
import com.luvoos.skinmorph.event.EventListener;
import com.luvoos.skinmorph.menu.SkinMorphMenu;
import org.bukkit.plugin.java.JavaPlugin;

public final class SkinMorph extends JavaPlugin {

    public static SkinMorph instance;
    public SkinManager skinManager;
    public FavoritesManager favoritesManager;
    public SkinMorphMenu menu;
    public boolean SEND_RESPONSES;
    public boolean DEBUG;

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
        getLogger().info("SkinMorph was disabled!");
    }

    private void init()
    {
        instance = this;
        saveDefaultConfig();
        skinManager = new SkinManager();
        favoritesManager = new FavoritesManager(this, skinManager);
        favoritesManager.preloadFavorites();
        menu = new SkinMorphMenu(this, favoritesManager, skinManager);
        SEND_RESPONSES = getConfig().getBoolean("sendResponses");
        DEBUG = getConfig().getBoolean("debug");
        getCommand("skin").setExecutor(new SkinCommand());
        getCommand("skin").setTabCompleter(new SkinMorphTabCompleter());
        getServer().getPluginManager().registerEvents(menu, this);
        getServer().getPluginManager().registerEvents(new EventListener(this, favoritesManager, skinManager), this);
    }
}
