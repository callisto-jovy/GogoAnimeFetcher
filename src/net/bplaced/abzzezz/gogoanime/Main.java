package net.bplaced.abzzezz.gogoanime;

import net.bplaced.abzzezz.gogoanime.tasks.EpisodeDownloadTask;
import net.bplaced.abzzezz.gogoanime.util.Constant;
import net.bplaced.abzzezz.gogoanime.util.cache.CacheUtil;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    private final CacheUtil cacheUtil = new CacheUtil(Constant.MAIN_DIR);

    public static void main(String[] args) {
        if (!Constant.MAIN_DIR.exists())
            Constant.MAIN_DIR.mkdirs();

        final Main main = new Main();
        main.mainMenu();
    }

    public void mainMenu() {
        final String logo = "" +
                ":::'###::::'##::: ##:'####:'##::::'##:'########:'##::::::::'##:::'##::'#######::'##::::'##:\n" +
                "::'## ##::: ###:: ##:. ##:: ###::'###: ##.....:: ##:::'##::. ##:'##::'##.... ##: ##:::: ##:\n" +
                ":'##:. ##:: ####: ##:: ##:: ####'####: ##::::::: ##::: ##:::. ####::: ##:::: ##: ##:::: ##:\n" +
                "##:::. ##: ## ## ##:: ##:: ## ### ##: ######::: ##::: ##::::. ##:::: ##:::: ##: ##:::: ##:\n" +
                "#########: ##. ####:: ##:: ##. #: ##: ##...:::: #########:::: ##:::: ##:::: ##: ##:::: ##:\n" +
                "##.... ##: ##:. ###:: ##:: ##:.:: ##: ##:::::::...... ##::::: ##:::: ##:::: ##: ##:::: ##:\n" +
                "##:::: ##: ##::. ##:'####: ##:::: ##: ########::::::: ##::::: ##::::. #######::. #######::\n" +
                "..:::::..::..::::..::....::..:::::..::........::::::::..::::::..::::::.......::::.......:::";

        print(logo);

        cacheUtil.loadCache();
        Runtime.getRuntime().addShutdownHook(new Thread(cacheUtil::flushCache));

        final String[] options = new String[]{"Download", "Options", "Exit"};

        list(options);

        final Scanner scanner = new Scanner(System.in);

        final String input = scanner.nextLine();
        switch (input) {
            case "0":
                download();
                break;
            case "1":
                options();
                break;
            case "2":
                System.exit(0);
                break;
            default:
                print("Please only enter a valid number");
                break;
        }

    }

    private void options() {
        final String[] options = new String[]{"File output", "Time delay", "Return"};
        list(options);

        final Scanner scanner = new Scanner(System.in);

        final String input = scanner.nextLine();
        switch (input) {
            case "0":
                print("Please enter the path");
                final String path = scanner.nextLine();
                cacheUtil.cacheObject("outDir", path);
                break;
            case "1":
                print("Please enter the delay (in ms)");
                final int delay = scanner.nextInt();
                cacheUtil.cacheObject("delay", delay);
                break;
            case "2":
                mainMenu();
                break;
            default:
                print("Please only enter a valid number");
                break;
        }
        options();
    }

    private void download() {
        final Scanner scanner = new Scanner(System.in);
        print("Please enter the show's name");

        try {
            final String[] searchQueryResults = GogoAnimeFetcher.getURLsFromSearch(scanner.nextLine());

            for (int i = 0; i < searchQueryResults.length; i++) {
                print(String.format("(%d) %s With episodes: %d", i, searchQueryResults[i], searchQueryResults.length));
            }

            final int index = Integer.parseInt(scanner.nextLine());
            if (index < 0 || index >= searchQueryResults.length) {
                print("Index out of bounds");
                return;
            }
            final GogoAnimeFetcher gogoAnimeFetcher = new GogoAnimeFetcher(searchQueryResults[index]);

            downloadEpisodes(gogoAnimeFetcher.getFetchedReferrals(), new int[]{0, gogoAnimeFetcher.getFetchedReferrals().length(), 0});
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void downloadEpisodes(final JSONArray array, final int[] count) throws IOException {
        print((String) cacheUtil.getFromCacheOrCache("outDir", "outDir"));

        new EpisodeDownloadTask(array, new File((String) cacheUtil.getFromCacheOrCache("outDir", "outDir")), count, new EpisodeDownloadTask.EpisodeDownloadCallback() {
            @Override
            public void onDownloadCompleted(int[] count) {
                try {
                    downloadEpisodes(array, count);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorThrown(String message) {

            }
        }).executeAsync();
    }

    private void list(final String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            print(String.format("(%d) %s", i, strings[i]));
        }
    }

    private void print(final String s) {
        System.out.println(s);
    }

}
