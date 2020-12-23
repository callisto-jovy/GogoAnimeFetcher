package net.bplaced.abzzezz.gogofetcher;

import java.io.IOException;

public class Main {
    /**
     * Example
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        final GogoAnimeFetcher gogoAnimeFetcher = new GogoAnimeFetcher("https://gogoanime.so/category/darling-in-the-franxx-dub");
        //Download episodes (start to end)
        //gogoAnimeFetcher.fetch(0, 24);
        //Caches the show's cover and returns it's file
        // final File cover = gogoAnimeFetcher.fetchImage();
        //Get all urls to the api call
        //final String[] urls = GogoAnimeFetcher.fetchIDs("url",0 ( start),20 (end) );
    }
}
