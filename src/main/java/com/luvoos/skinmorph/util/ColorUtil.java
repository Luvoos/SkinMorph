package com.luvoos.skinmorph.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ColorUtil
{
    private ColorUtil() {}

    public static final String GREEN = "#39BD45";
    public static final String RED = "#FF5D42";
    public static final String YELLOW = "#FFD46B";

    public static Component colorize(String msg)
    {
        msg = msg
                .replace("<green>", "<color:" + GREEN + ">")
                .replace("</green>", "</color>")
                .replace("<red>", "<color:" + RED + ">")
                .replace("</red>", "</color>")
                .replace("<yellow>", "<color:" + YELLOW + ">")
                .replace("</yellow>", "</color>");

        return MiniMessage.miniMessage().deserialize(msg);
    }
}
