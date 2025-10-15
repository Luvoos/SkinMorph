package com.luvoos.skinmorph.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.luvoos.skinmorph.SkinData;
import com.luvoos.skinmorph.SkinMorph;
import com.luvoos.skinmorph.util.FormatUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkinMorphItems
{
    private SkinMorphItems() {}

    private static final SkinMorph plugin = SkinMorph.instance;
    private static final String PAGE_SWITCH_BASE64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDA4OWQwZTBjODhlNTI1MDAzOGU2NTlmNTA4ZWJkYTViNjVhNWJlMGJmYzQxMDNkMGUxMmZhYmE0MDE4ZDVjYiJ9fX0=";
    private static final String RANDOM_SKIN_BASE64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM0ZTA2M2NhZmI0NjdhNWM4ZGU0M2VjNzg2MTkzOTlmMzY5ZjRhNTI0MzRkYTgwMTdhOTgzY2RkOTI1MTZhMCJ9fX0=";
    private static final String RESET_SKIN_BASE64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2U1YWQxMzU2N2EzY2E5NjgyYzUxMzliNzJlZjhiNTY0NTUxZjRmNGNjYzE4NGE5NDYwZDVkNTVkNzIwZmU2NiJ9fX0=";
    private static final String SETTINGS_BASE64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWQyNzM3YThkMTlhYjA3OWNlZjdhODI0ZjE0OTcxNTVkYWE5MjVlOGMxYzZiN2E2Yzc1NjVlOGJlMzAzOWZhMCJ9fX0=";

    public static CompletableFuture<ItemStack> favoriteSkinHead(String name)
    {
        // check cache
        final SkinData cached = plugin.cacheManager.getSkin(name);

        CompletableFuture<SkinData> future =
                cached != null
                        ? CompletableFuture.completedFuture(cached)
                        // if we don't have it cached, fetch it asynchronously
                        : plugin.skinManager.getSkinAsync(name, 3);

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
                        profile.setProperty(skinData.getTextureProperty());
                    }

                    meta.setPlayerProfile(profile);
                    meta.displayName(plugin.langManager.translate("menu.favorite-name", Map.of(
                            "ph1", FormatUtil.normalize(name)
                    )));

                    List<Component> lore = List.of(
                            plugin.langManager.translate("menu.apply-skin"),
                            plugin.langManager.translate("menu.remove-skin"));
                    meta.lore(lore);

                    item.setItemMeta(meta);

                    itemFuture.complete(item);
                } catch (Exception e) {
                    e.printStackTrace();

                    // if something goes wrong we return default steve head
                    ItemStack fallbackItem = new ItemStack(Material.PLAYER_HEAD);
                    ItemMeta meta = fallbackItem.getItemMeta();
                    meta.displayName(plugin.langManager.translate("menu.failed-to-load"));
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
        meta.displayName(plugin.langManager.translate("menu.change-skin"));

        List<Component> lore = List.of(
                currentSkin.equalsIgnoreCase(player.getName()) ?
                        plugin.langManager.translate("menu.current-skin-default", Map.of(
                                "ph1", currentSkin.toLowerCase()
                        )) :
                        plugin.langManager.translate("menu.current-skin", Map.of(
                                "ph1", currentSkin.toLowerCase()
                        )),
                plugin.langManager.translate("menu.for-yourself"),
                plugin.langManager.translate("menu.for-another-player")
        );
        meta.lore(lore);

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack randomSkin()
    {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", RANDOM_SKIN_BASE64));
        meta.setPlayerProfile(profile);
        meta.displayName(plugin.langManager.translate("menu.random-skin"));

        List<Component> lore = List.of(
                plugin.langManager.translate("menu.random-skin-description"),
                plugin.langManager.translate("menu.for-yourself"),
                plugin.langManager.translate("menu.for-another-player")
        );
        meta.lore(lore);

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack resetSkin()
    {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", RESET_SKIN_BASE64));
        meta.setPlayerProfile(profile);
        meta.displayName(plugin.langManager.translate("menu.reset-skin"));

        List<Component> lore = List.of(
                plugin.langManager.translate("menu.for-yourself"),
                plugin.langManager.translate("menu.for-another-player")
        );
        meta.lore(lore);

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack manageFavorites()
    {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plugin.langManager.translate("menu.manage-favorites"));

        List<Component> lore = List.of(
                plugin.langManager.translate("menu.add-favorite"),
                plugin.langManager.translate("menu.remove-favorite")
        );
        meta.lore(lore);

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack settings()
    {
        return headFromBase64(plugin.langManager.translate("menu.settings"), SETTINGS_BASE64);
    }

    public static ItemStack clearCache()
    {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plugin.langManager.translate("menu.clear-cache"));

        List<Component> lore = List.of(
                plugin.langManager.translate("menu.clear-cache-description")
        );
        meta.lore(lore);

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack previousPage()
    {
        return headFromBase64(plugin.langManager.translate("menu.previous-page"), PAGE_SWITCH_BASE64);
    }

    public static ItemStack nextPage()
    {
        return headFromBase64(plugin.langManager.translate("menu.next-page"), PAGE_SWITCH_BASE64);
    }

    public static ItemStack back()
    {
        return headFromBase64(plugin.langManager.translate("menu.back"), PAGE_SWITCH_BASE64);
    }

    public static ItemStack backgroundGray()
    {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack backgroundBlack()
    {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack close()
    {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plugin.langManager.translate("menu.close"));
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack headFromBase64(String displayName, String base64)
    {
        return headFromBase64(Component.text(displayName), base64);
    }

    public static ItemStack headFromBase64(Component displayName, String base64)
    {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", base64));
        meta.setPlayerProfile(profile);
        meta.displayName(displayName);
        item.setItemMeta(meta);

        return item;
    }
}