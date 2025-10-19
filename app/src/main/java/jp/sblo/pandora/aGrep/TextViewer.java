package jp.sblo.pandora.aGrep;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.mozilla.universalchardet.UniversalDetector;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class TextViewer extends AppCompatActivity implements OnItemLongClickListener , OnItemClickListener{
    public  static final String EXTRA_LINE = "line";
    public  static final String EXTRA_QUERY = "query";
    public  static final String EXTRA_PATH = "path";
    public  static final String EXTRA_URI = "uri";
    public  static final String EXTRA_DISPLAY_NAME = "display";
    private static final String TAG = "TextViewer";

    private TextLoadTask mTask;
    private String mPatternText;
    private int mLine;
    private Prefs mPrefs;
    private Uri mSourceUri;
    private Uri mGrantedUri;
    private File mSharedCacheFile;
    private String mPath;
    private TextPreview mTextPreview;
    ArrayList<CharSequence> mData = new ArrayList<CharSequence>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.textviewer);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mPrefs = Prefs.loadPrefes(getApplicationContext());
        mTextPreview = (TextPreview)findViewById(R.id.TextPreview);

        mTextPreview.setOnItemLongClickListener(this);
        mTextPreview.setOnItemClickListener(this);

        Intent it = getIntent();
        if (it!=null){
            handleIntent(it);
        }
    }

    private void handleIntent(Intent intent) {
        Bundle extra = intent.getExtras();
        clearSharedUriGrant();
        mSourceUri = null;
        mPath = null;
        mPatternText = null;
        mLine = 0;

        // Support EXTRA_URI from SAF implementation
        if (extra != null && extra.containsKey(EXTRA_URI)) {
            String uriString = extra.getString(EXTRA_URI);
            if (uriString != null) {
                mSourceUri = Uri.parse(uriString);
            }
            mPath = extra.getString(EXTRA_DISPLAY_NAME);
            mPatternText = extra.getString(EXTRA_QUERY);
            mLine = extra.getInt(EXTRA_LINE);
        } else if (extra != null && extra.containsKey(EXTRA_PATH)) {
            mPath = extra.getString(EXTRA_PATH);
            if (!TextUtils.isEmpty(mPath)) {
                mSourceUri = Uri.fromFile(new File(mPath));
            }
            mPatternText = extra.getString(EXTRA_QUERY);
            mLine = extra.getInt(EXTRA_LINE);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            mSourceUri = intent.getData();
            if ("file".equals(mSourceUri.getScheme())) {
                mPath = mSourceUri.getPath();
            } else if ("content".equals(mSourceUri.getScheme())) {
                mPath = resolveDisplayName(mSourceUri);
            } else {
                mPath = mSourceUri.toString();
            }
            if (extra != null) {
                mPatternText = extra.getString(EXTRA_QUERY);
                mLine = extra.getInt(EXTRA_LINE, 0);
            }
        }

        if (mSourceUri == null) {
            finish();
            return;
        }

        if (mPatternText == null) {
            mPatternText = "";
        } else if (!mPrefs.mRegularExrpression) {
            mPatternText = Search.escapeMetaChar(mPatternText);
        }

        if (!TextUtils.isEmpty(mPath)) {
            setTitle( mPath + " - aGrep" );
        } else {
            setTitle(getString(R.string.app_name));
        }
        mTask = new TextLoadTask();
        mTask.execute(mSourceUri);
    }

    private String resolveDisplayName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to resolve display name", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return uri.toString();
    }
    class TextLoadTask extends AsyncTask<Uri, Integer, Boolean >{
        int mOffsetForLine=-1;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(Uri... params)
        {
            if (params == null || params.length == 0) {
                return false;
            }
            Uri target = params[0];
            if (target == null) {
                return false;
            }

            InputStream rawStream = null;
            BufferedInputStream is = null;
            BufferedReader br = null;
            try {
                if ("file".equals(target.getScheme()) || target.getScheme() == null) {
                    File f = new File(target.getPath());
                    if (!f.exists()) {
                        return false;
                    }
                    rawStream = new FileInputStream(f);
                } else {
                    rawStream = getContentResolver().openInputStream(target);
                    if (rawStream == null) {
                        return false;
                    }
                }

                is = new BufferedInputStream(rawStream , 65536 );
                is.mark(65536);

                String encode = null;
                //  文字コードの判定
                UniversalDetector detector = new UniversalDetector(null);
                try{
                    int nread;
                    byte[] buff = new byte[4096];
                    if ((nread = is.read(buff)) > 0 ) {
                        detector.handleData(buff, 0, nread);
                    }
                    detector.dataEnd();
                    encode = detector.getDetectedCharset();
                    is.reset();
                    detector.reset();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                try {
                    if ( encode != null ){
                        br = new BufferedReader( new InputStreamReader( is , encode ) , 8192 );
                    }else{
                        br = new BufferedReader( new InputStreamReader( is ) , 8192 );
                    }

                    String text;
                    while(  ( text = br.readLine() )!=null ){
                        mData.add( text );

                    }
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                closeQuietly(br);
                closeQuietly(is);
                closeQuietly(rawStream);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result ) {

                TextPreview.Adapter adapter = new TextPreview.Adapter(getApplicationContext(),  R.layout.textpreview_row, R.id.TextPreview, mData);
                mData = null;

                Pattern pattern = null;

                if (!TextUtils.isEmpty(mPatternText)) {
                    if ( mPrefs.mIgnoreCase ){
                        pattern = Pattern.compile(mPatternText, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE|Pattern.MULTILINE );
                    }else{
                        pattern = Pattern.compile(mPatternText);
                    }
                }

                adapter.setFormat(pattern , mPrefs.mHighlightFg , mPrefs.mHighlightBg , mPrefs.mFontSize );
                mTextPreview.setAdapter( adapter );

                int height = mTextPreview.getHeight();
                if (mLine > 0) {
                    mTextPreview.setSelectionFromTop( mLine-1 , height / 4 );
                }
                adapter = null;
                mTextPreview = null;
                mTask = null;
            }
        }
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.menu_viewer) {
            Integer lineNumber = null;
            if ( mPrefs.addLineNumber ){
                TextPreview textPreview = (TextPreview)findViewById(R.id.TextPreview);
                if (textPreview != null) {
                    lineNumber = textPreview.getFirstVisiblePosition();
                }
            }
            launchExternalViewer(lineNumber);
            return true;
        }
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchExternalViewer(Integer lineNumber) {
        Uri uri = prepareSharedUri();
        if (uri == null) {
            Toast.makeText(this, R.string.label_open_external_failed, Toast.LENGTH_LONG).show();
            return;
        }
        Uri shareUri = uri;
        if (lineNumber != null) {
            shareUri = uri.buildUpon().appendQueryParameter("line", Integer.toString(lineNumber)).build();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(shareUri, "text/plain");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
            mGrantedUri = shareUri;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.label_open_external_failed, Toast.LENGTH_LONG).show();
            clearSharedUriGrant();
        }
    }

    private Uri prepareSharedUri() {
        if (mSourceUri == null) {
            return null;
        }
        clearSharedUriGrant();
        File cacheDir = new File(getCacheDir(), "shared_texts");
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            return null;
        }
        String scheme = mSourceUri.getScheme();
        String baseName = null;
        if (!TextUtils.isEmpty(mPath)) {
            baseName = new File(mPath).getName();
        }
        if (TextUtils.isEmpty(baseName) && ("file".equals(scheme) || scheme == null)) {
            File sourceFile = new File(mSourceUri.getPath());
            baseName = sourceFile.getName();
        }
        if (TextUtils.isEmpty(baseName)) {
            baseName = "shared.txt";
        }
        String extension = ".txt";
        int dot = baseName.lastIndexOf('.');
        if (dot >= 0 && dot < baseName.length() - 1) {
            extension = baseName.substring(dot);
        }
        File destination;
        try {
            destination = File.createTempFile("viewer_", extension, cacheDir);
        } catch (IOException e) {
            Log.e(TAG, "Failed to create cache file", e);
            return null;
        }
        if (!copySourceTo(destination)) {
            destination.delete();
            return null;
        }
        mSharedCacheFile = destination;
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", destination);
    }

    private boolean copySourceTo(File destination) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = openSourceStream();
            if (in == null) {
                Log.e(TAG, "Unable to open source for sharing: " + mSourceUri);
                return false;
            }
            out = new FileOutputStream(destination);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            out.flush();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy source into cache", e);
        } finally {
            closeQuietly(out);
            closeQuietly(in);
        }
        return false;
    }

    private InputStream openSourceStream() throws IOException {
        if (mSourceUri == null) {
            return null;
        }
        String scheme = mSourceUri.getScheme();
        if ("file".equals(scheme) || scheme == null) {
            File sourceFile = new File(mSourceUri.getPath());
            if (!sourceFile.exists()) {
                return null;
            }
            return new FileInputStream(sourceFile);
        }
        return getContentResolver().openInputStream(mSourceUri);
    }

    private void clearSharedUriGrant() {
        if (mGrantedUri != null) {
            try {
                revokeUriPermission(mGrantedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception e) {
                Log.w(TAG, "Unable to revoke uri permission", e);
            }
            mGrantedUri = null;
        }
        if (mSharedCacheFile != null && mSharedCacheFile.exists()) {
            if (!mSharedCacheFile.delete()) {
                Log.w(TAG, "Failed to delete shared cache file: " + mSharedCacheFile.getAbsolutePath());
            }
        }
        mSharedCacheFile = null;
    }

    @Override
    protected void onDestroy() {
        clearSharedUriGrant();
        super.onDestroy();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        Integer lineNumber = null;
        if ( mPrefs.addLineNumber ){
            lineNumber = position + 1;
        }
        launchExternalViewer(lineNumber);
        return true;
    }
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // テキストのコピー
        ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        TextView tv = (TextView)arg1;
        ClipData clip = ClipData.newPlainText("aGrep Text Viewer",tv.getText());
        cm.setPrimaryClip(clip);

        Toast.makeText(this, R.string.label_copied, Toast.LENGTH_LONG).show();
    }

}
