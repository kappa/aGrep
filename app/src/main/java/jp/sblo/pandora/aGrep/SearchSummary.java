package jp.sblo.pandora.aGrep;

import java.util.Collections;
import java.util.List;

/**
 * Represents the final summary of a completed search operation.
 */
public class SearchSummary {
    public final String query;
    public final int totalFilesProcessed;
    public final int totalMatchesFound;
    public final List<GrepView.Data> allMatches;
    public final long durationMillis;

    public SearchSummary(String query, int totalFilesProcessed, int totalMatchesFound,
                        List<GrepView.Data> allMatches, long durationMillis) {
        this.query = query;
        this.totalFilesProcessed = totalFilesProcessed;
        this.totalMatchesFound = totalMatchesFound;
        this.allMatches = allMatches != null ? allMatches : Collections.emptyList();
        this.durationMillis = durationMillis;
    }
}
