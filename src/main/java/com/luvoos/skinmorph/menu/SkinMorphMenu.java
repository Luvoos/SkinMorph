package com.luvoos.skinmorph.menu;

import com.luvoos.skinmorph.FavoritesManager;
import com.luvoos.skinmorph.SkinManager;
import com.luvoos.skinmorph.SkinMorph;
import com.luvoos.skinmorph.item.SkinMorphItems;
import com.luvoos.skinmorph.type.SkinMorphInputType;
import com.luvoos.skinmorph.type.SkinMorphMenuType;
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

public class SkinMorphMenu implements Listener
{
    private final SkinMorph plugin;
    private final FavoritesManager favoritesManager;
    private final SkinManager skinManager;
    private final int FAVORITES_PER_PAGE = 27;
    private final Set<Inventory> menuInventories = new HashSet<>();
    private final Map<UUID, SkinMorphMenuType> currentMenuTypes = new HashMap<>();
    private final Map<UUID, Integer> currentPages = new HashMap<>();
    public final Map<UUID, SkinMorphInputType> awaitingInput = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> inputWaitingTimes = new ConcurrentHashMap<>();

    public SkinMorphMenu(SkinMorph plugin, FavoritesManager favoritesManager, SkinManager skinManager)
    {
        this.plugin = plugin;
        this.favoritesManager = favoritesManager;
        this.skinManager = skinManager;
    }

    public void open(@NotNull final Player player, int page)
    {
        int totalPages = totalPages(player);

        page = Math.clamp(page, 1, totalPages);

        setCurrentMenuType(player, SkinMorphMenuType.MAIN);
        setCurrentPage(player, page);

        List<String> favorites = Optional.ofNullable(favoritesManager.getFavorites(player))
                                         .orElse(Collections.emptyList());

        Inventory menu = Bukkit.createInventory(null, 9 * 5, "Skin Menu                 Page " + page + "/" + totalPages);
        menuInventories.add(menu);

        int startIndex = (getCurrentPage(player) - 1) * FAVORITES_PER_PAGE;
        int endIndex = Math.min(startIndex + FAVORITES_PER_PAGE, favorites.size());

        boolean hasRandomSkinItem = favorites.size() > 1;
        boolean hasResetSkinItem = !player.getName().equalsIgnoreCase(skinManager.getCurrentSkin(player));

        if (hasRandomSkinItem) menu.setItem(0, SkinMorphItems.randomSkin(player));
        if (hasResetSkinItem) menu.setItem(2, SkinMorphItems.resetSkin(player));
        menu.setItem(4, SkinMorphItems.changeSkin(player));
        menu.setItem(6, SkinMorphItems.manageFavorites());
        menu.setItem(8, SkinMorphItems.settings());

        boolean hasNextPage = endIndex < favorites.size();
        boolean hasPreviousPage = getCurrentPage(player) > 1;

        if (hasPreviousPage) menu.setItem(36, SkinMorphItems.previousPage());
        if (hasNextPage) menu.setItem(44, SkinMorphItems.nextPage());

        menu.setItem(40, SkinMorphItems.close());

        for (int i = startIndex; i < endIndex; i++) {
            final int slot = (i - startIndex) + 9;
            SkinMorphItems.favoriteSkinHead(favorites.get(i)).thenAccept(item -> {
                Bukkit.getScheduler().runTask(plugin, () -> menu.setItem(slot, item));
            });
        }

        fillBackground(player, menu);

        player.openInventory(menu);
    }

