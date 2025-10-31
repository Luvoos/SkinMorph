package com.luvoos.skinmorph.util;

public final class FormatUtil
{
    private FormatUtil() {}

    /**
     * Normalizes a string by putting it in lowercase and removing spaces
     * @param s String to be normalized
     * @return Normalized string
     */
    public static String normalize(String s)
    {
        s = s.toLowerCase();
        s = s.replace(" ", "");
        return s;
    }

    /**
     * Converts bytes to human-readable format (e.g. '42 MB')
     * @param bytes Number of bytes to convert
     * @return Byte count with appropriate unit
     */
    public static String humanReadableByteCount(long bytes)
    {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
