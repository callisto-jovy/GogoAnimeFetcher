package net.bplaced.abzzezz.gogoanime.util;

public class StringUtil {

    /**
     * Removes windows disallowed characters
     *
     * @param string String to be sanitized
     * @return sanitized string
     */
    public static String sanitizeString(String string) {
        if (string == null) return "";
        return string.replaceAll("[\u0000-\u001f<>:\"/\\\\|?*\u007f]+", "").trim();
    }

    public static String extractNumber(String str) {
        return str.replaceAll("[^0-9\\.]", "");
    }

}
