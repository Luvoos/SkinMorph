package com.luvoos.skinmorph;

import com.destroystokyo.paper.profile.ProfileProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@AllArgsConstructor
@Getter
@Setter
public class SkinData
{
    private String username;
    private Collection<ProfileProperty> textureProperties;
    private Long lastUpdated;
}