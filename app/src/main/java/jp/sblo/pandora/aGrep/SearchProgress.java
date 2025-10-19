package jp.sblo.pandora.aGrep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents incremental progress during a search operation.
 */
public class SearchProgress {
    public final String query;
    public final int filesProcessed;
    public final int matchesFound;
    public final List<GrepView.Data> newMatches;

    public SearchProgress(String query, int filesProcessed, int matchesFound, List<GrepView.Data> newMatches) {
        this.query = query;
        this.filesProcessed = filesProcessed;
        this.matchesFound = matchesFound;
        this.newMatches = newMatches != null ? newMatches : Collections.<GrepView.Data>emptyList();
    }
}
