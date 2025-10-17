package jp.sblo.pandora.aGrep

import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.ArrayDeque
import java.util.ArrayList
import java.util.regex.Pattern
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import org.mozilla.universalchardet.UniversalDetector

/**
 * Coroutine based grep engine that mirrors the behaviour of the old AsyncTask implementation
 * while being cancellation friendly and emitting incremental progress updates.
 */
class CoroutineGrepEngine(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun search(
        request: SearchRequest,
        onProgress: suspend (SearchProgress) -> Unit
    ): SearchSummary = withContext(ioDispatcher) {
        val results = ArrayList<GrepView.Data>()
        var filesProcessed = 0
        var matchesFound = 0

        suspend fun emitProgress(newMatches: List<GrepView.Data> = emptyList()) {
            onProgress(
                SearchProgress(
                    query = request.query,
                    filesProcessed = filesProcessed,
                    matchesFound = matchesFound,
                    newMatches = newMatches
                )
            )
        }

        fun shouldIncludeFile(file: File): Boolean {
            val extensionList = request.prefs.mExtList
            if (extensionList == null || extensionList.isEmpty()) {
                return true
            }
            val name = file.name
            for (ext in extensionList) {
                if (!ext.checked) {
                    continue
                }
                if (ext.string == "*") {
                    if (!name.contains('.')) {
                        return true
                    }
                } else if (name.lowercase().endsWith("." + ext.string.lowercase())) {
                    return true
                }
            }
            return false
        }

        suspend fun grepFile(file: File) {
            coroutineContext.ensureActive()
            if (!shouldIncludeFile(file)) {
                return
            }
            var reader: BufferedReader? = null
            var stream: InputStream? = null
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

                val matcher = request.pattern.matcher("")
                var lineNumber = 0
                var localMatches: ArrayList<GrepView.Data>? = null
                var foundInFile = false
                while (true) {
                    coroutineContext.ensureActive()
                    val line = reader.readLine() ?: break
                    lineNumber += 1
                    matcher.reset(line)
                    if (matcher.find()) {
                        foundInFile = true
                        matchesFound += 1
                        val data = GrepView.Data(file, lineNumber, line)
                        results.add(data)
                        if (localMatches == null) {
                            localMatches = ArrayList()
                        }
                        localMatches!!.add(data)
                        if (localMatches!!.size >= BATCH_SIZE) {
                            emitProgress(ArrayList(localMatches!!))
                            localMatches!!.clear()
                        }
                    }
                }
                if (!localMatches.isNullOrEmpty()) {
                    emitProgress(ArrayList(localMatches!!))
                } else if (!foundInFile && filesProcessed % 10 == 0) {
                    emitProgress(emptyList())
                }
            } catch (_: IOException) {
                // Ignore and move on to the next file.
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

        suspend fun grepDirectory(directory: File) {
            coroutineContext.ensureActive()
            if (!directory.exists()) {
                return
            }
            val queue = ArrayDeque<File>()
            queue.add(directory)
            while (queue.isNotEmpty()) {
                coroutineContext.ensureActive()
                val current = queue.removeFirst()
                val files = current.listFiles() ?: continue
                for (entry in files) {
                    coroutineContext.ensureActive()
                    if (entry.isDirectory) {
                        queue.add(entry)
                    } else {
                        filesProcessed += 1
                        grepFile(entry)
                        emitProgress()
                    }
                }
            }
        }

        val directories = request.prefs.mDirList
        if (directories != null) {
            for (dir in directories) {
                coroutineContext.ensureActive()
                if (!dir.checked) {
                    continue
                }
                grepDirectory(File(dir.string))
            }
        }

        SearchSummary(
            query = request.query,
            filesProcessed = filesProcessed,
            matchesFound = matchesFound,
            results = results
        )
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

    companion object {
        private const val BATCH_SIZE = 10
    }
}

data class SearchRequest(
    val query: String,
    val prefs: Prefs,
    val pattern: Pattern
)

data class SearchProgress(
    val query: String,
    val filesProcessed: Int,
    val matchesFound: Int,
    val newMatches: List<GrepView.Data>
)

data class SearchSummary(
    val query: String,
    val filesProcessed: Int,
    val matchesFound: Int,
    val results: List<GrepView.Data>
)
