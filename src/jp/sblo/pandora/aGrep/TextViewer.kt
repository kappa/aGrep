package jp.sblo.pandora.aGrep

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.util.ArrayList
import java.util.regex.Pattern

class TextViewer : AppCompatActivity(), OnItemLongClickListener, OnItemClickListener {

    private lateinit var viewModel: TextViewerViewModel
    private lateinit var prefs: Prefs
    private lateinit var textPreview: TextPreview
    private lateinit var statusContainer: View
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView

    private var patternText: String = ""
    private var line: Int = 0
    private var path: String = ""
    private var adapter: TextPreview.Adapter? = null
    private var lastError: String? = null
    private var lastCancelled = false
    private var lastCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.textviewer)

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        prefs = Prefs.loadPrefes(applicationContext)

        textPreview = findViewById(R.id.TextPreview)
        textPreview.onItemLongClickListener = this
        textPreview.onItemClickListener = this

        statusContainer = findViewById(R.id.textStatusContainer)
        progressBar = findViewById(R.id.textProgress)
        statusText = findViewById(R.id.textStatus)

        viewModel = ViewModelProvider(this)[TextViewerViewModel::class.java]
        viewModel.stateLiveData.observe(this) { state ->
            renderState(state)
        }

        val extras = intent.extras
        if (extras == null) {
            finish()
            return
        }
        path = extras.getString(EXTRA_PATH) ?: run {
            finish()
            return
        }
        patternText = extras.getString(EXTRA_QUERY, "")
        line = extras.getInt(EXTRA_LINE)

        if (!prefs.mRegularExrpression) {
            patternText = Search.escapeMetaChar(patternText)
        }

        title = "$path - aGrep"

        lastError = null
        lastCancelled = false
        lastCompleted = false
        adapter = null
        textPreview.adapter = null

        viewModel.loadText(TextViewerViewModel.TextLoadRequest(path))
    }

    private fun renderState(state: TextViewerViewModel.TextViewerState) {
        statusContainer.visibility = if (state.isLoading || state.statusMessage != null || state.errorMessage != null || state.isCancelled || state.isCompleted) View.VISIBLE else View.GONE
        progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        val statusMessage = when {
            state.errorMessage != null -> state.errorMessage
            state.isLoading -> state.statusMessage ?: getString(R.string.grep_spinner)
            state.isCancelled -> getString(R.string.grep_canceled)
            state.isCompleted -> state.statusMessage ?: getString(R.string.grep_finished)
            else -> state.statusMessage ?: ""
        }
        statusText.text = statusMessage

        if (state.errorMessage != null && state.errorMessage != lastError) {
            lastError = state.errorMessage
            Toast.makeText(applicationContext, state.errorMessage, Toast.LENGTH_LONG).show()
        }

        if (state.isCancelled && !lastCancelled) {
            lastCancelled = true
            Toast.makeText(applicationContext, R.string.grep_canceled, Toast.LENGTH_LONG).show()
        }

        if (state.isCompleted && !lastCompleted) {
            lastCompleted = true
            Toast.makeText(applicationContext, R.string.grep_finished, Toast.LENGTH_LONG).show()
        }

        if (state.isCompleted && adapter == null) {
            val displayData = ArrayList<CharSequence>(state.lines)
            adapter = TextPreview.Adapter(applicationContext, R.layout.textpreview_row, R.id.TextPreview, displayData)
            val compiledPattern = if (prefs.mIgnoreCase) {
                Pattern.compile(patternText, Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE or Pattern.MULTILINE)
            } else {
                Pattern.compile(patternText)
            }
            adapter?.setFormat(compiledPattern, prefs.mHighlightFg, prefs.mHighlightBg, prefs.mFontSize)
            textPreview.adapter = adapter
            textPreview.post {
                val height = textPreview.height
                if (line > 0) {
                    textPreview.setSelectionFromTop(line - 1, height / 4)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.mainmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_viewer -> {
                val intent = Intent(Intent.ACTION_VIEW)
                if (prefs.addLineNumber) {
                    val firstVisible = textPreview.firstVisiblePosition
                    intent.setDataAndType(Uri.parse("file://" + path + "?line=" + (firstVisible + 1)), "text/plain")
                } else {
                    intent.setDataAndType(Uri.parse("file://" + path), "text/plain")
                }
                startActivity(intent)
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
        if (prefs.addLineNumber) {
            intent.setDataAndType(Uri.parse("file://" + path + "?line=" + (position + 1)), "text/plain")
        } else {
            intent.setDataAndType(Uri.parse("file://" + path), "text/plain")
        }
        startActivity(intent)
        return true
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val textView = view as TextView
        val clip = ClipData.newPlainText("aGrep Text Viewer", textView.text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, R.string.label_copied, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            viewModel.cancel()
        }
    }

    companion object {
        const val EXTRA_LINE = "line"
        const val EXTRA_QUERY = "query"
        const val EXTRA_PATH = "path"
    }
}
