package com.luvoos.skinmorph;

import com.destroystokyo.paper.profile.ProfileProperty;

public class SkinData {

    private final String username;
    private final ProfileProperty textureProperty;
    private final long lastUpdated;

    public SkinData(String username, ProfileProperty textureProperty, long lastUpdated) {
        this.username = username;
        this.textureProperty = textureProperty;
        this.lastUpdated = lastUpdated;
    }

    public String getUsername() {
        return username;
    }

    public ProfileProperty getTextureProperty() {
        return textureProperty;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public boolean isDirty(long expiryMs) {
        long diff = System.currentTimeMillis() - lastUpdated;
        return diff > expiryMs;
    }
}