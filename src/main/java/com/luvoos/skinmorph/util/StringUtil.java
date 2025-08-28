package com.luvoos.skinmorph.util;

public class StringUtil {

    /**
     * Normalize a string by putting it in lowercase and removing spaces
     * @param s
     * @return Normalized string
     */
    public static String normalize(String s)
    {
        s = s.toLowerCase();
        s.replace(" ", "");
        return s;
    }
}
