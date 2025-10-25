package dev.kappa.agrep_again;

/**
 * Interface for search engine implementations that process grep operations.
 */
public interface GrepEngine {
    /**
     * Callback interface for receiving search progress updates.
     */
    interface ProgressCallback {
        /**
         * Called when search progress is updated.
         * @param progress The current search progress
         */
        void onProgress(SearchProgress progress);

        /**
         * Called when search completes successfully.
         * @param summary The final search summary
         */
        void onComplete(SearchSummary summary);

        /**
         * Called when search encounters an error.
         * @param error The error message
         */
        void onError(String error);
    }

    /**
     * Start a search operation.
     * @param request The search request containing query, pattern, and preferences
     * @param callback The callback for receiving progress updates
     */
    void search(SearchRequest request, ProgressCallback callback);

    /**
     * Cancel the current search operation.
     */
    void cancel();

    /**
     * Shutdown the engine and release resources.
     */
    void shutdown();
}
