package com.luvoos.skinmorph;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PersistentCacheManager
{
    private final Path cacheFile;
    public final Map<String, SkinData> cache = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final long expiryMillis = 7 * 24 * 60 * 60 * 1000L; // 1 week expiry time

    public PersistentCacheManager(Path cacheFile)
    {
        this.cacheFile = cacheFile;
        loadCache();
    }

    // Load persistent cache from JSON file
    private void loadCache()
    {
        if (!Files.exists(cacheFile)) return;

        try (Reader reader = Files.newBufferedReader(cacheFile)) {
            Type type = new TypeToken<Map<String, SkinData>>(){}.getType();
            Map<String, SkinData> loaded = gson.fromJson(reader, type);
            if (loaded != null) cache.putAll(loaded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save persistent cache to JSON file
    public void saveCache()
    {
        try (Writer writer = Files.newBufferedWriter(cacheFile)) {
            gson.toJson(cache, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get a PlayerProfile from persistent cache
    public PlayerProfile getProfile(String username)
    {
        SkinData skinData = cache.get(username.toLowerCase());
        if (skinData == null) return null;

        // Expiry check
        if (System.currentTimeMillis() - skinData.getLastUpdated() > expiryMillis) return null;

        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), skinData.getUsername());
        profile.setProperties(skinData.getTextureProperties());

        return profile;
    }

    // Update persistent cache with new skin data
    public void updateCache(String username, PlayerProfile profile)
    {
        ProfileProperty property = profile.getProperties().stream()
                .filter(p -> p.getName().equals("textures"))
                .findFirst()
                .orElse(null);

        if (property == null) return;

        SkinData skinData = new SkinData(
                username.toLowerCase(),
                List.of(property),
                System.currentTimeMillis()
        );

        cache.put(username.toLowerCase(), skinData);
        saveCache();
    }
}
