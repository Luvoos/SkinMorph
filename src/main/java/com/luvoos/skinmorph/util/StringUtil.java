package com.luvoos.skinmorph.util;

public class StringUtil {

    /**
     * Normalize a string by putting it in lower-case and removing spaces
     * @param s
     * @return
     */
    public static String normalize(String s)
    {
        s = s.toLowerCase();
        s.replace(" ", "");
        return s;
    }
}
