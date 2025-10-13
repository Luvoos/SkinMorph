package com.luvoos.skinmorph;

import com.destroystokyo.paper.profile.ProfileProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SkinData
{
    private String username;
    private ProfileProperty textureProperty;
    private Long lastUpdated;

    public boolean isDirty(final long expiryMs)
    {
        long diff = System.currentTimeMillis() - getLastUpdated();
        return diff > expiryMs;
    }
}