package com.luvoos.skinmorph.manager;

import com.luvoos.skinmorph.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

public class LanguageManager
{
    private final FileConfiguration config;

    public LanguageManager(JavaPlugin plugin, String lang)
    {
        final File langFile = new File(plugin.getDataFolder() + "/lang/" + lang + ".yml");

        if (!langFile.exists()) {
            plugin.saveResource("lang/" + lang + ".yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(langFile);
    }

    public Component translate(String key, Map<String, String> placeholders)
    {
        String msg = config.getString(key, key);

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return ColorUtil.colorize("<!i>" + msg);
    }

    public Component translate(String key)
    {
        return translate(key, Map.of());
    }
}
