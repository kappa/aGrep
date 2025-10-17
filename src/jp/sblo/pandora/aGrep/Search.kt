package jp.sblo.pandora.aGrep

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.util.ArrayList
import java.util.regex.Pattern

class Search : AppCompatActivity(), GrepView.Callback {

    private lateinit var grepView: GrepView
    private lateinit var adapter: GrepView.GrepAdapter
    private val data = ArrayList<GrepView.Data>()
    private lateinit var prefs: Prefs
    private lateinit var viewModel: SearchViewModel
    private lateinit var statusContainer: View
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView

    private var query: String? = null
    private var pattern: Pattern? = null

    private var lastCompleted = false
    private var lastCancelled = false
    private var lastError: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result)

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        prefs = Prefs.loadPrefes(this)
        if (prefs.mDirList.isEmpty()) {
            Toast.makeText(applicationContext, R.string.label_no_target_dir, Toast.LENGTH_LONG).show()
            startActivity(Intent(this, Settings::class.java))
            finish()
            return
        }

        grepView = findViewById(R.id.DicView01)
        adapter = GrepView.GrepAdapter(applicationContext, R.layout.list_row, R.id.DicView01, data)
        grepView.adapter = adapter
        grepView.setCallback(this)

        statusContainer = findViewById(R.id.searchStatusContainer)
        progressBar = findViewById(R.id.searchProgress)
        statusText = findViewById(R.id.searchStatus)

        viewModel = ViewModelProvider(this, SearchViewModel.Factory(CoroutineGrepEngine()))
            .get(SearchViewModel::class.java)

        viewModel.stateLiveData.observe(this) { state ->
            renderState(state)
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null || intent.action != Intent.ACTION_SEARCH) {
            return
        }
        val extras = intent.extras ?: return
        val newQuery = extras.getString(SearchManager.QUERY)
        if (newQuery.isNullOrEmpty()) {
            finish()
            return
        }
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(applicationContext, R.string.label_unable_access, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        query = newQuery
        val currentState = viewModel.state.value
        val patternText = createPatternText(newQuery)
        val compiledPattern = if (prefs.mIgnoreCase) {
            Pattern.compile(patternText, Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE or Pattern.MULTILINE)
        } else {
            Pattern.compile(patternText)
        }
        pattern = compiledPattern
        adapter.setFormat(compiledPattern, prefs.mHighlightFg, prefs.mHighlightBg, prefs.mFontSize)

        if (currentState.query == newQuery && (currentState.isSearching || currentState.isCompleted)) {
            data.clear()
            data.addAll(currentState.results)
            adapter.notifyDataSetChanged()
            lastCompleted = currentState.isCompleted
            lastCancelled = currentState.isCancelled
            lastError = currentState.errorMessage
            return
        }

        prefs.addRecent(this, newQuery)
        data.clear()
        adapter.notifyDataSetChanged()
        lastCompleted = false
        lastCancelled = false
        lastError = null
        viewModel.startSearch(
            SearchRequest(
                query = newQuery,
                prefs = prefs,
                pattern = compiledPattern
            )
        )
    }

    private fun createPatternText(text: String): String {
        var patternText = text
        if (!prefs.mRegularExrpression) {
            patternText = escapeMetaChar(patternText)
            patternText = convertOrPattern(patternText)
        }
        return patternText
    }

    private fun renderState(state: SearchViewModel.SearchUiState) {
        data.clear()
        data.addAll(state.results)
        adapter.notifyDataSetChanged()
        if (state.isCompleted && data.isNotEmpty()) {
            grepView.setSelection(0)
        }
        statusContainer.visibility = if (state.isSearching || state.statusMessage != null || state.errorMessage != null || state.isCancelled || state.isCompleted) View.VISIBLE else View.GONE
        progressBar.visibility = if (state.isSearching) View.VISIBLE else View.GONE
        val statusMessage = when {
            state.errorMessage != null -> state.errorMessage
            state.isSearching -> state.statusMessage ?: getString(R.string.grep_spinner)
            state.isCancelled -> getString(R.string.grep_canceled)
            state.isCompleted -> state.statusMessage ?: getString(R.string.grep_finished)
            else -> state.statusMessage ?: ""
        }
        statusText.text = statusMessage

        if (state.isCompleted && !lastCompleted) {
            lastCompleted = true
            Toast.makeText(applicationContext, R.string.grep_finished, Toast.LENGTH_LONG).show()
        }
        if (state.isCancelled && !lastCancelled) {
            lastCancelled = true
            Toast.makeText(applicationContext, R.string.grep_canceled, Toast.LENGTH_LONG).show()
        }
        if (state.errorMessage != null && state.errorMessage != lastError) {
            lastError = state.errorMessage
            Toast.makeText(applicationContext, state.errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onGrepItemClicked(position: Int) {
        val data = grepView.adapter.getItem(position) as GrepView.Data
        val intent = Intent(this, TextViewer::class.java)
        intent.putExtra(TextViewer.EXTRA_PATH, data.mFile.absolutePath)
        intent.putExtra(TextViewer.EXTRA_QUERY, query)
        intent.putExtra(TextViewer.EXTRA_LINE, data.mLinenumber)
        startActivity(intent)
    }

    override fun onGrepItemLongClicked(position: Int): Boolean {
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            viewModel.cancelSearch()
        }
    }

    companion object {
        @JvmStatic
        fun escapeMetaChar(pattern: String): String {
            val metachar = ".^${}[]*+?|()\\"
            val newpat = StringBuilder()
            for (c in pattern) {
                if (metachar.indexOf(c) >= 0) {
                    newpat.append('\\')
                }
                newpat.append(c)
            }
            return newpat.toString()
        }

        @JvmStatic
        fun convertOrPattern(pattern: String): String {
            return if (pattern.contains(" ")) {
                "(" + pattern.replace(" ", "|") + ")"
            } else {
                pattern
            }
        }

        @JvmStatic
        fun highlightKeyword(text: CharSequence, pattern: Pattern, fgcolor: Int, bgcolor: Int): SpannableString {
            val spannable = SpannableString(text)
            var start = 0
            val matcher = pattern.matcher(text)
            while (matcher.find(start)) {
                start = matcher.start()
                val end = matcher.end()
                val bgSpan = BackgroundColorSpan(bgcolor)
                spannable.setSpan(bgSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                val fgSpan = ForegroundColorSpan(fgcolor)
                spannable.setSpan(fgSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                start = end
            }
            return spannable
        }
    }
}
