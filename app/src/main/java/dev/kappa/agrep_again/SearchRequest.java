package dev.kappa.agrep_again;

import java.util.regex.Pattern;

/**
 * Represents a search request with query pattern and preferences.
 */
public class SearchRequest {
    public final String query;
    public final Pattern pattern;
    public final Prefs prefs;

    public SearchRequest(String query, Pattern pattern, Prefs prefs) {
        this.query = query;
        this.pattern = pattern;
        this.prefs = prefs;
    }
}
