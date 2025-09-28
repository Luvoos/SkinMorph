package com.luvoos.skinmorph.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.luvoos.skinmorph.SkinData;
import com.luvoos.skinmorph.SkinManager;
import com.luvoos.skinmorph.SkinMorph;
import com.luvoos.skinmorph.util.FormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkinMorphItems
{
    private static final SkinMorph plugin = SkinMorph.instance;
    private static final String PAGE_SWITCH_BASE64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDA4OWQwZTBjODhlNTI1MDAzOGU2NTlmNTA4ZWJkYTViNjVhNWJlMGJmYzQxMDNkMGUxMmZhYmE0MDE4ZDVjYiJ9fX0=";
    private static final String RANDOM_SKIN_BASE64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM0ZTA2M2NhZmI0NjdhNWM4ZGU0M2VjNzg2MTkzOTlmMzY5ZjRhNTI0MzRkYTgwMTdhOTgzY2RkOTI1MTZhMCJ9fX0=";
    private static final String SETTINGS_BASE64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWQyNzM3YThkMTlhYjA3OWNlZjdhODI0ZjE0OTcxNTVkYWE5MjVlOGMxYzZiN2E2Yzc1NjVlOGJlMzAzOWZhMCJ9fX0=";

    public static CompletableFuture<ItemStack> favoriteSkinHead(String name)
    {
        // check cache
        final SkinData cached = SkinManager.cache.get(name);

        CompletableFuture<SkinData> future =
                cached != null
                        ? CompletableFuture.completedFuture(cached)
                        // if we don't have it cached, fetch it asynchronously
                        : plugin.skinManager.getTexturesAsync(name, 3);

        return future.thenCompose(skinData -> {
            CompletableFuture<ItemStack> itemFuture = new CompletableFuture<>();

            // once we have textures, switch back to main thread to perform bukkit operations
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    // build item
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) item.getItemMeta();

                    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), name);
                    if (skinData != null) {
                        profile.setProperties(skinData.getTextureProperties());
                    }

                    meta.setPlayerProfile(profile);
                    meta.setDisplayName("§a" + FormatUtil.normalize(name));
                    meta.setLore(List.of(
                            "§r§f§lLEFT CLICK §r§ato apply skin§r",
                            "§r§f§lRIGHT CLICK §r§cto remove skin§r"
                    ));

                    item.setItemMeta(meta);

                    itemFuture.complete(item);
                } catch (Exception e) {
                    e.printStackTrace();

                    // if something goes wrong we return default steve head
                    ItemStack fallbackItem = new ItemStack(Material.PLAYER_HEAD);
                    ItemMeta meta = fallbackItem.getItemMeta();
                    meta.setDisplayName("§cFailed to load.");
                    fallbackItem.setItemMeta(meta);

                    itemFuture.complete(fallbackItem);
                }
            });

            return itemFuture;
        });
    }

    public static ItemStack changeSkin(Player player)
    {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        String currentSkin = plugin.skinManager.getCurrentSkin(player);
        PlayerProfile profile = Bukkit.createProfile(player.getUniqueId(), player.getName());
        meta.setPlayerProfile(profile);
        meta.setDisplayName("§eChange Skin");
        meta.setLore(List.of(
                "§r§7Your current skin: §e" + currentSkin + (currentSkin.equalsIgnoreCase(player.getName()) ? " (default)" : ""),
                "§r§f§lLEFT CLICK §r§afor yourself§r",
                "§r§f§lRIGHT CLICK §r§afor another player§r"
        ));
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack randomSkin(Player player)
    {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", RANDOM_SKIN_BASE64));
        meta.setPlayerProfile(profile);
        meta.setDisplayName("§eRandom Skin");
        meta.setLore(List.of(
                "§r§7Randomly pick from favorites",
                "§r§f§lLEFT CLICK §r§afor yourself§r",
                "§r§f§lRIGHT CLICK §r§afor another player§r"
        ));
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack resetSkin(Player player)
    {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eReset Skin");
        meta.setLore(List.of(
                "§r§f§lLEFT CLICK §r§afor yourself§r",
                "§r§f§lRIGHT CLICK §r§afor another player§r"
        ));
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack manageFavorites()
    {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eManage Favorites");
        meta.setLore(List.of(
                "§r§f§lLEFT CLICK §r§ato add favorite",
                "§r§f§lRIGHT CLICK §r§cto remove favorite"
        ));
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack settings()
    {
        return headFromBase64("§eSettings", SETTINGS_BASE64);
    }

    public static ItemStack clearCache()
    {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eClear Cache");
        meta.setLore(List.of(
                "§r§7This can free memory, but is §cnot recommended",
                "§r§cas it queries the Mojang API again, which can lead",
                "§r§cto requests being rejected§7."
        ));
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack previousPage()
    {
        return headFromBase64("§ePrevious Page", PAGE_SWITCH_BASE64);
    }

    public static ItemStack nextPage()
    {
        return headFromBase64("§eNext Page", PAGE_SWITCH_BASE64);
    }

    public static ItemStack back()
    {
        return headFromBase64("§eBack", PAGE_SWITCH_BASE64);
    }

    public static ItemStack background()
    {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack close()
    {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cClose");
        item.setItemMeta(meta);

        return item;
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