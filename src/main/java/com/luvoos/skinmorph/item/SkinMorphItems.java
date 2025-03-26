package com.luvoos.skinmorph.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.luvoos.skinmorph.SkinManager;
import com.luvoos.skinmorph.SkinMorph;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkinMorphItems {

    private static final SkinMorph plugin = SkinMorph.instance;

    public static CompletableFuture<ItemStack> playerHead(String name) {
        return CompletableFuture.supplyAsync(() -> {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), name);

            Collection<ProfileProperty> cachedProperties = SkinManager.cache.get(name);

            CompletableFuture<Collection<ProfileProperty>> propertiesFuture =
                    cachedProperties != null
                            ? CompletableFuture.completedFuture(cachedProperties)
                            : plugin.skinManager.getTexturesAsync(name, 3);

            return propertiesFuture.thenApply(properties -> {
                if (properties != null) profile.setProperties(properties);
                meta.setPlayerProfile(profile);
                meta.setDisplayName("§r§a" + name.toLowerCase());
                meta.setLore(List.of(
                        "§r§f§lLEFT CLICK §r§ato apply skin§r",
                        "§r§f§lRIGHT CLICK §r§cto remove skin§r"
                ));
                item.setItemMeta(meta);
                return item;
            });
        }).thenCompose(f -> f); // Flatten nested CompletableFuture
    }


    public static ItemStack changeSkin(Player player)
    {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(player.getUniqueId(), player.getName());
        meta.setPlayerProfile(profile);
        meta.setDisplayName("§eChange skin");
        List<String> lore = new ArrayList<>();
        lore.add("§r§7Your current skin: §e" + plugin.skinManager.getCurrentSkin(player));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack resetSkin(Player player)
    {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eReset skin");
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack addFavorite() {
        ItemStack item = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eAdd favorite");
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack previousPage()
    {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§ePrevious Page");
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack nextPage()
    {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eNext Page");
        item.setItemMeta(meta);
        return item;
    }
}
