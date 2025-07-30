package com.luvoos.skinmorph;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SkinManager {

    private final SkinMorph plugin = SkinMorph.instance;
    public static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    public static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).connectTimeout(Duration.ofSeconds(10)).build();
    public static final Map<String, Collection<ProfileProperty>> cache = new ConcurrentHashMap<>();
    public static final Map<UUID, String> currentSkins = new ConcurrentHashMap<>();

    public void setSkinFromName(@NotNull Player target, @NotNull String skinName, boolean sendResponse)
    {
        setSkinFromName(target, skinName, sendResponse, 3);
    }

    public void setSkinFromName(@NotNull Player target, @NotNull String skinName, boolean sendResponse, int tries)
    {
        if (skinName.isEmpty() || !target.isOnline()) return;
        String noSpacesSkinName = skinName.replace(" ", "");

        if (wearsSkin(target, skinName)) {
            if (plugin.SEND_RESPONSES && sendResponse) target.sendMessage("§aYou're already wearing that skin! ('§e" + noSpacesSkinName.toLowerCase() + "§a')");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final PlayerProfile profile = target.getPlayerProfile();
                Collection<ProfileProperty> textures = getTextures(noSpacesSkinName, tries);

                if (textures == null) {
                    if (plugin.SEND_RESPONSES && sendResponse) target.sendMessage("§cInvalid profile! ('§e" + noSpacesSkinName.toLowerCase() + "§c')");
                    return;
                }

                profile.setProperties(textures);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    target.setPlayerProfile(profile);
                    setCurrentSkin(target, noSpacesSkinName.toLowerCase());
                    plugin.selectedSkinsManager.setSelectedSkin(target, skinName);
                    if (plugin.SEND_RESPONSES && sendResponse) target.sendMessage("§aYour skin has been changed to §e" + noSpacesSkinName.toLowerCase() + "§a!");
                });

            } catch (Exception e) {
                if (plugin.SEND_RESPONSES && sendResponse) target.sendMessage("§cError changing skin: " + e.getMessage());
            }
        });
    }

    public void setSkinFromName(@NotNull CommandSender caller, @NotNull Player target, @NotNull String skinName, boolean sendResponse)
    {
        setSkinFromName(caller, target, skinName, sendResponse, 3);
    }

    public void setSkinFromName(@NotNull CommandSender caller, @NotNull Player target, @NotNull String skinName, boolean sendResponse, int tries)
    {
        if (skinName.isEmpty() || !target.isOnline()) return;
        String noSpacesSkinName = skinName.replace(" ", "");

        if (wearsSkin(target, skinName)) {
            if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§a" + target.getName() + " is already wearing that skin! ('§e" + noSpacesSkinName.toLowerCase() + "§a')");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final PlayerProfile profile = target.getPlayerProfile();
                Collection<ProfileProperty> textures = getTextures(noSpacesSkinName, tries);

                if (textures == null) {
                    if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§cInvalid profile! ('§e" + noSpacesSkinName.toLowerCase() + "§c')");
                    return;
                }

                profile.setProperties(textures);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    target.setPlayerProfile(profile);
                    setCurrentSkin(target, noSpacesSkinName.toLowerCase());
                    plugin.selectedSkinsManager.setSelectedSkin(target, skinName);
                    if (plugin.SEND_RESPONSES && sendResponse) target.sendMessage("§aYour skin has been changed to §e" + noSpacesSkinName.toLowerCase() + "§a!");
                    boolean endsWithS = target.getName().endsWith("s") || target.getName().endsWith("S");
                    if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§a" + target.getName() + (endsWithS ? "'" : "'s") + " skin has been changed to §e" + noSpacesSkinName.toLowerCase() + "§a!");
                });

            } catch (Exception e) {
                if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§cError changing skin: " + e.getMessage());
            }
        });
    }

    public void setSkinFromName(@NotNull Player caller, @NotNull Player target, @NotNull String skinName, boolean sendResponse)
    {
        setSkinFromName(caller, target, skinName, sendResponse, 3);
    }

    public void setSkinFromName(@NotNull Player caller, @NotNull Player target, @NotNull String skinName, boolean sendResponse, int tries)
    {
        if (skinName.isEmpty() || !target.isOnline()) return;
        String noSpacesSkinName = skinName.replace(" ", "");

        if (wearsSkin(target, skinName)) {
            if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§a" + target.getName() + " is already wearing that skin! ('§e" + noSpacesSkinName.toLowerCase() + "§a')");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final PlayerProfile profile = target.getPlayerProfile();
                Collection<ProfileProperty> textures = getTextures(noSpacesSkinName, tries);

                if (textures == null) {
                    if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§cInvalid profile! ('§e" + noSpacesSkinName.toLowerCase() + "§c')");
                    return;
                }

                profile.setProperties(textures);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    target.setPlayerProfile(profile);
                    setCurrentSkin(target, noSpacesSkinName.toLowerCase());
                    plugin.selectedSkinsManager.setSelectedSkin(target, skinName);
                    if (plugin.SEND_RESPONSES && sendResponse) target.sendMessage("§aYour skin has been changed to §e" + noSpacesSkinName.toLowerCase() + "§a!");
                    boolean endsWithS = target.getName().endsWith("s") || target.getName().endsWith("S");
                    if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§a" + target.getName() + (endsWithS ? "'" : "'s") + " skin has been changed to §e" + noSpacesSkinName.toLowerCase() + "§a!");
                });

            } catch (Exception e) {
                if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§cError changing skin: " + e.getMessage());
            }
        });
    }

    public void resetSkin(@NotNull Player target, boolean sendResponse)
    {
        target.closeInventory();
        if (wearsSkin(target, target.getName().toLowerCase())) {
            if (plugin.SEND_RESPONSES && sendResponse) target.sendMessage("§aYou're already wearing your §edefault skin§a!");
            return;
        }
        setSkinFromName(target, target.getName().toLowerCase(), false);
        if (plugin.SEND_RESPONSES && sendResponse) target.sendMessage("§aYour skin has been reset to your §edefault skin§a!");
    }

    public void resetSkin(@NotNull CommandSender caller, @NotNull Player target, boolean sendResponse)
    {
        if (wearsSkin(target, target.getName().toLowerCase())) {
            if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§a" + target.getName() + " is already wearing their §edefault skin§a!");
            return;
        }
        setSkinFromName(target, target.getName().toLowerCase(), false);
        if (plugin.SEND_RESPONSES && sendResponse) target.sendMessage("§aYour skin has been reset to your §edefault skin§a!");
        boolean endsWithS = target.getName().endsWith("s") || target.getName().endsWith("S");
        if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§a" + target.getName() + (endsWithS ? "'" : "'s") + " skin has been reset to their §edefault skin§a!");
    }

    public void resetSkin(@NotNull Player caller, @NotNull Player target, boolean sendResponse)
    {
        if (wearsSkin(target, target.getName().toLowerCase())) {
            if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§a" + target.getName() + " is already wearing their §edefault skin§a!");
            return;
        }
        setSkinFromName(target, target.getName().toLowerCase(), false);
        if (plugin.SEND_RESPONSES && sendResponse) target.sendMessage("§aYour skin has been reset to your §edefault skin§a!");
        boolean endsWithS = target.getName().endsWith("s") || target.getName().endsWith("S");
        if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§a" + target.getName() + (endsWithS ? "'" : "'s") + " skin has been reset to their §edefault skin§a!");
    }

    public void applyRandomSkin(@NotNull Player target, boolean sendResponse)
    {
        String randomSkin = plugin.favoritesManager.getRandomFavorite(target);
        if (randomSkin.isEmpty()) {
            if (plugin.SEND_RESPONSES && sendResponse) target.sendMessage("§cYou need to have at least §e2 §cfavorites to do that!");
            return;
        }
        setSkinFromName(target, randomSkin, false);
        if (plugin.SEND_RESPONSES && sendResponse) target.sendMessage("§aRandomly picked and applied skin '§e" + randomSkin.toLowerCase() + "§a'!");
    }

    public void applyRandomSkin(@NotNull CommandSender caller, @NotNull Player target, boolean sendResponse)
    {
        String randomSkin = plugin.favoritesManager.getRandomFavorite(target);
        if (randomSkin.isEmpty()) {
            if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§c" + target.getName() + " needs to have at least §e2 §cfavorites to do that!");
            return;
        }
        setSkinFromName(target, randomSkin, false);
        if (plugin.SEND_RESPONSES && sendResponse) target.sendMessage("§aRandomly picked and applied skin '§e" + randomSkin.toLowerCase() + "§a'!");
        if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§aRandomly picked and applied skin '§e" + randomSkin.toLowerCase() + "§a'!");
    }

    public void applyRandomSkin(@NotNull Player caller, @NotNull Player target, boolean sendResponse)
    {
        String randomSkin = plugin.favoritesManager.getRandomFavorite(target);
        if (randomSkin.isEmpty()) {
            if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§c" + target.getName() + " needs to have at least §e2 §cfavorites to do that!");
            return;
        }
        setSkinFromName(target, randomSkin, false);
        if (plugin.SEND_RESPONSES && sendResponse) target.sendMessage("§aRandomly picked and applied skin '§e" + randomSkin.toLowerCase() + "§a'!");
        if (plugin.SEND_RESPONSES && sendResponse) caller.sendMessage("§aRandomly picked and applied skin '§e" + randomSkin.toLowerCase() + "§a'!");
    }

    public CompletableFuture<Collection<ProfileProperty>> getTexturesAsync(String skinName, int tries)
    {
        return CompletableFuture.supplyAsync(() -> getTextures(skinName, tries));
    }

    /**
     * Get textures by skinName (username)
     * @param skinName (username)
     * @param tries How many attempts the plugin should make to retrieve the textures
     * @return Collection of ProfileProperties containing the textures
     */
    private Collection<ProfileProperty> getTextures(String skinName, int tries)
    {
        skinName = skinName.toLowerCase();
        skinName = skinName.replace(" ", "");
        if (cache.containsKey(skinName)) return cache.get(skinName);

        if (skinName.length() > 16) {
            plugin.getLogger().warning("'" + skinName + "' exceeds the maximum username length (16)!");
            return null;
        }

        int retryDelay = 2000;

        for (int attempt = 1; attempt <= tries; attempt++) {
            try {
                final String profileResponse = makeRequest(PROFILE_URL + skinName);
                if (profileResponse == null || profileResponse.startsWith("<!DOCTYPE")) {
                    if (plugin.DEBUG) plugin.getLogger().warning("Received invalid response (HTML or null) for '" + skinName + "'");
                    if (attempt < tries) {
                        Thread.sleep(retryDelay);
                        continue;
                    }
                    return null;
                }
                JsonReader reader = new JsonReader(new StringReader(profileResponse));
                reader.setLenient(true);
                JsonObject profileObject = JsonParser.parseReader(reader).getAsJsonObject();

                if (!profileObject.has("id")) {
                    if (plugin.DEBUG) plugin.getLogger().warning("Profile response does not contain 'id' field for '" + skinName + "'");
                    if (attempt < tries) {
                        Thread.sleep(retryDelay);
                        continue;
                    }
                    return null;
                }

                final String uuid = profileObject.get("id").getAsString();

                final String skinResponse = makeRequest(SKIN_URL.formatted(uuid));
                if (skinResponse == null || skinResponse.startsWith("<!DOCTYPE")) {
                    if (plugin.DEBUG) plugin.getLogger().warning("Received invalid response (HTML or null) for skin data of '" + skinName + "'");
                    if (attempt < tries) {
                        Thread.sleep(retryDelay);
                        continue;
                    }
                    return null;
                }
                final JsonObject skinObject = JsonParser.parseString(skinResponse).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
                final String value = skinObject.get("value").getAsString();
                final String signature = skinObject.get("signature").getAsString();
                final ProfileProperty profileProperty = new ProfileProperty("textures", value, signature);

                cache.put(skinName, List.of(profileProperty));

                if (plugin.DEBUG) plugin.getLogger().info("Successfully loaded textures for '" + skinName + "'!");
                return List.of(profileProperty);
            } catch (JsonSyntaxException | IllegalStateException e) {
                if (plugin.DEBUG) plugin.getLogger().warning("Failed to get textures for '" + skinName + "'! -> " + e.getMessage());
            } catch (Exception e) {
                if (plugin.DEBUG) plugin.getLogger().warning("Unexpected error while trying to get textures for '" + skinName + "'! -> " + e.getMessage());
            }
        }
        return null;
    }

    public String makeRequest(String url)
    {
        try {
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (plugin.DEBUG) plugin.getLogger().warning("Request for " + url + " -> " + response.statusCode());
            return response.body();
        } catch (IOException | InterruptedException e) {
            if (plugin.DEBUG) plugin.getLogger().warning("Request failed for " + url + " -> " + e.getMessage());
            return null;
        }
    }

    public void setCurrentSkin(@NotNull final Player player, @NotNull String skinName)
    {
        final UUID uuid = player.getUniqueId();
        final String noSpacesSkinName = skinName.replace(" ", "");
        currentSkins.put(uuid, noSpacesSkinName.toLowerCase());
    }

    public String getCurrentSkin(@NotNull final Player player)
    {
        final UUID uuid = player.getUniqueId();
        return currentSkins.get(uuid);
    }

    public boolean wearsSkin(@NotNull final Player player, @NotNull String skinName)
    {
        final UUID uuid = player.getUniqueId();
        final String noSpacesSkinName = skinName.replace(" ", "");
        return currentSkins.containsKey(uuid) && currentSkins.get(uuid).equalsIgnoreCase(noSpacesSkinName);
    }
}
