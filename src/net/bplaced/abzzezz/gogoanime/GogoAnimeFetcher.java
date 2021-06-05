package net.bplaced.abzzezz.gogoanime;

import net.bplaced.abzzezz.gogoanime.util.Constant;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collector;

public class GogoAnimeFetcher implements GogoAnime {

    private final JSONArray fetchedReferrals;
    private final Document showDocument;

    public GogoAnimeFetcher(final String urlIn) throws IOException {
        this.showDocument = createGogoConnection(urlIn).get();
        this.fetchedReferrals = this.fetchReferrals();
    }

    /**
     * Fetches all referral ids
     *
     * @param idIn     show's id to fetch
     * @param epiStart episode to start from
     * @param epiEnd   episode to end on
     * @return JSONArray with all referrals
     * @throws IOException Connection failure etc.
     */
    public static JSONArray fetchReferrals(final String idIn, final int epiStart, final int epiEnd) throws IOException {
        final int id = Integer.parseInt(idIn); //Parse id
        return collectReferrals(epiStart, epiEnd, id);
    }

    /**
     * Base method for the fetchReferrals functions
     *
     * @param epiStart episode to start on
     * @param epiEnd   episode to end on
     * @param id       the show's id
     * @return JSONArray with all referrals
     * @throws IOException URL related
     */
    private static JSONArray collectReferrals(int epiStart, int epiEnd, int id) throws IOException {
        final String episodesURL = String.format(Locale.ENGLISH, EPISODE_API_URL, epiStart, epiEnd, id); //Format episode request URL
        final Document episodesDocument = createGogoCdnConnection(episodesURL).get(); //Create httpsurlconnection

        return episodesDocument.body().getElementById("episode_related")
                .children()
                .stream()
                .map(element -> BASE_URL + element.selectFirst("a").attr("href").trim())
                .collect(
                        Collector.of(
                                JSONArray::new, //init accumulator
                                JSONArray::put, //processing each element
                                JSONArray::put  //confluence 2 accumulators in parallel execution
                        ));
    }

    /**
     * Fetches the id to an episode using it's site's referral
     *
     * @param referral referral to the site
     * @return the
     */
    public static Optional<String> fetchIDLink(final String referral) {
        if (referral.isEmpty()) return Optional.empty(); //Empty case, return empty

        try {
            final Document episodeDocument = createGogoCdnConnection(referral).get();

            final String src = episodeDocument.selectFirst("iframe").attr("src");
            final Matcher matcher = PATTERN.matcher(src);

            if (matcher.find()) {
                return Optional.of(String.format(API_URL, matcher.group().substring(3, matcher.group().length() - 1)));
            } else return Optional.empty();
        } catch (final IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Calls gogoanime's search api
     *
     * @param searchQuery query to search for
     * @return String array with all url results
     * @throws IOException url error
     */
    public static String[] getURLsFromSearch(final String searchQuery) throws IOException {
        final Document document = Jsoup.connect(String.format(SEARCH_URL, searchQuery)).userAgent(Constant.USER_AGENT).get();
        return document.getElementsByClass("name")
                .stream()
                .map(element -> BASE_URL + element.select("a").attr("href"))
                .toArray(String[]::new);
    }

    /**
     * Create jsoup connection
     *
     * @param url url in
     * @return pre constructed connection
     */
    private static Connection createGogoConnection(final String url) {
        return Jsoup.connect(url)
                .userAgent(Constant.USER_AGENT)
                .header("authority", "gogoanime.so")
                .referrer("https://gogoanime.so/search.html");
    }

    /**
     * Create jsoup connection
     *
     * @param url url in
     * @return pre constructed connection
     */
    private static Connection createGogoCdnConnection(final String url) {
        return Jsoup.connect(url)
                .userAgent(Constant.USER_AGENT)
                .header("authority", "ajax.gogocdn.net");
    }

    /**
     * Gets the direct video url from the formatted api link
     *
     * @param in read in lines
     * @return url to mp4
     */
    public static String getVidURL(final String referral) throws JSONException, IOException {
        final Document document = createGogoCdnConnection(referral).get();
        return new JSONObject(document.text()).getJSONArray("source").getJSONObject(0).getString("file");
    }

    /**
     * Fetches all ids from the given url
     *
     * @return String array containing all ids for the direct url
     * @throws IOException some connection goes wrong
     */
    private JSONArray fetchReferrals() throws IOException {
        final Element body = showDocument.body();
        final int id = Integer.parseInt(body.selectFirst("input#movie_id").val());
        final int epiStart = Integer.parseInt(body.selectFirst("#episode_page a.active").attr("ep_start"));
        final int epiEnd = Integer.parseInt(body.selectFirst("#episode_page a.active").attr("ep_end"));

        return collectReferrals(epiStart, epiEnd, id);
    }

    /**
     * Returns the show's id
     *
     * @return the shod id
     */
    public String getID() {
        return showDocument.body().selectFirst("input#movie_id").val();
    }

    /**
     * @return episode start from local document
     */
    public String getEpisodeStart() {
        return showDocument.body().selectFirst("#episode_page a.active").attr("ep_start");
    }

    /**
     * @return episode end from local document
     */
    public String getEpisodeEnd() {
        return showDocument.body().selectFirst("#episode_page a.active").attr("ep_end");
    }

    public JSONArray getFetchedReferrals() {
        return fetchedReferrals;
    }
}