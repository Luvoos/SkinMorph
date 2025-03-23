package com.luvoos.skinmorph.menu;

import com.luvoos.skinmorph.FavoritesManager;
import com.luvoos.skinmorph.SkinManager;
import com.luvoos.skinmorph.SkinMorph;
import com.luvoos.skinmorph.item.SkinMorphItems;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SkinMorphMenu implements Listener {

    private final SkinMorph plugin;
    private final FavoritesManager favoritesManager;
    private final SkinManager skinManager;

    public SkinMorphMenu(SkinMorph plugin, FavoritesManager favoritesManager, SkinManager skinManager)
    {
        this.plugin = plugin;
        this.favoritesManager = favoritesManager;
        this.skinManager = skinManager;
    }

    public void open(@NotNull Player player) throws ExecutionException, InterruptedException {
        Inventory menu = Bukkit.createInventory(null, 9 * 5, "Skin Menu");

        List<String> favorites = favoritesManager.getFavorites(player);
        if (favorites == null) favorites = new ArrayList<>();

        menu.setItem(3, SkinMorphItems.resetYourSkin(player));
        menu.setItem(4, SkinMorphItems.changeYourSkin(player));
        menu.setItem(5, SkinMorphItems.addFavorite);

        int maxSlots = 9 * 5 - 8;
        for (int i = 0; i < Math.min(favorites.size(), maxSlots); i++) {
            final int slot = i + 9;
            SkinMorphItems.playerHead(favorites.get(i)).thenAccept(item -> {
                Bukkit.getScheduler().runTask(plugin, () -> menu.setItem(slot, item));
            });
        }

        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) throws ExecutionException, InterruptedException {
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        if (event.getView().getTitle().equals("Skin Menu")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta() ) return;

            String itemName = clickedItem.getItemMeta().getDisplayName();

            if (itemName.contains("Change your skin")) {
                player.sendMessage("Change your skin");
            }

            if (itemName.contains("Add favorite")) {
                player.sendMessage("Add fav");
            }

            if (itemName.contains("Reset your skin")) {
                skinManager.resetSkin(player, true);
            }

            if (itemName.startsWith("§a")) {
                String skinName = itemName.replace("§a", "");
                if (event.isLeftClick()) {
                    skinManager.setSkinFromName(player, skinName, true);
                } else if (event.isRightClick()) {
                    favoritesManager.removeFavorite(player, skinName);
                    open(player);
                }
            }
        }
    }
}
