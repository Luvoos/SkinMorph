package com.luvoos.skinmorph;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager
{
    private final SkinMorph plugin = SkinMorph.instance;
    private final Path cacheFile;
    public static final Map<String, SkinData> cache = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private long expiryMs;

    public CacheManager(Path cacheFile)
    {
        this.cacheFile = cacheFile;
        init();
    }

    private void init()
    {
        expiryMs = plugin.CACHE_EXPIRY;
        loadCache();
    }

    // Load persistent cache from JSON file
    private void loadCache()
    {
        if (!Files.exists(cacheFile)) return;

        try (Reader reader = Files.newBufferedReader(cacheFile)) {
            Type type = new TypeToken<Map<String, SkinData>>(){}.getType();
            Map<String, SkinData> loaded = gson.fromJson(reader, type);
            if (loaded != null)
                cache.putAll(loaded);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load cache into memory: " + e.getMessage());
        }
    }

    // Save persistent cache to JSON file
    public void saveCache()
    {
        try (Writer writer = Files.newBufferedWriter(cacheFile)) {
            gson.toJson(cache, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save cache to cache.json: " + e.getMessage());
        }
    }

    // Get a skin from persistent cache
    public SkinData getSkin(String skinName)
    {
        SkinData skinData = cache.get(skinName.toLowerCase());

        if (skinData == null) return null;

        // Expiry check
        if (skinData.isDirty(expiryMs)) {
            plugin.getLogger().info("SkinData expired for '" + skinName.toLowerCase() + "', returning null");
            return null;
        }

       return skinData;
    }

    // Update persistent cache with new skin data
    public void updateCache(SkinData skinData)
    {
        cache.put(skinData.getUsername().toLowerCase(), skinData);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveCache);
    }

    public boolean hasSkin(String skinName)
    {
        return cache.containsKey(skinName.toLowerCase());
    }
}
