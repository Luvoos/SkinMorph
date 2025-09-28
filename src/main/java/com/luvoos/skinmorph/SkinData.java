package com.luvoos.skinmorph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SkinData
{
    private String username;
    private String textureValue;
    private String textureSignature;
    private Long lastUpdated;
}