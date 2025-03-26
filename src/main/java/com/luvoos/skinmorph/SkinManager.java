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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SkinManager {

    private final SkinMorph plugin = SkinMorph.instance;
    public static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    public static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    public static final Map<String, Collection<ProfileProperty>> cache = new ConcurrentHashMap<>();
    public static final Map<UUID, String> currentSkins = new ConcurrentHashMap<>();

    public void setSkinFromName(@NotNull Player target, @NotNull String skinName, boolean sendResponse)
    {
        if (skinName.isEmpty() || !target.isOnline()) return;
        String noSpacesSkinName = skinName.replace(" ", "");

        if (wearsSkin(target, skinName)) {
            if (sendResponse) target.sendMessage("§aYou're already wearing that skin! ('§e" + noSpacesSkinName.toLowerCase() + "§a')");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final PlayerProfile profile = target.getPlayerProfile();
                Collection<ProfileProperty> textures = getTextures(noSpacesSkinName, 2);

                if (textures == null) {
                    if (sendResponse) target.sendMessage("§cInvalid profile! ('§e" + noSpacesSkinName.toLowerCase() + "§c')");
                    return;
                }

                profile.setProperties(textures);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    target.setPlayerProfile(profile);
                    setCurrentSkin(target, noSpacesSkinName);
                    if (sendResponse) target.sendMessage("§aYour skin has been changed to §e" + noSpacesSkinName.toLowerCase());
                });

            } catch (Exception e) {
                if (sendResponse) target.sendMessage("§cError changing skin: " + e.getMessage());
            }
        });
    }


    public void setSkinFromName(@NotNull Player target, @NotNull String skinName, boolean sendResponse, int tries)
    {
        if (skinName.isEmpty() || !target.isOnline()) return;
        String noSpacesSkinName = skinName.replace(" ", "");

        if (wearsSkin(target, skinName)) {
            if (sendResponse) target.sendMessage("§aYou're already wearing that skin! ('§e" + noSpacesSkinName.toLowerCase() + "§a')");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final PlayerProfile profile = target.getPlayerProfile();
                Collection<ProfileProperty> textures = getTextures(noSpacesSkinName, tries);

                if (textures == null) {
                    if (sendResponse) target.sendMessage("§cInvalid profile! ('§e" + noSpacesSkinName.toLowerCase() + "§c')");
                    return;
                }

                profile.setProperties(textures);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    target.setPlayerProfile(profile);
                    setCurrentSkin(target, noSpacesSkinName);
                    if (sendResponse) target.sendMessage("§aYour skin has been changed to §e" + noSpacesSkinName.toLowerCase());
                });

            } catch (Exception e) {
                if (sendResponse) target.sendMessage("§cError changing skin: " + e.getMessage());
            }
        });
    }

    public void setSkinFromName(@NotNull CommandSender caller, @NotNull Player target, @NotNull String skinName, boolean sendResponse)
    {
        if (skinName.isEmpty() || !target.isOnline()) return;
        String noSpacesSkinName = skinName.replace(" ", "");

        if (wearsSkin(target, skinName)) {
            if (sendResponse) target.sendMessage("§aYou're already wearing that skin! ('§e" + noSpacesSkinName.toLowerCase() + "§a')");
            if (sendResponse) caller.sendMessage("§a" + target.getName() + " are already wearing that skin! ('§e" + noSpacesSkinName.toLowerCase() + "§a')");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final PlayerProfile profile = target.getPlayerProfile();
                Collection<ProfileProperty> textures = getTextures(noSpacesSkinName, 2);

                if (textures == null) {
                    if (sendResponse) target.sendMessage("§cInvalid profile! ('§e" + noSpacesSkinName.toLowerCase() + "§c')");
                    return;
                }

                profile.setProperties(textures);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    target.setPlayerProfile(profile);
                    setCurrentSkin(target, noSpacesSkinName);
                    if (sendResponse) target.sendMessage("§aYour skin has been changed to §e" + noSpacesSkinName.toLowerCase());
                    if (sendResponse) caller.sendMessage("§a" + target.getName() + "'s skin has been changed to §e" + noSpacesSkinName.toLowerCase());
                });

            } catch (Exception e) {
                if (sendResponse) target.sendMessage("§cError changing skin: " + e.getMessage());
            }
        });
    }

    public void setSkinFromName(@NotNull CommandSender caller, @NotNull Player target, @NotNull String skinName, boolean sendResponse, int tries)
    {
        if (skinName.isEmpty() || !target.isOnline()) return;
        String noSpacesSkinName = skinName.replace(" ", "");

        if (wearsSkin(target, skinName)) {
            if (sendResponse) target.sendMessage("§aYou're already wearing that skin! ('§e" + noSpacesSkinName.toLowerCase() + "§a')");
            if (sendResponse) caller.sendMessage("§a" + target.getName() + " are already wearing that skin! ('§e" + noSpacesSkinName.toLowerCase() + "§a')");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final PlayerProfile profile = target.getPlayerProfile();
                Collection<ProfileProperty> textures = getTextures(noSpacesSkinName, tries);

                if (textures == null) {
                    if (sendResponse) target.sendMessage("§cInvalid profile! ('§e" + noSpacesSkinName.toLowerCase() + "§c')");
                    return;
                }

                profile.setProperties(textures);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    target.setPlayerProfile(profile);
                    setCurrentSkin(target, noSpacesSkinName);
                    if (sendResponse) target.sendMessage("§aYour skin has been changed to §e" + noSpacesSkinName.toLowerCase());
                    if (sendResponse) caller.sendMessage("§a" + target.getName() + "'s skin has been changed to §e" + noSpacesSkinName.toLowerCase());
                });

            } catch (Exception e) {
                if (sendResponse) target.sendMessage("§cError changing skin: " + e.getMessage());
            }
        });
    }

    public void resetSkin(@NotNull Player player, boolean sendResponse)
    {
        setSkinFromName(player, player.getName().toLowerCase(), false);
        player.closeInventory();
        if (sendResponse) player.sendMessage("§aYour skin has been reset to your §edefault skin");
    }

    public CompletableFuture<Collection<ProfileProperty>> getTexturesAsync(String skinName, int tries)
    {
        return CompletableFuture.supplyAsync(() -> getTextures(skinName, tries));
    }

    private Collection<ProfileProperty> getTextures(String skinName, int tries)
    {
        skinName = skinName.toLowerCase();
        if (cache.containsKey(skinName)) return cache.get(skinName);

        int retryDelay = 1000;

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

                return List.of(profileProperty);
            } catch (JsonSyntaxException | IllegalStateException e) {
                if (plugin.DEBUG) plugin.getLogger().warning("Failed to get textures for '" + skinName + "'! -> " + e.getMessage());
            } catch (Exception e) {
                if (plugin.DEBUG) plugin.getLogger().warning("Unexpected error while trying to get textures for '" + skinName + "'! -> " + e.getMessage());
            }
        }

        if (plugin.DEBUG) plugin.getLogger().warning("Failed to get textures for " + skinName + " after " + tries + " unsuccessful attempts!");
        return null;
    }

    public String makeRequest(String url)
    {
        try {
            final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            if (plugin.DEBUG) plugin.getLogger().warning("Request failed for " + url + " -> " + e.getMessage());
            return "";
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
        return currentSkins.getOrDefault(uuid, null);
    }

    public boolean wearsSkin(@NotNull final Player player, @NotNull String skinName)
    {
        final UUID uuid = player.getUniqueId();
        final String noSpacesSkinName = skinName.replace(" ", "");
        return currentSkins.containsKey(uuid) && currentSkins.get(uuid).equalsIgnoreCase(noSpacesSkinName);
    }
}
