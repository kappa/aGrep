package jp.sblo.pandora.aGrep

import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class CoroutineGrepEngineTest {

    @Test
    fun searchHonorsExtensionFilter() = runBlocking {
        val root = createTempDir()
        val include = File(root, "include.txt")
        include.writeText("needle")
        val exclude = File(root, "exclude.log")
        exclude.writeText("needle")

        val prefs = Prefs()
        prefs.mDirList.add(CheckedString(true, root.absolutePath))
        prefs.mExtList.clear()
        prefs.mExtList.add(CheckedString(true, "txt"))

        val engine = CoroutineGrepEngine(Dispatchers.Default)
        val request = SearchRequest("needle", prefs, Pattern.compile("needle"))
        val summary = engine.search(request) { }

        assertEquals(1, summary.results.size)
        assertEquals("include.txt", summary.results.first().mFile.name)
    }

    @Test
    fun searchCanBeCancelled() = runBlocking {
        val root = createTempDir()
        repeat(10) { index ->
            val file = File(root, "file$index.txt")
            file.writeText((0..1000).joinToString("\n") { "line $it needle" })
        }

        val prefs = Prefs()
        prefs.mDirList.add(CheckedString(true, root.absolutePath))
        prefs.mExtList.add(CheckedString(true, "txt"))

        val engine = CoroutineGrepEngine(Dispatchers.Default)
        val request = SearchRequest("needle", prefs, Pattern.compile("needle"))
        val cancelled = AtomicBoolean(false)

        val job = launch {
            try {
                engine.search(request) {
                    cancelled.set(true)
                    throw CancellationException("stop")
                }
            } catch (ignored: CancellationException) {
            }
        }
        job.join()
        assertTrue(cancelled.get())
    }

    @Test
    fun searchEmitsProgress() = runBlocking {
        val root = createTempDir()
        val file = File(root, "progress.txt")
        file.writeText("needle\nnope\nneedle")

        val prefs = Prefs()
        prefs.mDirList.add(CheckedString(true, root.absolutePath))
        prefs.mExtList.add(CheckedString(true, "txt"))

        val engine = CoroutineGrepEngine(Dispatchers.Default)
        val request = SearchRequest("needle", prefs, Pattern.compile("needle"))
        var progressEvents = 0
        val summary = engine.search(request) { progress ->
            if (progress.newMatches.isNotEmpty()) {
                progressEvents++
            }
        }
        assertEquals(2, summary.matchesFound)
        assertTrue(progressEvents >= 1)
    }

    private fun createTempDir(): File {
        val dir = kotlin.io.path.createTempDirectory().toFile()
        dir.deleteOnExit()
        return dir
    }
}
