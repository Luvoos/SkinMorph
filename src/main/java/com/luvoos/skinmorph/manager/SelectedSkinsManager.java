package com.luvoos.skinmorph.manager;

import com.luvoos.skinmorph.SkinMorph;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectedSkinsManager
{
    private final SkinMorph plugin;
    private final SkinManager skinManager;
    private File file;
    private FileConfiguration config;

    public SelectedSkinsManager(SkinMorph plugin, SkinManager skinManager)
    {
        this.plugin = plugin;
        this.skinManager = skinManager;
        loadFile();
    }

    private void loadFile()
    {
        file = new File(plugin.getDataFolder(), "skins.yml");
        if (!file.exists())
            plugin.saveResource("skins.yml", false);
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveFile()
    {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save skins.yml! -> " + e.getMessage());
        }
    }

    public void addPlayer(@NotNull Player player)
    {
        final String uuid = player.getUniqueId().toString();

        if (!config.contains("players." + uuid)) {
            config.set("players." + uuid, player.getName().toLowerCase());
            saveFile();
            reloadSelectedSkins();
        }
    }

    public List<String> getAllSelectedSkins()
    {
        List<String> allSelectedSkins = new ArrayList<>();
        ConfigurationSection playerSection = config.getConfigurationSection("players");

        if (playerSection != null) {
            for (String uuid : playerSection.getKeys(false)) {
                String selectedSkin = config.getString("players." + uuid);
                allSelectedSkins.add(selectedSkin);
            }
        }

        return allSelectedSkins;
    }

    public String getSelectedSkin(@NotNull Player player)
    {
        final String uuid = player.getUniqueId().toString();
        final String selectedSkin = config.getString("players." + uuid);

        return (selectedSkin == null || selectedSkin.isEmpty()) ? "" : selectedSkin;
    }

    public void setSelectedSkin(@NotNull Player player, @NotNull String skinName)
    {
        final String uuid = player.getUniqueId().toString();
        String noSpacesSkinName = skinName.replace(" ", "");

        config.set("players." + uuid, noSpacesSkinName.toLowerCase());
        saveFile();
        reloadSelectedSkins();
    }

    public void preloadSelectedSkins()
    {
        plugin.getLogger().info("Loading " + getAllSelectedSkins().size() + " selected skins...");

        for (String skinName : getAllSelectedSkins()) {
            skinManager.getSkinAsync(skinName, 3);
        }
    }

    public void reloadSelectedSkins()
    {
        plugin.reloadConfig();
    }
}
