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
    private static final String pageSwitcherBase64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDA4OWQwZTBjODhlNTI1MDAzOGU2NTlmNTA4ZWJkYTViNjVhNWJlMGJmYzQxMDNkMGUxMmZhYmE0MDE4ZDVjYiJ9fX0=";

    public static CompletableFuture<ItemStack> favoriteSkinHead(String name)
    {
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
        }).thenCompose(f -> f);
    }

    public static ItemStack changeSkin(Player player)
    {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(player.getUniqueId(), player.getName());
        meta.setPlayerProfile(profile);
        meta.setDisplayName("§eChange skin");
        List<String> lore = new ArrayList<>();
        lore.add("§r§f§lLEFT CLICK §r§afor yourself§r");
        lore.add("§r§f§lRIGHT CLICK §r§afor another player§r");
        lore.add("§r§7Your current skin: §e" + plugin.skinManager.getCurrentSkin(player) + ((player.getName().equalsIgnoreCase(plugin.skinManager.getCurrentSkin(player))) ? " (default)" : ""));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack randomSkin(Player player)
    {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM0ZTA2M2NhZmI0NjdhNWM4ZGU0M2VjNzg2MTkzOTlmMzY5ZjRhNTI0MzRkYTgwMTdhOTgzY2RkOTI1MTZhMCJ9fX0="));
        meta.setPlayerProfile(profile);
        meta.setDisplayName("§eRandom skin");
        List<String> lore = new ArrayList<>();
        lore.add("§r§7Randomly choose a favorite");
        lore.add("§r§f§lLEFT CLICK §r§afor yourself§r");
        lore.add("§r§f§lRIGHT CLICK §r§afor another player§r");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack resetSkin(Player player)
    {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eReset skin");
        List<String> lore = new ArrayList<>();
        lore.add("§r§f§lLEFT CLICK §r§afor yourself§r");
        lore.add("§r§f§lRIGHT CLICK §r§afor another player§r");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack manageFavorites()
    {
        ItemStack item = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eManage favorites");
        List<String> lore = new ArrayList<>();
        lore.add("§r§f§lLEFT CLICK §r§ato add favorite");
        lore.add("§r§f§lRIGHT CLICK §r§cto remove favorite");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack previousPage()
    {
        return headFromBase64("§r§ePrevious Page", pageSwitcherBase64);
    }

    public static ItemStack nextPage()
    {
        return headFromBase64("§r§eNext Page", pageSwitcherBase64);
    }

    public static ItemStack headFromBase64(String displayName, String base64)
    {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", base64));
        meta.setPlayerProfile(profile);
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);

        return item;
    }
}
