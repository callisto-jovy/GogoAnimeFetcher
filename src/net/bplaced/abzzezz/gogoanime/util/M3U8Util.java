/*
 * Copyright (c) 2021. Roman P.
 * All code is owned by Roman P. APIs are mentioned.
 * Last modified: 03.04.21, 18:49
 */

package net.bplaced.abzzezz.gogoanime.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class M3U8Util {

    public static int getSegments(final String url, final String[][] requestHeaders) throws IOException {
        final boolean isHTTPS = url.startsWith("https://");

        final URLConnection urlConnection;

        if (isHTTPS)
            urlConnection = URLUtil.createHTTPSURLConnection(url, requestHeaders);
        else
            urlConnection = URLUtil.createHTTPURLConnection(url, requestHeaders);

        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));

        try (final Stream<String> lines = bufferedReader.lines()) {
            return (int) lines.filter(s -> s.startsWith("#EXTINF:")).count();
        }
    }

}
