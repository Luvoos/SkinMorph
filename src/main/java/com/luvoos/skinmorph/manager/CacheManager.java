package com.luvoos.skinmorph.manager;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.luvoos.skinmorph.SkinData;
import com.luvoos.skinmorph.SkinMorph;
import com.luvoos.skinmorph.util.FileUtil;
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
    private final Map<String, SkinData> cache = new ConcurrentHashMap<>();
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

        // schedule automatic cache cleanups
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> cleanupCache(true),
                20 * 60 * 10L, 20 * 60 * 10L);
    }

    // Load cache from JSON file
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

    // Save cache to JSON file
    public void saveCache()
    {
        try (Writer writer = Files.newBufferedWriter(cacheFile)) {
            gson.toJson(cache, writer);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save cache to cache.json: " + e.getMessage());
        }
    }

    // Get a skin from cache
    public SkinData getSkin(String skinName)
    {
        SkinData skinData = cache.get(skinName.toLowerCase());

        if (skinData == null) return null;

        // Expiry check
        if (skinData.isDirty(expiryMs)) {
            if (plugin.DEBUG)
                plugin.getLogger().info("SkinData expired for '" + skinName.toLowerCase() + "', returning null");
            return null;
        }

       return skinData;
    }

    // Update cache with new skin data
    public void addSkin(SkinData skinData)
    {
        cache.put(skinData.getUsername().toLowerCase(), skinData);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveCache);
    }

    public boolean removeSkin(String skinName)
    {
        boolean cached = cache.containsKey(skinName.toLowerCase());
        cache.remove(skinName.toLowerCase());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveCache);
        return cached;
    }

    public void cleanupCache(boolean async)
    {
        if (plugin.DEBUG)
            plugin.getLogger().info("Performing automatic cache cleanup " + (async ? "(async)" : "(sync)") + "!");

        cache.entrySet().removeIf(entry -> entry.getValue().isDirty(expiryMs));

        if (async && plugin.isEnabled()) {
            // standard asynchronous cleanup during runtime
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveCache);
        } else {
            // synchronous save during shutdown
            saveCache();
        }
    }

    public void clearCache()
    {
        cache.clear();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveCache);
    }

    public int getTotalSkinsCachedAmount()
    {
        return cache.size();
    }

    public String getCacheSize()
    {
        if (!Files.exists(cacheFile))
            return "0 B";

        try {
            long bytes = Files.size(cacheFile);
            return FileUtil.humanReadableByteCount(bytes);
        } catch (IOException e) {
            if (plugin.DEBUG)
                plugin.getLogger().warning("Failed to get cache size -> " + e.getMessage());
            return "Unknown";
        }
    }

    public boolean hasSkin(String skinName)
    {
        return cache.containsKey(skinName.toLowerCase());
    }
}
