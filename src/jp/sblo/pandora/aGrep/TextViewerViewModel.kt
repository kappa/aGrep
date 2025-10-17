package jp.sblo.pandora.aGrep

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.ArrayList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import org.mozilla.universalchardet.UniversalDetector

class TextViewerViewModel : ViewModel() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.IO)
    private var loadJob: Job? = null

    private val _state = MutableStateFlow(TextViewerState())
    val state: StateFlow<TextViewerState> = _state
    val stateLiveData: LiveData<TextViewerState> = state.asLiveData()

    fun loadText(request: TextLoadRequest) {
        if (loadJob?.isActive == true && _state.value.path == request.path) {
            return
        }
        loadJob?.cancel()
        _state.value = TextViewerState(path = request.path, isLoading = true)
        loadJob = scope.launch {
            try {
                val (lines, totalLines) = readAllLines(request.path)
                _state.update {
                    it.copy(
                        isLoading = false,
                        isCompleted = true,
                        lines = lines,
                        statusMessage = "$totalLines lines",
                        errorMessage = null
                    )
                }
            } catch (cancelled: CancellationException) {
                _state.update {
                    it.copy(isLoading = false, isCancelled = true)
                }
            } catch (throwable: Throwable) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = throwable.message)
                }
            }
        }
    }

    fun cancel() {
        loadJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

    private suspend fun readAllLines(path: String): Pair<List<CharSequence>, Int> {
        val file = File(path)
        if (!file.exists()) {
            throw IOException("File not found: $path")
        }
        return withContext(Dispatchers.IO) {
            val lines = ArrayList<CharSequence>()
            var stream: InputStream? = null
            var reader: BufferedReader? = null
            try {
                stream = BufferedInputStream(FileInputStream(file), 65536)
                stream.mark(65536)
                val encoding = detectEncoding(stream)
                stream.reset()
                reader = if (encoding != null) {
                    BufferedReader(InputStreamReader(stream, encoding), 8192)
                } else {
                    BufferedReader(InputStreamReader(stream), 8192)
                }
                var lineCount = 0
                while (true) {
                    coroutineContext.ensureActive()
                    val line = reader.readLine() ?: break
                    lines.add(line)
                    lineCount += 1
                    if (lineCount % 200 == 0) {
                        _state.update { current ->
                            current.copy(statusMessage = "$lineCount lines")
                        }
                    }
                }
                Pair(lines.toList(), lineCount)
            } finally {
                try {
                    reader?.close()
                } catch (_: IOException) {
                }
                try {
                    stream?.close()
                } catch (_: IOException) {
                }
            }
        }
    }

    private fun detectEncoding(stream: InputStream): String? {
        return try {
            val detector = UniversalDetector()
            val buffer = ByteArray(4096)
            val read = stream.read(buffer)
            if (read > 0) {
                detector.handleData(buffer, 0, read)
                detector.dataEnd()
            }
            detector.charset
        } catch (_: Exception) {
            null
        }
    }

    data class TextLoadRequest(val path: String)

    data class TextViewerState(
        val path: String? = null,
        val isLoading: Boolean = false,
        val isCompleted: Boolean = false,
        val isCancelled: Boolean = false,
        val lines: List<CharSequence> = emptyList(),
        val statusMessage: String? = null,
        val errorMessage: String? = null
    )
}
