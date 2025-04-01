package com.luvoos.skinmorph;

import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

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

    public void addFavorite(@NotNull Player player, @NotNull String skinName, boolean sendResponse) throws ExecutionException, InterruptedException
    {
        final String uuid = player.getUniqueId().toString();
        List<String> favorites = getFavorites(player);
        String noSpacesSkinName = skinName.replace(" ", "");
        CompletableFuture<Collection<ProfileProperty>> textures = skinManager.getTexturesAsync(skinName, 3);
        if (textures.get() == null) {
            player.sendMessage("§cInvalid profile! ('§e" + noSpacesSkinName.toLowerCase() + "§c')");
            return;
        }
        if (!favorites.contains(noSpacesSkinName.toLowerCase())) {
            favorites.add(noSpacesSkinName.toLowerCase());
            config.set("players." + uuid, favorites);
            saveFile();
        } else {
            if (sendResponse && player.isOnline()) player.sendMessage("§e" + noSpacesSkinName.toLowerCase() + " §ais already in your favorites!");
            return;
        }
        reloadFavorites();
        if (sendResponse && player.isOnline()) player.sendMessage("§aAdded §e" + noSpacesSkinName.toLowerCase() + " §ato your favorites!");
    }

    public void removeFavorite(@NotNull Player player, @NotNull String skinName, boolean sendResponse)
    {
        final String uuid = player.getUniqueId().toString();
        List<String> favorites = getFavorites(player);
        String noSpacesSkinName = skinName.replace(" ", "");
        if (favorites.contains(noSpacesSkinName.toLowerCase())) {
            favorites.remove(noSpacesSkinName.toLowerCase());
            config.set("players." + uuid, favorites);
            saveFile();
        } else {
            if (sendResponse && player.isOnline()) player.sendMessage("§e" + noSpacesSkinName.toLowerCase() + " §cis not in your favorites!");
            return;
        }
        reloadFavorites();
        if (sendResponse && player.isOnline()) player.sendMessage("§cRemoved §e" + noSpacesSkinName.toLowerCase() + " §cfrom your favorites!");
    }

    public String getRandomFavorite(@NotNull Player player)
    {
        final String uuid = player.getUniqueId().toString();
        List<String> favorites = getFavorites(player);
        if (favorites == null || favorites.isEmpty() || favorites.size() < 2) return "";
        List<String> loadedFavorites = new ArrayList<>();
        for (String s : favorites) {
            if (SkinManager.cache.containsKey(s.toLowerCase())) loadedFavorites.add(s);
        }
        return loadedFavorites.get(ThreadLocalRandom.current().nextInt(loadedFavorites.size()));
    }

    public void preloadFavorites()
    {
        plugin.getLogger().info("Loading " + getAllFavorites().size() + " favorites...");
        for (String skinName : getAllFavorites()) {
            skinManager.getTexturesAsync(skinName, 3);
        }
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
