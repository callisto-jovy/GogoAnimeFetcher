/*
 * Copyright (c) 2021. Roman P.
 * All code is owned by Roman P. APIs are mentioned.
 * Last modified: 02.06.21, 21:56
 */

package net.bplaced.abzzezz.gogoanime.util;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ArrayHelper {
    /**
     * Counts all the "truths" in a given array
     *
     * @param array array to examine
     * @return the total amount of "true" indices
     */
    public static int getTotalTruthsInArray(final boolean[] array) {
        int i = 0;
        for (final boolean b : array) {
            if (b) i++;
        }
        return i;
    }

    /**
     * Counts all the ... in the given array
     *
     * @param array array to examine
     * @return the total amount of "true" indices
     */
    public static <V> int getTotalInArray(final V[] array, final Predicate<V> predicate) {
        int i = 0;
        for (final V v0 : array) {
            if (predicate.test(v0)) i++;
        }
        return i;
    }

    public static Map<String, String> stringArrayToMap(final String[]... requestProperties) {
        return Arrays.stream(requestProperties)
                .collect(
                        Collectors.toMap(
                                key -> key[0],
                                value -> value[1])
                );
    }

}
