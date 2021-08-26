package net.bplaced.abzzezz.gogoanime;

import net.bplaced.abzzezz.gogoanime.tasks.EpisodeDownloadTask;
import net.bplaced.abzzezz.gogoanime.util.ConsoleUtil;
import net.bplaced.abzzezz.gogoanime.util.Constant;
import net.bplaced.abzzezz.gogoanime.util.StringUtil;
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
        final String logo =
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
        ConsoleUtil.createOptionsMenu(options, integer -> {
            switch (integer) {
                case 0:
                    download();
                    break;
                case 1:
                    optionsMenu();
                    break;
                case 2:
                    System.exit(0);
                    break;
                default:
                    print("Please only enter a valid number");
                    mainMenu();
                    break;
            }
        });
    }

    private void optionsMenu() {
        final String[] options = new String[]{"File output", "Time delay", "Return"};

        ConsoleUtil.createOptionsMenu(options, integer -> {
            final Scanner scanner = new Scanner(System.in);
            switch (integer) {
                case 0:
                    print("Please enter the path");
                    final String path = scanner.nextLine();
                    cacheUtil.cacheObject("outDir", path);
                    break;
                case 1:
                    print("Please enter the delay (in ms)");
                    final int delay = scanner.nextInt();
                    cacheUtil.cacheObject("delay", delay);
                    break;
                case 2:
                    mainMenu();
                    break;
                default:
                    print("Please only enter a valid number");
                    optionsMenu();
                    break;
            }
            optionsMenu();
        });
    }

    private void download() {
        final Scanner scanner = new Scanner(System.in);
        print("Please enter the show's name");

        try {
            final String[] searchQueryResults = GogoAnimeFetcher.getURLsFromSearch(scanner.nextLine());

            ConsoleUtil.list(searchQueryResults);

            final int index = Integer.parseInt(scanner.nextLine());
            if (index < 0 || index >= searchQueryResults.length) {
                print("Index out of bounds");
                download();
                return;
            }

            final String queryResult = searchQueryResults[index];

            print("Please enter the starting episode (0 for the first one)");
            final int startInput = Integer.parseInt(scanner.nextLine());
            print("Please enter the ending episode (0 for the last one)");
            final int endInput = Integer.parseInt(scanner.nextLine());

            final GogoAnimeFetcher gogoAnimeFetcher = new GogoAnimeFetcher(queryResult);

            final int len = gogoAnimeFetcher.getFetchedReferrals().length();

            final int end = endInput == 0 ? len : Math.min(endInput, len); //Take the lowest of the two values eg. input = 10 > max then take the max
            final int start = startInput >= end ? end : Math.max(startInput, 0);

            final File outDir = new File((String) cacheUtil.getFromCacheOrCache("outDir", "outDir"), StringUtil.sanitizeString(queryResult));

            downloadEpisodes(outDir, gogoAnimeFetcher.getFetchedReferrals(), new int[]{0, end, start});
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    public void downloadEpisodes(final File outDir, final JSONArray array, final int[] count) throws IOException {
        new EpisodeDownloadTask(array, outDir, count, new EpisodeDownloadTask.EpisodeDownloadCallback() {
            @Override
            public void onDownloadCompleted(int[] count) {
                try {
                    downloadEpisodes(outDir, array, count);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorThrown(String message) {

            }
        }).executeAsync();
    }

    private void print(final String s) {
        System.out.println(s);
    }
}
