package com.luvoos.skinmorph;

import com.luvoos.skinmorph.command.SkinCommand;
import com.luvoos.skinmorph.command.SkinCommandTabCompleter;
import com.luvoos.skinmorph.event.SkinMorphListener;
import com.luvoos.skinmorph.manager.*;
import com.luvoos.skinmorph.menu.SkinMorphMenu;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;

public final class SkinMorph extends JavaPlugin
{
    public static SkinMorph instance;
    public SkinManager skinManager;
    public FavoritesManager favoritesManager;
    public SelectedSkinsManager selectedSkinsManager;
    public SkinMorphMenu menu;
    public CacheManager cacheManager;
    public InputManager inputManager;
    public LanguageManager langManager;
    public boolean PLAYER_FEEDBACK;
    public boolean DEBUG;
    public boolean SESSION_AGNOSTIC_SKINS;
    public boolean PERSISTENT_CACHE;
    public int RETRY_DELAY;
    public long CACHE_EXPIRY;
    public String LANGUAGE;

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

        LANGUAGE = getConfig().getString("language", "en");
        langManager = new LanguageManager(this, LANGUAGE);
        PLAYER_FEEDBACK = getConfig().getBoolean("player-feedback", true);
        DEBUG = getConfig().getBoolean("debug", false);
        SESSION_AGNOSTIC_SKINS = getConfig().getBoolean("session-agnostic-skins", true);
        PERSISTENT_CACHE = getConfig().getBoolean("persistent-cache", true);
        RETRY_DELAY = getConfig().getInt("retry-delay", 2000);
        CACHE_EXPIRY = getConfig().getLong("cache-expiry-ms", 24 * 60 * 60 * 1000L);

        cacheManager = new CacheManager(Paths.get(getDataPath() + "/cache.json"));
        skinManager = new SkinManager(langManager);
        selectedSkinsManager = new SelectedSkinsManager(this, skinManager);
        selectedSkinsManager.preloadSelectedSkins();
        favoritesManager = new FavoritesManager(this, skinManager, cacheManager, langManager);
        inputManager = new InputManager(this);
        menu = new SkinMorphMenu(this, favoritesManager, skinManager, langManager);

        getCommand("skin").setExecutor(new SkinCommand(this, favoritesManager, skinManager, langManager));
        getCommand("skin").setTabCompleter(new SkinCommandTabCompleter(this, favoritesManager, skinManager));

        getServer().getPluginManager().registerEvents(new SkinMorphListener(this, favoritesManager, skinManager, selectedSkinsManager, langManager), this);
        getServer().getPluginManager().registerEvents(menu, this);
        getServer().getPluginManager().registerEvents(inputManager, this);
    }
}
