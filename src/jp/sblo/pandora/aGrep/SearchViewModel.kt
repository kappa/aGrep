package jp.sblo.pandora.aGrep

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import java.util.ArrayList
import java.util.Collections
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val grepEngine: CoroutineGrepEngine
) : ViewModel() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.Default)
    private var searchJob: Job? = null

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state
    val stateLiveData: LiveData<SearchUiState> = state.asLiveData()

    fun startSearch(request: SearchRequest) {
        val current = _state.value
        if (current.isSearching && current.query == request.query) {
            return
        }
        searchJob?.cancel()
        _state.value = SearchUiState(query = request.query, isSearching = true)
        searchJob = scope.launch {
            try {
                val summary = grepEngine.search(request) { progress ->
                    _state.update { previous ->
                        val combined = if (progress.newMatches.isEmpty()) {
                            previous.results
                        } else {
                            val newList = ArrayList(previous.results.size + progress.newMatches.size)
                            newList.addAll(previous.results)
                            newList.addAll(progress.newMatches)
                            newList
                        }
                        previous.copy(
                            filesProcessed = progress.filesProcessed,
                            matchesFound = progress.matchesFound,
                            statusMessage = progressStatus(progress.filesProcessed, progress.matchesFound),
                            results = combined
                        )
                    }
                }
                val sorted = ArrayList(summary.results)
                Collections.sort(sorted, GrepView.Data())
                _state.update {
                    it.copy(
                        isSearching = false,
                        isCompleted = true,
                        statusMessage = progressStatus(summary.filesProcessed, summary.matchesFound),
                        results = sorted
                    )
                }
            } catch (cancelled: CancellationException) {
                _state.update {
                    it.copy(isSearching = false, isCancelled = true)
                }
            } catch (throwable: Throwable) {
                _state.update {
                    it.copy(isSearching = false, errorMessage = throwable.message)
                }
            }
        }
    }

    fun cancelSearch() {
        searchJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

    private fun progressStatus(files: Int, matches: Int): String =
        "$files files • $matches matches"

    data class SearchUiState(
        val query: String = "",
        val isSearching: Boolean = false,
        val isCompleted: Boolean = false,
        val isCancelled: Boolean = false,
        val filesProcessed: Int = 0,
        val matchesFound: Int = 0,
        val results: List<GrepView.Data> = emptyList(),
        val statusMessage: String? = null,
        val errorMessage: String? = null
    )

    class Factory(
        private val grepEngine: CoroutineGrepEngine
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(grepEngine) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}
