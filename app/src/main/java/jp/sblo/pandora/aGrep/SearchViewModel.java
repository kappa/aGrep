package jp.sblo.pandora.aGrep;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for managing search operations and UI state.
 */
public class SearchViewModel extends ViewModel {
    private final MutableLiveData<SearchUiState> uiState;
    private GrepEngine grepEngine;

    public SearchViewModel() {
        this.uiState = new MutableLiveData<SearchUiState>(SearchUiState.idle());
    }

    public LiveData<SearchUiState> getUiState() {
        return uiState;
    }

    /**
     * Initialize the grep engine with a Context.
     * Should be called from Activity/Fragment after ViewModel creation.
     */
    public void initializeEngine(Context context) {
        if (grepEngine == null) {
            grepEngine = new ExecutorGrepEngine(context);
        }
    }

    /**
     * Start a new search operation.
     */
    public void search(SearchRequest request) {
        if (grepEngine == null) {
            uiState.setValue(SearchUiState.error("Engine not initialized"));
            return;
        }

        uiState.setValue(SearchUiState.searching(new SearchProgress(
            request.query,
            0,
            0,
            null
        )));

        grepEngine.search(request, new GrepEngine.ProgressCallback() {
            @Override
            public void onProgress(SearchProgress progress) {
                uiState.postValue(SearchUiState.searching(progress));
            }

            @Override
            public void onComplete(SearchSummary summary) {
                uiState.postValue(SearchUiState.completed(summary));
            }

            @Override
            public void onError(String error) {
                uiState.postValue(SearchUiState.error(error));
            }
        });
    }

    /**
     * Cancel the current search operation.
     */
    public void cancelSearch() {
        if (grepEngine != null) {
            grepEngine.cancel();
        }
        uiState.setValue(SearchUiState.idle());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (grepEngine != null) {
            grepEngine.shutdown();
        }
    }
}
