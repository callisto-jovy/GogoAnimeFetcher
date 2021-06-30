package net.bplaced.abzzezz.gogoanime;

import java.util.regex.Pattern;

public interface GogoAnime {

    String BASE_URL = "https://gogoanime.pe";

    String BASE_SEARCH_API = "https://gogoanime.pe/search.html?keyword=%s";

    String API_URL = "https://gogo-play.net/ajax.php?id=%s";
    //Start, end, anime-id, Returns a "list" containing all redirects to the other episodes
    String EPISODE_API_URL = " https://ajax.gogo-load.com/ajax/load-list-episode?ep_start=%d&ep_end=%d&id=%d";

    String FORMATTED_SEARCH_API = "https://gogoanime.pe/search.html?keyword=%s";

    Pattern SOURCE_PATTERN = Pattern.compile("(?<=sources:\\[\\{file: ')[^']+");

}
