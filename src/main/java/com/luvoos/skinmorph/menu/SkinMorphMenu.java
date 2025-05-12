package com.luvoos.skinmorph.menu;

import com.luvoos.skinmorph.FavoritesManager;
import com.luvoos.skinmorph.SkinManager;
import com.luvoos.skinmorph.SkinMorph;
import com.luvoos.skinmorph.type.SkinMorphInputType;
import com.luvoos.skinmorph.item.SkinMorphItems;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class SkinMorphMenu implements Listener {

    private final SkinMorph plugin;
    private final FavoritesManager favoritesManager;
    private final SkinManager skinManager;
    private final int FAVORITES_PER_PAGE = 27;
    private final Map<UUID, Integer> currentPages = new HashMap<>();
    public final Map<UUID, SkinMorphInputType> awaitingInput = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> inputWaitingTimes = new ConcurrentHashMap<>();

    public SkinMorphMenu(SkinMorph plugin, FavoritesManager favoritesManager, SkinManager skinManager)
    {
        this.plugin = plugin;
        this.favoritesManager = favoritesManager;
        this.skinManager = skinManager;
    }

    public void open(@NotNull final Player player, int page) throws ExecutionException, InterruptedException
    {
        setCurrentPage(player, page);

        List<String> favorites = favoritesManager.getFavorites(player);
        if (favorites == null) favorites = new ArrayList<>();

        int totalPages = totalPages(player);

        Inventory menu = Bukkit.createInventory(null, 9 * 5, "Skin Menu                 Page " + page + "/" + (totalPages));

        int startIndex = (getCurrentPage(player) - 1) * FAVORITES_PER_PAGE;
        int endIndex = Math.min(startIndex + FAVORITES_PER_PAGE, favorites.size());

        if (favorites.size() > 1) menu.setItem(2, SkinMorphItems.randomSkin(player));
        menu.setItem(3, SkinMorphItems.resetSkin(player));
        menu.setItem(4, SkinMorphItems.changeSkin(player));
        menu.setItem(5, SkinMorphItems.manageFavorites());

        boolean hasNextPage = endIndex < favorites.size();
        boolean hasPreviousPage = getCurrentPage(player) > 1;

        if (hasPreviousPage) {
            menu.setItem(36, SkinMorphItems.previousPage());
        }
        if (hasNextPage) {
            menu.setItem(44, SkinMorphItems.nextPage());
        }

        for (int i = startIndex; i < endIndex; i++) {
            final int slot = (i - startIndex) + 9;
            SkinMorphItems.favoriteSkinHead(player, favorites.get(i)).thenAccept(item -> {
                Bukkit.getScheduler().runTask(plugin, () -> menu.setItem(slot, item));
            });
        }

        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) throws ExecutionException, InterruptedException
    {
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        if (event.getView().getTitle().startsWith("Skin Menu                 Page ")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta() ) return;
            event.setCursor(null);
            player.updateInventory();

            String itemName = clickedItem.getItemMeta().getDisplayName();

            if (itemName.contains("Change skin")) {
                if (event.isLeftClick()) {
                    awaitInput(player, SkinMorphInputType.CHANGE_SKIN_SELF);
                } else if (event.isRightClick()) {
                    awaitInput(player, SkinMorphInputType.CHANGE_SKIN_OTHERS);
                }
            }

            if (itemName.contains("Manage favorites")) {
                if (event.isLeftClick()) {
                    awaitInput(player, SkinMorphInputType.ADD_FAVORITE);
                } else if (event.isRightClick()) {
                    awaitInput(player, SkinMorphInputType.REMOVE_FAVORITE);
                }
            }

            if (itemName.contains("Reset skin")) {
                if (event.isLeftClick()) {
                    skinManager.resetSkin(player, true);
                } else if (event.isRightClick()) {
                    awaitInput(player, SkinMorphInputType.RESET_SKIN_OTHERS);
                }
            }

            if (itemName.contains("Random skin")) {
                if (event.isLeftClick()) {
                    skinManager.applyRandomSkin(player, true);
                    player.closeInventory();
                } else if (event.isRightClick()) {
                    awaitInput(player, SkinMorphInputType.RANDOM_SKIN_OTHERS);
                }
            }

            if (itemName.startsWith("§a")) {
                String skinName = itemName.replace("§a", "");
                if (event.isLeftClick()) {
                    skinManager.setSkinFromName(player, skinName, true);
                    player.closeInventory();
                } else if (event.isRightClick()) {
                    favoritesManager.removeFavorite(player, skinName, true);
                    open(player, (getCurrentPage(player) <= totalPages(player) ? getCurrentPage(player) : getCurrentPage(player) - 1));
                }
            }

            if (itemName.contains("Previous Page")) {
                if (getCurrentPage(player) > 1) {
                    decreaseCurrentPage(player);
                    open(player, getCurrentPage(player));
                }
            }

            if (itemName.contains("Next Page")) {
                if (getCurrentPage(player) > 0) {
                    increaseCurrentPage(player);
                    open(player, getCurrentPage(player));
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event)
    {
        final Player player = (Player) event.getPlayer();
        if (event.getView().getTitle().startsWith("Skin Menu                 Page ")) {

        }
    }

    /**
     * Await chat input from a player
     * @param player
     * @param inputType
     */
    private void awaitInput(@NotNull final Player player, @NotNull SkinMorphInputType inputType)
    {
        awaitingInput.remove(player.getUniqueId());
        inputWaitingTimes.remove(player.getUniqueId());
        player.closeInventory();
        if (inputType == SkinMorphInputType.ADD_FAVORITE || inputType == SkinMorphInputType.CHANGE_SKIN_SELF || inputType == SkinMorphInputType.REMOVE_FAVORITE) player.sendMessage("§aPlease enter a skin name in chat within the next 20 seconds!");
        if (inputType == SkinMorphInputType.CHANGE_SKIN_OTHERS) player.sendMessage("§aPlease enter a skin name and a target username in chat separated by a semicolon (;) within the next 20 seconds!");
        if (inputType == SkinMorphInputType.RESET_SKIN_OTHERS || inputType == SkinMorphInputType.RANDOM_SKIN_OTHERS) player.sendMessage("§aPlease enter a target username in chat within the next 20 seconds!");
        inputWaitingTimes.put(player.getUniqueId(), 20);
        awaitingInput.put(player.getUniqueId(), inputType);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (inputWaitingTimes.containsKey(player.getUniqueId()) && awaitingInput.containsKey(player.getUniqueId())) {
                    if (inputWaitingTimes.get(player.getUniqueId()) > 0) {
                        inputWaitingTimes.compute(player.getUniqueId(), (k, oldTime) -> oldTime - 1);
                    } else {
                        this.cancel();
                        awaitingInput.remove(player.getUniqueId());
                        inputWaitingTimes.remove(player.getUniqueId());
                        if (player.isOnline()) player.sendMessage("§cChat input expired!");
                    }
                } else {
                    this.cancel();
                    inputWaitingTimes.remove(player.getUniqueId());
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent event)
    {
        if (event.getView().getTitle().startsWith("Skin Menu                 Page ")) event.setCancelled(true);
    }

    private int getCurrentPage(@NotNull Player player)
    {
        return currentPages.get(player.getUniqueId());
    }

    private void setCurrentPage(@NotNull Player player, int value)
    {
        currentPages.put(player.getUniqueId(), value);
    }

    private void increaseCurrentPage(@NotNull Player player)
    {
        currentPages.put(player.getUniqueId(), getCurrentPage(player) + 1);
    }

    private void decreaseCurrentPage(@NotNull Player player)
    {
        currentPages.put(player.getUniqueId(), getCurrentPage(player) - 1);
    }

    private int totalPages(@NotNull final Player player)
    {
        int favoritesSize = favoritesManager.getFavorites(player).size();
        return (favoritesSize > 0) ? (int) Math.ceil((double) favoritesSize / FAVORITES_PER_PAGE) : 1;
    }
}
