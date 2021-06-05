package net.bplaced.abzzezz.gogoanime.util;

import java.util.function.Predicate;

public class ConsoleUtil {

    public static final char[] CHARS = new char[]{'|', '/', '-', '\\'};

    public static void displayProgressbar(final int progress, final int total) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < (((double) progress / total) * 100.); i++) {
            builder.append('#');
            System.out.print("[" + builder + "] " + i + "% " + CHARS[i % 4] + "\r");
        }
    }

    public static void displayProgressbar(final Predicate<Integer> integerPredicate) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; integerPredicate.test(i); i++) {
            builder.append('#');
            System.out.print("[" + builder + "] " + CHARS[i % 4] + "\r");
        }
    }
}
