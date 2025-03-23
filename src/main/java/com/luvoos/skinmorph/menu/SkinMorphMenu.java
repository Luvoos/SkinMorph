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

    public void open(@NotNull Player player, int page) throws ExecutionException, InterruptedException {
        Inventory menu = Bukkit.createInventory(null, 9 * 5, "Skin Menu                           " + page);
        System.out.println("gjreogjogjoegjerig");
        int favsPerPage = 36; // 9*4 grid
        int startIndex = (page - 1) * favsPerPage;
        int endIndex = Math.min(startIndex + favsPerPage, favoritesManager.getFavorites(player).size()) - 1;

        List<String> favorites = favoritesManager.getFavorites(player);
        if (favorites == null) favorites = new ArrayList<>();

        menu.setItem(3, SkinMorphItems.resetYourSkin(player));
        menu.setItem(4, SkinMorphItems.changeYourSkin(player));
        menu.setItem(5, SkinMorphItems.addFavorite);

        //int maxSlots = 9 * 5 - 8;
        for (int i = startIndex; i < endIndex; i++) {
            final int slot = (i - startIndex) + 9;
            SkinMorphItems.playerHead(favorites.get(i)).thenAccept(item -> {
                Bukkit.getScheduler().runTask(plugin, () -> menu.setItem(slot, item));
            });
            System.out.println(i);
        }

        if (page > 1) {
            menu.setItem(36, SkinMorphItems.previousPage());
        }

        //if (endIndex < favorites.size()) {
            menu.setItem(44, SkinMorphItems.nextPage());
        //}

        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) throws ExecutionException, InterruptedException {
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        if (event.getView().getTitle().startsWith("Skin Menu                           ")) {
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
                    open(player, 1);
                }
            }

            if (itemName.contains("Previous Page")) {
                open(player, 1);
            }

            if (itemName.contains("Next Page")) {
                open(player, getCurrentPage(event) + 1);
            }
        }
    }

    private int getCurrentPage(final InventoryClickEvent event) {
        return 1;
    }
}
