package com.luvoos.skinmorph.manager;

import com.luvoos.skinmorph.SkinData;
import com.luvoos.skinmorph.SkinMorph;
import com.luvoos.skinmorph.util.FormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class FavoritesManager
{
    private final SkinMorph plugin;
    private final SkinManager skinManager;
    private final CacheManager cacheManager;
    private final LanguageManager langManager;
    private File file;
    private FileConfiguration config;

    public FavoritesManager(SkinMorph plugin, SkinManager skinManager, CacheManager cacheManager, LanguageManager languageManager)
    {
        this.plugin = plugin;
        this.skinManager = skinManager;
        this.cacheManager = cacheManager;
        this.langManager = languageManager;
        loadFile();
    }

    private void loadFile()
    {
        file = new File(plugin.getDataFolder(), "favorites.yml");
        if (!file.exists())
            plugin.saveResource("favorites.yml", false);
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

    public void addFavorite(@NotNull Player player, @NotNull String skinName, boolean playerFeedback)
    {
        final String uuid = player.getUniqueId().toString();
        final String normalizedSkinName = FormatUtil.normalize(skinName);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<String> favorites = getFavorites(player);
                CompletableFuture<SkinData> skin = skinManager.getSkinAsync(normalizedSkinName, 3);
                if (skin.get() == null) {
                    if (plugin.PLAYER_FEEDBACK && playerFeedback)
                        player.sendMessage(langManager.translate("messages.invalid-profile", Map.of(
                                "ph1", normalizedSkinName
                        )));
                    return;
                }

                if (!favorites.contains(normalizedSkinName)) {
                    favorites.add(normalizedSkinName);
                    config.set("players." + uuid, favorites);
                    saveFile();
                } else {
                    if (plugin.PLAYER_FEEDBACK && playerFeedback && player.isOnline())
                        player.sendMessage(langManager.translate("messages.already-in-favorites", Map.of(
                                "ph1", normalizedSkinName
                        )));
                    return;
                }
                reloadFavorites();
                if (plugin.PLAYER_FEEDBACK && playerFeedback && player.isOnline())
                    player.sendMessage(langManager.translate("messages.added-to-favorites", Map.of(
                            "ph1", normalizedSkinName
                    )));
            } catch (Exception e) {
                if (plugin.DEBUG)
                    plugin.getLogger().warning("Failed to save favorite '" + normalizedSkinName + "' -> " + e.getMessage());
            }
        });
    }

    public void removeFavorite(@NotNull Player player, @NotNull String skinName, boolean playerFeedback)
    {
        final String uuid = player.getUniqueId().toString();
        List<String> favorites = getFavorites(player);
        final String normalizedSkinName = FormatUtil.normalize(skinName);

        if (favorites.contains(normalizedSkinName)) {
            favorites.remove(normalizedSkinName);
            config.set("players." + uuid, favorites);
            saveFile();
        } else {
            if (plugin.PLAYER_FEEDBACK && playerFeedback && player.isOnline())
                player.sendMessage(langManager.translate("messages.not-in-favorites", Map.of(
                        "ph1", normalizedSkinName
                )));
            return;
        }

        reloadFavorites();

        if (plugin.PLAYER_FEEDBACK && playerFeedback && player.isOnline())
            player.sendMessage(langManager.translate("messages.removed-from-favorites", Map.of(
                    "ph1", normalizedSkinName
            )));
    }

    public String getRandomFavorite(@NotNull Player player)
    {
        final String uuid = player.getUniqueId().toString();
        List<String> favorites = getFavorites(player);

        if (favorites == null || favorites.isEmpty() || favorites.size() < 2) return "";

        List<String> loadedFavorites = new ArrayList<>();

        for (String favorite : favorites) {
            if (cacheManager.hasSkin(favorite))
                loadedFavorites.add(favorite);
        }

        String chosenFavorite;
        do
            chosenFavorite = loadedFavorites.get(ThreadLocalRandom.current().nextInt(loadedFavorites.size()));
        while
            (skinManager.isWearingSkin(player, chosenFavorite) && loadedFavorites.size() > 1);

        return chosenFavorite;
    }

    public void preloadAllFavorites()
    {
        plugin.getLogger().info("Loading " + getAllFavorites().size() + " favorite(s)...");
        for (String skinName : getAllFavorites()) {
            skinManager.getSkinAsync(skinName, 5);
        }
    }

    public void preloadFavorites(@NotNull Player player)
    {
        plugin.getLogger().info("Loading " + getAllFavorites().size() + " favorite(s) for " + player.getName());
        for (String skinName : getFavorites(player)) {
            skinManager.getSkinAsync(skinName, 5);
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
