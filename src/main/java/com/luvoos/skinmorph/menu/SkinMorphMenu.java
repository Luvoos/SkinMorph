package com.luvoos.skinmorph.menu;

import com.luvoos.skinmorph.SkinMorph;
import com.luvoos.skinmorph.item.SkinMorphItems;
import com.luvoos.skinmorph.manager.FavoritesManager;
import com.luvoos.skinmorph.manager.LanguageManager;
import com.luvoos.skinmorph.manager.SkinManager;
import com.luvoos.skinmorph.type.SkinMorphInputType;
import com.luvoos.skinmorph.type.SkinMorphMenuType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SkinMorphMenu implements Listener
{
    private final SkinMorph plugin;
    private final FavoritesManager favoritesManager;
    private final SkinManager skinManager;
    private final LanguageManager langManager;
    private final int FAVORITES_PER_PAGE = 27;
    private final Set<Inventory> menuInventories = new HashSet<>();
    private final Map<UUID, SkinMorphMenuType> currentMenuTypes = new HashMap<>();
    private final Map<UUID, Integer> currentPages = new HashMap<>();

    public SkinMorphMenu(SkinMorph plugin, FavoritesManager favoritesManager, SkinManager skinManager, LanguageManager languageManager)
    {
        this.plugin = plugin;
        this.favoritesManager = favoritesManager;
        this.skinManager = skinManager;
        this.langManager = languageManager;
    }

    public void open(@NotNull final Player player, int page)
    {
        int totalPages = totalPages(player);

        page = Math.clamp(page, 1, totalPages);

        setCurrentMenuType(player, SkinMorphMenuType.MAIN);
        setCurrentPage(player, page);

        List<String> favorites = Optional.ofNullable(favoritesManager.getFavorites(player))
                                         .orElse(Collections.emptyList());

        Inventory menu = Bukkit.createInventory(null, 9 * 5, langManager.translate("menu.title-main", Map.of(
                "ph1", String.valueOf(page),
                "ph2", String.valueOf(totalPages)
        )));
        menuInventories.add(menu);

        int startIndex = (getCurrentPage(player) - 1) * FAVORITES_PER_PAGE;
        int endIndex = Math.min(startIndex + FAVORITES_PER_PAGE, favorites.size());

        boolean hasRandomSkinItem = favorites.size() > 1;
        boolean hasResetSkinItem = !player.getName().equalsIgnoreCase(skinManager.getCurrentSkin(player));

        if (hasRandomSkinItem)
            menu.setItem(0, SkinMorphItems.randomSkin());
        if (hasResetSkinItem)
            menu.setItem(2, SkinMorphItems.resetSkin());
        menu.setItem(4, SkinMorphItems.changeSkin(player));
        menu.setItem(6, SkinMorphItems.manageFavorites());
        menu.setItem(8, SkinMorphItems.settings());

        boolean hasNextPage = endIndex < favorites.size();
        boolean hasPreviousPage = getCurrentPage(player) > 1;

        if (hasPreviousPage)
            menu.setItem(36, SkinMorphItems.previousPage());
        if (hasNextPage)
            menu.setItem(44, SkinMorphItems.nextPage());

        menu.setItem(40, SkinMorphItems.close());

        for (int i = startIndex; i < endIndex; i++) {
            final int slot = (i - startIndex) + 9;
            SkinMorphItems.favoriteSkinHead(favorites.get(i)).thenAccept(item -> {
                Bukkit.getScheduler().runTask(plugin, () -> menu.setItem(slot, item));
            });
        }

        fillBackground(getCurrentMenuType(player), menu);

        player.openInventory(menu);
    }

    public void openSettings(@NotNull final Player player)
    {
        setCurrentMenuType(player, SkinMorphMenuType.SETTINGS);

        Inventory menu = Bukkit.createInventory(null, 9 * 2, langManager.translate("menu.title-settings"));
        menuInventories.add(menu);

        menu.setItem(3, SkinMorphItems.clearCache());
        menu.setItem(5, SkinMorphItems.removeSkinFromCache());
        menu.setItem(9, SkinMorphItems.back());
        menu.setItem(13, SkinMorphItems.close());

        fillBackground(getCurrentMenuType(player), menu);

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

            if (!clickedItem.getItemMeta().getDisplayName().equalsIgnoreCase(" ")) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.25f, 1.0f);
            }

            Component itemNameComponent = clickedItem.getItemMeta().displayName();
            if (itemNameComponent == null) return;
            String itemName = PlainTextComponentSerializer.plainText().serialize(itemNameComponent);

            if (itemName.contains(PlainTextComponentSerializer.plainText().serialize(langManager.translate("menu.change-skin")))) {
                if (event.isLeftClick()) {
                    plugin.inputManager.awaitInput(player, SkinMorphInputType.CHANGE_SKIN_SELF);
                } else if (event.isRightClick()) {
                    plugin.inputManager.awaitInput(player, SkinMorphInputType.CHANGE_SKIN_OTHER);
                }
            }

            if (itemName.contains(PlainTextComponentSerializer.plainText().serialize(langManager.translate("menu.manage-favorites")))) {
                if (event.isLeftClick()) {
                    plugin.inputManager.awaitInput(player, SkinMorphInputType.ADD_FAVORITE);
                } else if (event.isRightClick()) {
                    plugin.inputManager.awaitInput(player, SkinMorphInputType.REMOVE_FAVORITE);
                }
            }

            if (itemName.contains(PlainTextComponentSerializer.plainText().serialize(langManager.translate("menu.reset-skin")))) {
                if (event.isLeftClick()) {
                    skinManager.resetSkin(player, true);
                } else if (event.isRightClick()) {
                    plugin.inputManager.awaitInput(player, SkinMorphInputType.RESET_SKIN_OTHER);
                }
            }

            if (itemName.contains(PlainTextComponentSerializer.plainText().serialize(langManager.translate("menu.random-skin")))) {
                if (event.isLeftClick()) {
                    skinManager.applyRandomSkin(player, true);
                    player.closeInventory();
                } else if (event.isRightClick()) {
                    plugin.inputManager.awaitInput(player, SkinMorphInputType.RANDOM_SKIN_OTHER);
                }
            }

            if (event.getSlot() > 8 && event.getSlot() < 36 && getCurrentMenuType(player) == SkinMorphMenuType.MAIN) {
                String skinName = itemName.trim();

                if (event.isLeftClick()) {
                    skinManager.setSkinFromName(player, skinName, true);
                    player.closeInventory();
                } else if (event.isRightClick()) {
                    favoritesManager.removeFavorite(player, skinName, true);
                    open(player, (getCurrentPage(player) <= totalPages(player) ? getCurrentPage(player) : getCurrentPage(player) - 1));
                }
            }

            if (itemName.contains(PlainTextComponentSerializer.plainText().serialize(langManager.translate("menu.previous-page")))) {
                if (getCurrentPage(player) > 1) {
                    decreaseCurrentPage(player);
                    open(player, getCurrentPage(player));
                }
            }

            if (itemName.contains(PlainTextComponentSerializer.plainText().serialize(langManager.translate("menu.next-page")))) {
                if (getCurrentPage(player) > 0) {
                    increaseCurrentPage(player);
                    open(player, getCurrentPage(player));
                }
            }

            if (itemName.contains(PlainTextComponentSerializer.plainText().serialize(langManager.translate("menu.settings")))) {
                openSettings(player);
            }

            if (itemName.contains(PlainTextComponentSerializer.plainText().serialize(langManager.translate("menu.back")))) {
                open(player, getCurrentPage(player));
            }

            if (itemName.contains(PlainTextComponentSerializer.plainText().serialize(langManager.translate("menu.clear-cache")))) {
                skinManager.clearCache();
                player.sendMessage(plugin.langManager.translate("messages.cache-cleared"));
                player.closeInventory();
            }

            if (itemName.contains(PlainTextComponentSerializer.plainText().serialize(langManager.translate("menu.remove-skin-from-cache")))) {
                plugin.inputManager.awaitInput(player, SkinMorphInputType.REMOVE_SKIN_FROM_CACHE);
            }

            if (itemName.contains(PlainTextComponentSerializer.plainText().serialize(langManager.translate("menu.close")))) {
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event)
    {
        menuInventories.remove(event.getInventory());
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

    private void fillBackground(final SkinMorphMenuType menuType, @NotNull final Inventory inventory)
    {
        final int size = inventory.getSize();
        for (int i = 0; i < size; i++) {
            if (inventory.getItem(i) != null) continue;
            if (i < 9 || i > size - 10) {
                switch (i) {
                    case 0, 2, 4, 6, 8 -> {
                        if (menuType == SkinMorphMenuType.MAIN) {
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