    public void openSettings(@NotNull final Player player)
    {
        setCurrentMenuType(player, SkinMorphMenuType.SETTINGS);

        Inventory menu = Bukkit.createInventory(null, 9 * 2, "SkinMorph Settings");
        menuInventories.add(menu);

        menu.setItem(4, SkinMorphItems.clearCache());
        menu.setItem(9, SkinMorphItems.back());
        menu.setItem(13, SkinMorphItems.close());

        fillBackground(player, menu);

        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        if (menuInventories.contains(event.getClickedInventory())) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;
            event.setCursor(null);
            player.updateInventory();

            String itemName = clickedItem.getItemMeta().getDisplayName();

            if (itemName.contains("Change Skin")) {
                if (event.isLeftClick()) {
                    awaitInput(player, SkinMorphInputType.CHANGE_SKIN_SELF);
                } else if (event.isRightClick()) {
                    awaitInput(player, SkinMorphInputType.CHANGE_SKIN_OTHER);
                }
            }

            if (itemName.contains("Manage Favorites")) {
                if (event.isLeftClick()) {
                    awaitInput(player, SkinMorphInputType.ADD_FAVORITE);
                } else if (event.isRightClick()) {
                    awaitInput(player, SkinMorphInputType.REMOVE_FAVORITE);
                }
            }

            if (itemName.contains("Reset Skin")) {
                if (event.isLeftClick()) {
                    skinManager.resetSkin(player, true);
                } else if (event.isRightClick()) {
                    awaitInput(player, SkinMorphInputType.RESET_SKIN_OTHER);
                }
            }

            if (itemName.contains("Random Skin")) {
                if (event.isLeftClick()) {
                    skinManager.applyRandomSkin(player, true);
                    player.closeInventory();
                } else if (event.isRightClick()) {
                    awaitInput(player, SkinMorphInputType.RANDOM_SKIN_OTHER);
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

            if (itemName.contains("Settings")) {
                openSettings(player);
            }

            if (itemName.contains("Back")) {
                open(player, getCurrentPage(player));
            }

            if (itemName.contains("Clear Cache")) {
                skinManager.clearCache();
                player.sendMessage("§aThe cache was cleared!");
                player.closeInventory();
            }

            if (itemName.contains("Close")) {
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event)
    {
        menuInventories.remove(event.getInventory());
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
        if (inputType == SkinMorphInputType.CHANGE_SKIN_OTHER) player.sendMessage("§aPlease enter a skin name and a target username in chat separated by a semicolon (;) within the next 20 seconds!");
        if (inputType == SkinMorphInputType.RESET_SKIN_OTHER || inputType == SkinMorphInputType.RANDOM_SKIN_OTHER) player.sendMessage("§aPlease enter a target username in chat within the next 20 seconds!");
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
        }.runTaskTimer(plugin, 0, 20);
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent event)
    {
        if (menuInventories.contains(event.getInventory())) event.setCancelled(true);
    }

    public SkinMorphMenuType getCurrentMenuType(@NotNull Player player)
    {
        return currentMenuTypes.getOrDefault(player.getUniqueId(), SkinMorphMenuType.MAIN);
    }

    public void setCurrentMenuType(@NotNull Player player, SkinMorphMenuType menuType)
    {
        currentMenuTypes.put(player.getUniqueId(), menuType);
    }

    public int getCurrentPage(@NotNull Player player)
    {
        return currentPages.getOrDefault(player.getUniqueId(), 1);
    }

    public void setCurrentPage(@NotNull Player player, int value)
    {
        currentPages.put(player.getUniqueId(), value);
    }

    public void increaseCurrentPage(@NotNull Player player)
    {
        currentPages.put(player.getUniqueId(), getCurrentPage(player) + 1);
    }

    public void decreaseCurrentPage(@NotNull Player player)
    {
        currentPages.put(player.getUniqueId(), getCurrentPage(player) - 1);
    }

    public int totalPages(@NotNull final Player player)
    {
        int favoritesSize = Optional.ofNullable(favoritesManager.getFavorites(player))
                                    .map(List::size).orElse(0);

        return Math.max(1, (favoritesSize + FAVORITES_PER_PAGE - 1) / FAVORITES_PER_PAGE);
    }

    private void fillBackground(final Player player, @NotNull final Inventory inventory)
    {
        final int size = inventory.getSize();
        for (int i = 0; i < size; i++) {
            if (inventory.getItem(i) != null) continue;
            if (i < 9 || i > size - 10) {
                switch (i) {
                    case 0, 2, 4, 6, 8, 36, 40, 44 -> {
                        if (getCurrentMenuType(player) == SkinMorphMenuType.MAIN) {
                            inventory.setItem(i, SkinMorphItems.backgroundBlack());
                        } else {
                            inventory.setItem(i, SkinMorphItems.backgroundGray());
                        }
                    }
                    default ->
                            inventory.setItem(i, SkinMorphItems.backgroundGray());
                }
            }
        }
    }
}
