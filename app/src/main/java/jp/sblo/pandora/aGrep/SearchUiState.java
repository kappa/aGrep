package jp.sblo.pandora.aGrep;

/**
 * Represents the UI state for search operations.
 */
public class SearchUiState {
    public final boolean isSearching;
    public final SearchProgress progress;
    public final SearchSummary summary;
    public final String errorMessage;

    private SearchUiState(boolean isSearching, SearchProgress progress,
                         SearchSummary summary, String errorMessage) {
        this.isSearching = isSearching;
        this.progress = progress;
        this.summary = summary;
        this.errorMessage = errorMessage;
    }

    public static SearchUiState idle() {
        return new SearchUiState(false, null, null, null);
    }

    public static SearchUiState searching(SearchProgress progress) {
        return new SearchUiState(true, progress, null, null);
    }

    public static SearchUiState completed(SearchSummary summary) {
        return new SearchUiState(false, null, summary, null);
    }

    public static SearchUiState error(String errorMessage) {
        return new SearchUiState(false, null, null, errorMessage);
    }
}
