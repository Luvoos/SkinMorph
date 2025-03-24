package com.luvoos.skinmorph;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {

    private final SkinMorph plugin;
    private final SkinManager skinManager;
    private File file;
    private FileConfiguration config;

    public FavoritesManager(SkinMorph plugin, SkinManager skinManager)
    {
        this.plugin = plugin;
        this.skinManager = skinManager;
        loadFile();
    }

    private void loadFile()
    {
        file = new File(plugin.getDataFolder(), "favorites.yml");
        if (!file.exists()) plugin.saveResource("favorites.yml", false);
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveFile()
    {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save favorites.yml! -> " + e.getMessage());
        }
    }

    public List<String> getAllFavorites()
    {
        List<String> allFavorites = new ArrayList<>();
        ConfigurationSection playerSection = config.getConfigurationSection("players");
        if (playerSection != null) {
            for (String uuid : playerSection.getKeys(false)) {
                List<String> playerFavorites = config.getStringList("players." + uuid);
                allFavorites.addAll(playerFavorites);
            }
        }

        return allFavorites;
    }

    public List<String> getFavorites(@NotNull Player player)
    {
        final String uuid = player.getUniqueId().toString();
        return config.getStringList("players." + uuid);
    }

    public void addFavorite(@NotNull Player player, @NotNull String skinName, boolean sendResponse)
    {
        final String uuid = player.getUniqueId().toString();
        List<String> favorites = getFavorites(player);
        if (!favorites.contains(skinName.toLowerCase())) {
            favorites.add(skinName.toLowerCase());
            config.set("players." + uuid, favorites);
            saveFile();
        }
        reloadFavorites();
        if (sendResponse && player.isOnline()) player.sendMessage("§aAdded §e" + skinName.toLowerCase() + " §ato your favorites!");
    }

    public void removeFavorite(@NotNull Player player, @NotNull String skinName, boolean sendResponse)
    {
        final String uuid = player.getUniqueId().toString();
        List<String> favorites = getFavorites(player);
        if (favorites.contains(skinName.toLowerCase())) {
            favorites.remove(skinName.toLowerCase());
            config.set("players." + uuid, favorites);
            saveFile();
        }
        reloadFavorites();
        if (sendResponse && player.isOnline()) player.sendMessage("§cRemoved §e" + skinName.toLowerCase() + " §cfrom your favorites!");
    }

    public void preloadFavorites()
    {
        plugin.getLogger().info("Loading favorites...");
        for (String skinName : getAllFavorites()) {
            skinManager.getTexturesAsync(skinName, 3);
        }
        plugin.getLogger().info("Loaded all " + getAllFavorites().size() + " favorites!");
    }

    public void reloadFavorites()
    {
        plugin.reloadConfig();
    }

    public boolean isFavorite(@NotNull Player player, @NotNull String skinName)
    {
        return getFavorites(player).contains(skinName.toLowerCase());
    }
}
