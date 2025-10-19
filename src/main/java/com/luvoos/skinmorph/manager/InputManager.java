package com.luvoos.skinmorph.manager;

import com.luvoos.skinmorph.SkinMorph;
import com.luvoos.skinmorph.type.SkinMorphInputType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InputManager implements Listener
{
    private final SkinMorph plugin;
    private final Map<UUID, SkinMorphInputType> awaitingInput = new ConcurrentHashMap<>();
    private final Map<UUID, String> firstInput = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> step = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> timeoutTasks = new ConcurrentHashMap<>();

    public InputManager(SkinMorph plugin)
    {
        this.plugin = plugin;
    }

    public void awaitInput(@NotNull final Player player, @NotNull final SkinMorphInputType inputType)
    {
        remove(player);

        player.closeInventory();
        awaitingInput.put(player.getUniqueId(), inputType);
        step.put(player.getUniqueId(), 1);

        sendInstruction(player, inputType, 1);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (awaitingInput.containsKey(player.getUniqueId())) {
                    remove(player);
                    if (player.isOnline())
                        player.sendMessage(langManager().translate("messages.chat-input-expired"));
                }
            }
        }.runTaskLater(plugin, 20L * 20); // give player 20 seconds to enter input

        timeoutTasks.put(player.getUniqueId(), task);
    }

    @EventHandler
    public void onChat(final AsyncPlayerChatEvent event)
    {
        final Player player = event.getPlayer();

        if (!awaitingInput.containsKey(player.getUniqueId())) return;

        event.setCancelled(true);

        final String input = event.getMessage().trim();
        final SkinMorphInputType inputType = awaitingInput.get(player.getUniqueId());
        final int currentStep = step.getOrDefault(player.getUniqueId(), 1);

        Bukkit.getScheduler().runTask(plugin, () -> handleInput(player, inputType, currentStep, input));
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event)
    {
        final Player player = event.getPlayer();
        remove(player);
    }

    private void handleInput(@NotNull final Player player, @NotNull SkinMorphInputType inputType, final int currentStep, final String input)
    {
        switch (inputType) {
            case CHANGE_SKIN_SELF -> {
                plugin.skinManager.setSkinFromName(player, input, true);
                remove(player);
            }

            case CHANGE_SKIN_OTHER -> {
                if (currentStep == 1) {
                    firstInput.put(player.getUniqueId(), input);
                    step.put(player.getUniqueId(), 2);
                    sendInstruction(player, inputType, 2);
                } else {
                    final String skinName = firstInput.get(player.getUniqueId());
                    final String targetName = input.trim();
                    final Player target = Bukkit.getPlayerExact(targetName);

                    if (target == null || !target.isOnline()) {
                        player.sendMessage(langManager().translate("messages.player-not-found", Map.of(
                                "ph1", targetName
                        )));
                        remove(player);
                        return;
                    }

                    skinManager().setSkinFromName(player, target, skinName, true);
                    remove(player);
                }
            }

            case ADD_FAVORITE -> {
                favoritesManager().addFavorite(player, input, true);
                remove(player);
            }

            case REMOVE_FAVORITE -> {
                favoritesManager().removeFavorite(player, input, true);
                remove(player);
            }

            case RESET_SKIN_OTHER -> {
                final String targetName = input.trim();
                final Player target = Bukkit.getPlayerExact(targetName);

                if (target == null) {
                    player.sendMessage(langManager().translate("messages.player-not-found", Map.of(
                            "ph1", targetName
                    )));
                } else {
                    skinManager().resetSkin(player, target, true);
                }

                remove(player);
            }

            case RANDOM_SKIN_OTHER -> {
                final String targetName = input.trim();
                final Player target = Bukkit.getPlayerExact(targetName);

                if (target == null) {
                    player.sendMessage(langManager().translate("messages.player-not-found", Map.of(
                            "ph1", targetName
                    )));
                } else {
                    skinManager().applyRandomSkin(player, target, true);
                }

                remove(player);
            }

            case REMOVE_SKIN_FROM_CACHE -> {
                final String skinName = input.trim();

                boolean wasCached = plugin.cacheManager.removeSkin(skinName);

                if (wasCached)
                    player.sendMessage(langManager().translate("messages.skin-removed-from-cache", Map.of(
                            "ph1", skinName.toLowerCase()
                    )));
                else
                    player.sendMessage(langManager().translate("messages.not-cached", Map.of(
                            "ph1", skinName.toLowerCase()
                    )));

                remove(player);
            }
        }
    }

    private void sendInstruction(@NotNull final Player player, @NotNull final SkinMorphInputType inputType, final int step)
    {
        switch (inputType) {
            case CHANGE_SKIN_SELF, ADD_FAVORITE, REMOVE_FAVORITE, REMOVE_SKIN_FROM_CACHE ->
                player.sendMessage(langManager().translate("messages.enter-skin-name"));

            case CHANGE_SKIN_OTHER -> {
                if (step == 1)
                    player.sendMessage(langManager().translate("messages.enter-skin-name"));
                else
                    player.sendMessage(langManager().translate("messages.enter-target-name"));
            }

            case RESET_SKIN_OTHER, RANDOM_SKIN_OTHER ->
                player.sendMessage(langManager().translate("messages.enter-target-name"));
        }
    }

    private void remove(final Player player)
    {
        awaitingInput.remove(player.getUniqueId());
        firstInput.remove(player.getUniqueId());
        step.remove(player.getUniqueId());

        BukkitTask task = timeoutTasks.remove(player.getUniqueId());
        if (task != null && !task.isCancelled())
            task.cancel();
    }

    private SkinManager skinManager()
    {
        return plugin.skinManager;
    }

    private FavoritesManager favoritesManager()
    {
        return plugin.favoritesManager;
    }

    private LanguageManager langManager()
    {
        return plugin.langManager;
    }
}
