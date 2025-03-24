package com.luvoos.skinmorph.menu;

import com.luvoos.skinmorph.FavoritesManager;
import com.luvoos.skinmorph.SkinManager;
import com.luvoos.skinmorph.SkinMorph;
import com.luvoos.skinmorph.SkinMorphInputType;
import com.luvoos.skinmorph.item.SkinMorphItems;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class SkinMorphMenu implements Listener {

    private final SkinMorph plugin;
    private final FavoritesManager favoritesManager;
    private final SkinManager skinManager;
    private final int FAVORITES_PER_PAGE = 27;
    private final Map<UUID, Integer> currentPages = new HashMap<>();
    public final Map<UUID, SkinMorphInputType> awaitingInput = new HashMap<>();
    private final Map<UUID, Integer> inputWaitingTimes = new HashMap<>();

    public SkinMorphMenu(SkinMorph plugin, FavoritesManager favoritesManager, SkinManager skinManager)
    {
        this.plugin = plugin;
        this.favoritesManager = favoritesManager;
        this.skinManager = skinManager;
    }

    public void open(@NotNull final Player player, int page) throws ExecutionException, InterruptedException
    {
        currentPages.put(player.getUniqueId(), page);

        List<String> favorites = favoritesManager.getFavorites(player);
        if (favorites == null) favorites = new ArrayList<>();

        int totalPages = totalPages(player);

        Inventory menu = Bukkit.createInventory(null, 9 * 5, "Skin Menu                 Page " + page + "/" + (totalPages));

        int startIndex = getCurrentPage(player) * FAVORITES_PER_PAGE;
        int endIndex = Math.min(startIndex + FAVORITES_PER_PAGE, favorites.size());

        menu.setItem(3, SkinMorphItems.resetYourSkin(player));
        menu.setItem(4, SkinMorphItems.changeYourSkin(player));
        menu.setItem(5, SkinMorphItems.addFavorite);

        List<ItemStack> itemsOnPage = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            final int slot = (i - startIndex) + 9;
            SkinMorphItems.playerHead(favorites.get(i)).thenAccept(item -> {
                // Add item to the list
                itemsOnPage.add(item);

                // Ensure inventory updates happen on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    menu.setItem(slot, item);
                });
            });
        }

        boolean hasNextPage = endIndex < favorites.size();
        boolean hasPreviousPage = getCurrentPage(player) > 1;

        for (int i = 0; i < itemsOnPage.size(); i++) {
            menu.setItem(i + 9, itemsOnPage.get(i));
        }

        if (hasPreviousPage) {
            menu.setItem(36, SkinMorphItems.previousPage());
        }
        if (hasNextPage) {
            menu.setItem(44, SkinMorphItems.nextPage());
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

            String itemName = clickedItem.getItemMeta().getDisplayName();

            if (itemName.contains("Change your skin")) {
                awaitInput(player, SkinMorphInputType.CHANGE_SKIN_SELF);
            }

            if (itemName.contains("Add favorite")) {
                awaitInput(player, SkinMorphInputType.ADD_FAVORITE);
            }

            if (itemName.contains("Reset your skin")) {
                skinManager.resetSkin(player, true);
            }

            if (itemName.startsWith("§a")) {
                String skinName = itemName.replace("§a", "");
                if (event.isLeftClick()) {
                    skinManager.setSkinFromName(player, skinName, true);
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
     * Await skin name input from a player
     * @param player
     * @param inputType
     */
    private void awaitInput(@NotNull final Player player, @NotNull SkinMorphInputType inputType)
    {
        player.closeInventory();
        player.sendMessage("§aPlease enter a skin name in chat within the next 20 seconds!");
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
                        player.sendMessage("§cChat input expired!");
                    }
                } else {
                    this.cancel();
                    inputWaitingTimes.remove(player.getUniqueId());
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20);
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

    private int totalPages(@NotNull final Player player) {
        return (int) Math.ceil((double) favoritesManager.getFavorites(player).size() / FAVORITES_PER_PAGE) - 1;
    }
}
