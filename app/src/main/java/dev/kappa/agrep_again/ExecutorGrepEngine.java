package dev.kappa.agrep_again;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ExecutorService-based grep engine implementation using SAF (Storage Access Framework).
 */
public class ExecutorGrepEngine implements GrepEngine {
    private final Context context;
    private final ContentResolver contentResolver;
    private final ExecutorService executor;
    private final AtomicBoolean cancelled;
    private Future<?> currentTask;

    public ExecutorGrepEngine(Context context) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
        this.executor = Executors.newSingleThreadExecutor();
        this.cancelled = new AtomicBoolean(false);
    }

    @Override
    public void search(SearchRequest request, ProgressCallback callback) {
        cancelled.set(false);

        currentTask = executor.submit(() -> {
            long startTime = System.currentTimeMillis();
            AtomicInteger fileCount = new AtomicInteger(0);
            AtomicInteger matchCount = new AtomicInteger(0);
            List<GrepView.Data> allMatches = Collections.synchronizedList(new ArrayList<>());

            try {
                for (CheckedString dir : request.prefs.mDirList) {
                    if (cancelled.get()) {
                        callback.onComplete(new SearchSummary(
                            request.query,
                            fileCount.get(),
                            matchCount.get(),
                            allMatches,
                            System.currentTimeMillis() - startTime
                        ));
                        return;
                    }

                    if (dir.checked && dir.hasValue()) {
                        Uri uri = Uri.parse(dir.string);
                        DocumentFile root = DocumentFile.fromTreeUri(context, uri);
                        if (root != null && root.isDirectory()) {
                            String base = dir.getDisplayName();
                            if (base == null) {
                                base = safeName(root);
                            }
                            if (!processDocumentTree(root, base, request, callback,
                                    fileCount, matchCount, allMatches)) {
                                callback.onComplete(new SearchSummary(
                                    request.query,
                                    fileCount.get(),
                                    matchCount.get(),
                                    allMatches,
                                    System.currentTimeMillis() - startTime
                                ));
                                return;
                            }
                        }
                    }
                }

                callback.onComplete(new SearchSummary(
                    request.query,
                    fileCount.get(),
                    matchCount.get(),
                    allMatches,
                    System.currentTimeMillis() - startTime
                ));
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
            }
        });
    }

    private boolean processDocumentTree(DocumentFile dir, String basePath, SearchRequest request,
                                       ProgressCallback callback, AtomicInteger fileCount,
                                       AtomicInteger matchCount, List<GrepView.Data> allMatches) {
        if (cancelled.get()) {
            return false;
        }

        DocumentFile[] children = dir.listFiles();
        if (children != null) {
            for (DocumentFile child : children) {
                if (cancelled.get()) {
                    return false;
                }

                String childName = safeName(child);
                String displayPath = basePath != null && !basePath.isEmpty()
                    ? basePath + "/" + childName : childName;

                boolean res;
                if (child.isDirectory()) {
                    res = processDocumentTree(child, displayPath, request, callback,
                            fileCount, matchCount, allMatches);
                } else {
                    res = processDocument(child, displayPath, request, callback,
                            fileCount, matchCount, allMatches);
                }

                if (!res) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean processDocument(DocumentFile document, String displayPath, SearchRequest request,
                                   ProgressCallback callback, AtomicInteger fileCount,
                                   AtomicInteger matchCount, List<GrepView.Data> allMatches) {
        if (cancelled.get()) {
            return false;
        }

        if (document == null) {
            return true;
        }

        String name = safeName(document);
        if (!matchesExtension(name, request.prefs)) {
            return true;
        }

        InputStream is = null;
        BufferedReader br = null;
        try {
            is = new BufferedInputStream(contentResolver.openInputStream(document.getUri()), 65536);
            is.mark(65536);

            // Detect character encoding
            String encode;
            UniversalDetector detector = new UniversalDetector(null);
            try {
                int nread;
                byte[] buff = new byte[4096];
                if ((nread = is.read(buff)) > 0) {
                    detector.handleData(buff, 0, nread);
                }
                detector.dataEnd();
                encode = detector.getDetectedCharset();
                detector.reset();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }
            is.reset();

            if (encode != null) {
                br = new BufferedReader(new InputStreamReader(is, encode), 8192);
            } else {
                br = new BufferedReader(new InputStreamReader(is), 8192);
            }

            String text;
            int line = 0;
            boolean found = false;
            Pattern pattern = request.pattern;
            Matcher m = null;
            List<GrepView.Data> batch = new ArrayList<>();

            int currentFileCount = fileCount.incrementAndGet();

            while ((text = br.readLine()) != null) {
                if (cancelled.get()) {
                    break;
                }

                line++;
                if (m == null) {
                    m = pattern.matcher(text);
                } else {
                    m.reset(text);
                }

                if (m.find()) {
                    found = true;
                    int currentMatchCount = matchCount.incrementAndGet();

                    GrepView.Data data = new GrepView.Data(document.getUri(), displayPath, line, text);
                    batch.add(data);
                    allMatches.add(data);

                    // Send progress updates for first 10 matches
                    if (currentMatchCount < 10) {
                        callback.onProgress(new SearchProgress(
                            request.query,
                            currentFileCount,
                            currentMatchCount,
                                new ArrayList<>(batch)
                        ));
                        batch.clear();
                    }
                }
            }

            // Send remaining batch
            if (!batch.isEmpty()) {
                callback.onProgress(new SearchProgress(
                    request.query,
                    currentFileCount,
                    matchCount.get(),
                        new ArrayList<>(batch)
                ));
            }

            // Send periodic progress updates for files without matches
            if (!found && currentFileCount % 10 == 0) {
                callback.onProgress(new SearchProgress(
                    request.query,
                    currentFileCount,
                    matchCount.get(),
                    Collections.emptyList()
                ));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private String safeName(DocumentFile file) {
        if (file == null) {
            return "";
        }
        String name = file.getName();
        if (name == null) {
            Uri uri = file.getUri();
            name = uri.getLastPathSegment();
        }
        return name != null ? name : "";
    }

    private boolean matchesExtension(String fileName, Prefs prefs) {
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase(java.util.Locale.ROOT);
        boolean allow = false;
        boolean hasEnabled = false;

        for (CheckedString ext : prefs.mExtList) {
            if (ext.checked) {
                hasEnabled = true;
                if ("*".equals(ext.string)) {
                    if (lower.indexOf('.') == -1) {
                        allow = true;
                        break;
                    }
                } else if (lower.endsWith("." + ext.string.toLowerCase(java.util.Locale.ROOT))) {
                    allow = true;
                    break;
                }
            }
        }
        return !hasEnabled || allow;
    }

    @Override
    public void cancel() {
        cancelled.set(true);
        if (currentTask != null) {
            currentTask.cancel(true);
        }
    }

    @Override
    public void shutdown() {
        cancel();
        executor.shutdown();
    }
}
