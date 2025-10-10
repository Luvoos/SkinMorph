package com.luvoos.skinmorph;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.luvoos.skinmorph.util.FormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SkinManager
{
    private final SkinMorph plugin = SkinMorph.instance;

    public static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    public static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static final Map<UUID, String> currentSkins = new ConcurrentHashMap<>();

    //private static final ExecutorService requestExecutor = Executors.newFixedThreadPool(3);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    /*
    ----------
    Public API
    ----------
     */

    /* setSkinFromName overloads */
    public void setSkinFromName(@NotNull final Player target, @NotNull String skinName, boolean playerFeedback)
    {
        setSkinFromName(target, skinName, playerFeedback, 3);
    }

    public void setSkinFromName(@NotNull final Player target, @NotNull String skinName, boolean playerFeedback, int attempts)
    {
        setSkinFromNameInternal(null, target, skinName, playerFeedback, attempts);
    }

    public void setSkinFromName(@NotNull final CommandSender caller, @NotNull final Player target, @NotNull String skinName, boolean playerFeedback)
    {
        setSkinFromName(caller, target, skinName, playerFeedback, 3);
    }

    public void setSkinFromName(@NotNull final CommandSender caller, @NotNull final Player target, @NotNull String skinName, boolean playerFeedback, int attempts)
    {
        setSkinFromNameInternal(caller, target, skinName, playerFeedback, attempts);
    }

    public void setSkinFromName(@NotNull final Player caller, @NotNull final Player target, @NotNull String skinName, boolean playerFeedback)
    {
        setSkinFromName(caller, target, skinName, playerFeedback, 3);
    }

    public void setSkinFromName(@NotNull final Player caller, @NotNull final Player target, @NotNull String skinName, boolean playerFeedback, int attempts)
    {
        setSkinFromNameInternal(caller, target, skinName, playerFeedback, attempts);
    }

    /* resetSkin overloads */
    public void resetSkin(@NotNull final Player target, boolean playerFeedback)
    {
        resetSkinInternal(null, target, playerFeedback);
    }

    public void resetSkin(@NotNull final CommandSender caller, @NotNull final Player target, boolean playerFeedback)
    {
        resetSkinInternal(caller, target, playerFeedback);
    }

    public void resetSkin(@NotNull final Player caller, @NotNull final Player target, boolean playerFeedback)
    {
        resetSkinInternal(caller, target, playerFeedback);
    }

    /* applyRandomSkin overloads */
    public void applyRandomSkin(@NotNull final Player target, boolean playerFeedback)
    {
        applyRandomSkinInternal(null, target, playerFeedback);
    }

    public void applyRandomSkin(@NotNull final CommandSender caller, @NotNull final Player target, boolean playerFeedback)
    {
        applyRandomSkinInternal(caller, target, playerFeedback);
    }

    public void applyRandomSkin(@NotNull final Player caller, @NotNull final Player target, boolean playerFeedback)
    {
        applyRandomSkinInternal(caller, target, playerFeedback);
    }

    /*
    ------------------------
    Internal implementations
    ------------------------
     */

    private void setSkinFromNameInternal(@Nullable final CommandSender caller,
                                         @NotNull final Player target,
                                         @NotNull final String skinName,
                                         boolean playerFeedback,
                                         int attempts)
    {
        if (skinName.isEmpty() || !target.isOnline()) return;

        final String normalizedSkinName = FormatUtil.normalize(skinName);

        if (isWearingSkin(target, skinName)) {
            if (plugin.PLAYER_FEEDBACK && playerFeedback) {
                if (caller == null) {
                    target.sendMessage("§aYou're already wearing that skin! ('§e" + normalizedSkinName + "§a')");
                } else {
                    caller.sendMessage("§a" + target.getName() + " is already wearing that skin! ('§e" + normalizedSkinName + "§a')");
                }
            }
            return;
        }

        getSkinAsync(skinName, attempts).whenComplete((skin, t) -> {
            if (t != null) {
                if (plugin.DEBUG) plugin.getLogger().warning("Error fetching skin '" + normalizedSkinName + "' -> " + t.getMessage());
                if (plugin.PLAYER_FEEDBACK && playerFeedback) {
                    final String msg = "§cError changing skin: " + t.getMessage();
                    if (caller == null) target.sendMessage(msg); else caller.sendMessage(msg);
                }
                return;
            }

            if (skin == null) {
                if (plugin.PLAYER_FEEDBACK && playerFeedback) {
                    final String msg = "§cInvalid profile! ('§e" + normalizedSkinName + "§c')";
                    if (caller == null) target.sendMessage(msg); else caller.sendMessage(msg);
                }
                return;
            }

            // Apply skin on main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    PlayerProfile profile = target.getPlayerProfile();
                    ProfileProperty textureProp = skin.getTextureProperty();
                    profile.setProperty(textureProp);



                    target.setPlayerProfile(profile);
                    setCurrentSkin(target, normalizedSkinName);
                    plugin.selectedSkinsManager.setSelectedSkin(target, normalizedSkinName);

                    if (plugin.PLAYER_FEEDBACK && playerFeedback) {
                        target.sendMessage("§aYour skin has been changed to §e" + normalizedSkinName + "§a!");
                        if (caller != null) {
                            boolean endsWithS = target.getName().endsWith("s") || target.getName().endsWith("S");
                            caller.sendMessage("§a" + target.getName() + (endsWithS ? "'" : "'s") + " skin has been changed to §e" + normalizedSkinName + "§a!");
                        }
                    }
                } catch (Exception e) {
                    if (plugin.DEBUG) plugin.getLogger().warning("Failed to apply skin '" + normalizedSkinName + "' to player '" + target.getName() + "' -> " + e.getMessage());
                    if (plugin.PLAYER_FEEDBACK && playerFeedback) {
                        final String msg = "§cError changing skin: " + e.getMessage();
                        if (caller == null) target.sendMessage(msg); else caller.sendMessage(msg);
                    }
                }
            });
        });
    }

    private void resetSkinInternal(@Nullable final CommandSender caller, @NotNull final Player target, boolean playerFeedback)
    {
        final String defaultSkinName = target.getName().toLowerCase();
        if (isWearingSkin(target, defaultSkinName)) {
            if (plugin.PLAYER_FEEDBACK && playerFeedback) {
                if (caller == null) {
                    target.sendMessage("§aYou're already wearing your §edefault skin§a!");
                } else {
                    caller.sendMessage("§a" + target.getName() + " is already wearing their §edefault skin§a!");
                }
            }
            return;
        }

        setSkinFromNameInternal(caller, target, defaultSkinName, false, 3);
        if (plugin.PLAYER_FEEDBACK && playerFeedback) {
            boolean endsWithS = target.getName().endsWith("s") || target.getName().endsWith("S");
            if (caller == null) {
                target.sendMessage("§aYour skin has been reset to your §edefault skin§a!");
            } else {
                target.sendMessage("§aYour skin has been reset to your §edefault skin§a!");
                caller.sendMessage("§a" + target.getName() + (endsWithS ? "'" : "'s") + " skin has been reset to their §edefault skin§a!");
            }
        }
    }

    private void applyRandomSkinInternal(@Nullable final CommandSender caller, @NotNull final Player target, boolean playerFeedback)
    {
        String randomSkin = plugin.favoritesManager.getRandomFavorite(target);
        if (randomSkin.isEmpty()) {
            if (plugin.PLAYER_FEEDBACK && playerFeedback) {
                final String msg = "§cYou must have at least §e2 §cfavorites to do that!";
                if (caller == null) target.sendMessage(msg); else caller.sendMessage(msg);
            }
            return;
        }

        setSkinFromNameInternal(caller, target, randomSkin, false, 3);

        if (plugin.PLAYER_FEEDBACK && playerFeedback) {
            String msg = "§aRandomly picked and applied skin §e" + randomSkin.toLowerCase() + "§a!";
            if (caller == null) {
                target.sendMessage(msg);
            } else {
                target.sendMessage(msg);
                caller.sendMessage(msg);
            }
        }
    }

    public CompletableFuture<SkinData> getSkinAsync(String skinName, int attempts)
    {
        final String normalizedSkinName = FormatUtil.normalize(skinName);

        final SkinData cached = plugin.cacheManager.getSkin(normalizedSkinName);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        if (normalizedSkinName.length() > 16) {
            plugin.getLogger().warning("'" + normalizedSkinName + "' exceeds the maximum username length (16)");
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> null)
                                .thenCompose(ignored -> fetchSkinAttempt(normalizedSkinName, attempts));
    }

    private CompletableFuture<SkinData> fetchSkinAttempt(final String skinName, int attemptsLeft)
    {
        if (attemptsLeft < 1) return CompletableFuture.completedFuture(null);

        final String normalizedSkinName = FormatUtil.normalize(skinName);

        final HttpRequest profileReq = HttpRequest.newBuilder()
                .uri(URI.create(PROFILE_URL + normalizedSkinName))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        return HTTP_CLIENT.sendAsync(profileReq, HttpResponse.BodyHandlers.ofString())
                .thenCompose(profileRes -> {
                    if (plugin.DEBUG) plugin.getLogger().info("New profile request being sent out: " + profileReq.uri());

                    String profileBody = profileRes.body();
                    int profileStatus = profileRes.statusCode();

                    if (profileStatus != 200 || profileBody == null || profileBody.startsWith("<!DOCTYPE")) {
                        if (plugin.DEBUG) plugin.getLogger().warning("Invalid profile response for '" + normalizedSkinName + "' -> " + profileStatus);
                        return (attemptsLeft > 1) ? scheduleRetry(normalizedSkinName, attemptsLeft)
                                                  : CompletableFuture.completedFuture(null);
                    }

                    JsonObject profileObj;
                    try {
                        profileObj = JsonParser.parseString(profileBody).getAsJsonObject();
                    } catch (JsonSyntaxException | IllegalStateException e) {
                        if (plugin.DEBUG) plugin.getLogger().warning("Failed to parse profile JSON for '" + normalizedSkinName + "'");
                        return (attemptsLeft > 1) ? scheduleRetry(normalizedSkinName, attemptsLeft)
                                                  : CompletableFuture.completedFuture(null);
                    }

                    if (!profileObj.has("id")) {
                        if (plugin.DEBUG) plugin.getLogger().warning("Missing 'id' field in profile response for '" + normalizedSkinName + "'");
                        return (attemptsLeft > 1) ? scheduleRetry(normalizedSkinName, attemptsLeft)
                                                  : CompletableFuture.completedFuture(null);
                    }

                    String uuid = profileObj.get("id").getAsString();
                    final HttpRequest skinReq = HttpRequest.newBuilder()
                            .uri(URI.create(String.format(SKIN_URL, uuid)))
                            .timeout(REQUEST_TIMEOUT)
                            .GET()
                            .build();

                    return HTTP_CLIENT.sendAsync(skinReq, HttpResponse.BodyHandlers.ofString())
                            .thenCompose(skinRes -> {
                                if (plugin.DEBUG) plugin.getLogger().info("New skin request being sent out: " + skinReq.uri());
                                String skinBody = skinRes.body();
                                int skinStatus = skinRes.statusCode();

                                if (skinStatus != 200 || skinBody == null || skinBody.startsWith("<!DOCTYPE")) {
                                    if (plugin.DEBUG) plugin.getLogger().warning("Invalid skin response for '" + normalizedSkinName + "' -> " + skinStatus);
                                    return (attemptsLeft > 1) ? scheduleRetry(normalizedSkinName, attemptsLeft)
                                                              : CompletableFuture.completedFuture(null);
                                }

                                try {
                                    JsonObject skinObj = JsonParser.parseString(skinBody).getAsJsonObject();
                                    JsonArray props = skinObj.getAsJsonArray("properties");
                                    if (props == null || props.isEmpty()) {
                                        if (plugin.DEBUG) plugin.getLogger().warning("Missing 'properties' in skin response for '" + normalizedSkinName + "'");
                                        return (attemptsLeft > 1) ? scheduleRetry(normalizedSkinName, attemptsLeft)
                                                                  : CompletableFuture.completedFuture(null);
                                    }

                                    JsonObject prop = props.get(0).getAsJsonObject();
                                    final String value = prop.has("value") ? prop.get("value").getAsString() : null;
                                    final String signature = prop.has("signature") ? prop.get("signature").getAsString() : null;

                                    if (value == null || signature == null) {
                                        if (plugin.DEBUG) plugin.getLogger().warning("Missing value or signature in skin property for '" + normalizedSkinName + "'");
                                        return (attemptsLeft > 1) ? scheduleRetry(normalizedSkinName, attemptsLeft)
                                                                  : CompletableFuture.completedFuture(null);
                                    }

                                    ProfileProperty textureProp = new ProfileProperty(
                                            "textures",
                                            value,
                                            signature
                                    );

                                    final SkinData skinData = new SkinData(normalizedSkinName, textureProp, System.currentTimeMillis());
                                    plugin.cacheManager.updateCache(skinData);

                                    if (plugin.DEBUG) plugin.getLogger().info("Successfully fetched textures for '" + normalizedSkinName + "'");

                                    return CompletableFuture.completedFuture(skinData);
                                } catch (JsonSyntaxException | IllegalStateException e) {
                                    if (plugin.DEBUG) plugin.getLogger().warning("Failed to parse skin JSON for '" + normalizedSkinName + "'");
                                    return (attemptsLeft > 1) ? scheduleRetry(normalizedSkinName, attemptsLeft)
                                                              : CompletableFuture.completedFuture(null);
                                }
                            });
                })
                .exceptionally(e -> {
                    if (plugin.DEBUG) plugin.getLogger().warning("Unexpected error occurred while fetching '" + normalizedSkinName + "' -> " + e.getMessage());
                    return null;
                });
    }

    private CompletableFuture<SkinData> scheduleRetry(String skinName, int attemptsLeft)
    {
        final String normalizedSkinName = FormatUtil.normalize(skinName);
        if (attemptsLeft < 2) return CompletableFuture.completedFuture(null);
        long delayMs = Math.max(100, plugin.RETRY_DELAY);
        if (plugin.DEBUG) plugin.getLogger().info("Scheduling retry for '" + normalizedSkinName + "', attempts left: " + (attemptsLeft - 1));
        return CompletableFuture.supplyAsync(
                () -> null,
                CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS)
        ).thenCompose(ignored -> fetchSkinAttempt(normalizedSkinName, attemptsLeft - 1));
    }

    public void setCurrentSkin(@NotNull final Player player, @NotNull final String skinName)
    {
        final UUID uuid = player.getUniqueId();
        final String normalizedSkinName = FormatUtil.normalize(skinName);
        currentSkins.put(uuid, normalizedSkinName);
    }

    @Nullable
    public String getCurrentSkin(@NotNull final Player player)
    {
        final UUID uuid = player.getUniqueId();
        return currentSkins.get(uuid);
    }

    public boolean isWearingSkin(@NotNull final Player player, @NotNull final String skinName)
    {
        final UUID uuid = player.getUniqueId();
        final String normalizedSkinName = FormatUtil.normalize(skinName);
        return currentSkins.containsKey(uuid) && currentSkins.get(uuid).equals(normalizedSkinName);
    }

    public void clearCache()
    {
        CacheManager.cache.clear();
    }
}