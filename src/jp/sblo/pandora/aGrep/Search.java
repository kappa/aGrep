package jp.sblo.pandora.aGrep;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.universalchardet.UniversalDetector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;



@SuppressLint("DefaultLocale")
public class Search extends AppCompatActivity implements GrepView.Callback
{
    private GrepView mGrepView;
    private GrepView.GrepAdapter mAdapter;
    private ArrayList<GrepView.Data>	mData ;
    private GrepTask mTask;
    private String mQuery;
    private Pattern mPattern;

    private Prefs mPrefs;

    private boolean mPendingSearch;
    private boolean mPendingManageSettings;
    private boolean mManageEducationShown;

    private static final int REQUEST_MEDIA_PERMISSIONS = 0x2001;
    private static final int REQUEST_READ_STORAGE = 0x2002;

    private static final Set<String> IMAGE_EXTENSIONS = buildExtensionSet(new String[]{
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif"
    });
    private static final Set<String> VIDEO_EXTENSIONS = buildExtensionSet(new String[]{
            "mp4", "mkv", "avi", "mov", "wmv", "webm", "3gp"
    });
    private static final Set<String> AUDIO_EXTENSIONS = buildExtensionSet(new String[]{
            "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma"
    });
    private static final String PERMISSION_READ_MEDIA_AUDIO = "android.permission.READ_MEDIA_AUDIO";
    private static final String PERMISSION_READ_MEDIA_IMAGES = "android.permission.READ_MEDIA_IMAGES";
    private static final String PERMISSION_READ_MEDIA_VIDEO = "android.permission.READ_MEDIA_VIDEO";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mPrefs = Prefs.loadPrefes(this);

        setContentView(R.layout.result);

        if (mPrefs.needsDirectoryMigration) {
            showMigrationRequiredDialog();
            return;
        }

        if (!hasActiveDirectories()) {
            Toast.makeText(getApplicationContext(), R.string.label_no_target_dir, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, Settings.class));
            finish();
            return;
        }

        mGrepView = (GrepView)findViewById(R.id.DicView01);
        mData = new ArrayList<GrepView.Data>();
        mAdapter = new GrepView.GrepAdapter(getApplicationContext(), R.layout.list_row, R.id.DicView01, mData);
        mGrepView.setAdapter( mAdapter );
        mGrepView.setCallback(this);

        Intent it = getIntent();

        if (it != null &&
            Intent.ACTION_SEARCH.equals(it.getAction()) )
        {
            Bundle extras = it.getExtras();
            mQuery = extras.getString(SearchManager.QUERY);

            if ( mQuery!=null && mQuery.length() >0 ){

                mPrefs.addRecent(this , mQuery);

                String patternText = mQuery;
                if ( !mPrefs.mRegularExrpression ){
                    patternText = escapeMetaChar(patternText);
                    patternText = convertOrPattern(patternText);
                }

                if ( mPrefs.mIgnoreCase ){
//                        mPatternText = text.toLowerCase();
                    mPattern = Pattern.compile(patternText, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE|Pattern.MULTILINE );
                }else{
//                        mPatternText = text;
                    mPattern = Pattern.compile(patternText);
                }

                if ( Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState() ) ) {
                    mData.clear();
                    mAdapter.setFormat( mPattern , mPrefs.mHighlightFg , mPrefs.mHighlightBg , mPrefs.mFontSize );
                    beginSearch();
                }
            }else{
                finish();
            }
        }
    }

    static public String escapeMetaChar( String pattern )
    {
        final String metachar = ".^${}[]*+?|()\\";

        StringBuilder newpat = new StringBuilder();

        int len = pattern.length();

        for( int i=0;i<len;i++ ){
            char c = pattern.charAt(i);
            if ( metachar.indexOf(c) >=0 ){
                newpat.append('\\');
            }
            newpat.append(c);
        }
        return newpat.toString();
    }

    static public String convertOrPattern( String pattern )
    {
        if ( pattern.contains(" ") ){
            return "(" + pattern.replace(" ", "|") + ")";
        }else{
            return pattern;
        }
    }

    private static Set<String> buildExtensionSet(String[] values) {
        HashSet<String> set = new HashSet<String>();
        if (values != null) {
            for (String value : values) {
                if (value != null) {
                    set.add(value.toLowerCase());
                }
            }
        }
        return set;
    }

    private boolean hasActiveDirectories() {
        for (CheckedString dir : mPrefs.mDirList) {
            if (dir.checked && dir.hasValue()) {
                return true;
            }
        }
        return false;
    }

    private void showMigrationRequiredDialog() {
        mPrefs.markMigrationPrompted(this);
        new AlertDialog.Builder(this)
                .setTitle(R.string.label_migrate_directories_title)
                .setMessage(R.string.label_migrate_directories_blocking)
                .setPositiveButton(R.string.label_adddir, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Search.this, Settings.class));
                        finish();
                    }
                })
                .setNegativeButton(R.string.label_CANCEL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void beginSearch() {
        mPendingSearch = true;
        if (ensureStoragePermissions()) {
            startGrepTask();
        }
    }

    private void startGrepTask() {
        if (mTask != null) {
            mTask.cancel(true);
        }
        mPendingSearch = false;
        mTask = new GrepTask();
        mTask.execute(mQuery);
    }

    private boolean ensureStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && isAllFilesMode()) {
            if (!Environment.isExternalStorageManager()) {
                if (!mManageEducationShown) {
                    showManageStorageDialog();
                    mManageEducationShown = true;
                } else if (!mPendingManageSettings) {
                    showPermissionDenied();
                    finish();
                }
                return false;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ArrayList<String> needed = collectMediaPermissions();
            if (!needed.isEmpty()) {
                ActivityCompat.requestPermissions(this, needed.toArray(new String[needed.size()]), REQUEST_MEDIA_PERMISSIONS);
                return false;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
                return false;
            }
        }
        return true;
    }

    private ArrayList<String> collectMediaPermissions() {
        ArrayList<String> permissions = new ArrayList<String>();
        boolean needsAudio = false;
        boolean needsImage = false;
        boolean needsVideo = false;
        boolean wildcard = false;
        for (CheckedString ext : mPrefs.mExtList) {
            if (!ext.checked || ext.string == null) {
                continue;
            }
            String lower = ext.string.toLowerCase();
            if ("*".equals(lower)) {
                wildcard = true;
                break;
            }
            if (IMAGE_EXTENSIONS.contains(lower)) {
                needsImage = true;
            }
            if (VIDEO_EXTENSIONS.contains(lower)) {
                needsVideo = true;
            }
            if (AUDIO_EXTENSIONS.contains(lower)) {
                needsAudio = true;
            }
        }
        if (wildcard) {
            needsAudio = true;
            needsImage = true;
            needsVideo = true;
        }
        if (needsAudio && ContextCompat.checkSelfPermission(this, PERMISSION_READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(PERMISSION_READ_MEDIA_AUDIO);
        }
        if (needsImage && ContextCompat.checkSelfPermission(this, PERMISSION_READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(PERMISSION_READ_MEDIA_IMAGES);
        }
        if (needsVideo && ContextCompat.checkSelfPermission(this, PERMISSION_READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(PERMISSION_READ_MEDIA_VIDEO);
        }
        return permissions;
    }

    private boolean isAllFilesMode() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        for (CheckedString dir : mPrefs.mDirList) {
            if (!dir.checked || !dir.hasValue()) {
                continue;
            }
            Uri uri = Uri.parse(dir.string);
            String documentId = DocumentsContract.getTreeDocumentId(uri);
            if (documentId != null && documentId.endsWith(":")) {
                return true;
            }
        }
        return false;
    }

    private void showManageStorageDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.label_manage_storage_title)
                .setMessage(R.string.label_manage_storage_message)
                .setPositiveButton(R.string.label_open_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        mPendingManageSettings = true;
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.label_CANCEL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showPermissionDenied();
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showPermissionDenied() {
        Toast.makeText(this, R.string.label_permission_denied, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPendingManageSettings) {
            mPendingManageSettings = false;
            if (mPendingSearch) {
                if (ensureStoragePermissions()) {
                    startGrepTask();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                    showPermissionDenied();
                    finish();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MEDIA_PERMISSIONS || requestCode == REQUEST_READ_STORAGE) {
            boolean granted = true;
            if (grantResults != null) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        granted = false;
                        break;
                    }
                }
            }
            if (granted) {
                if (mPendingSearch) {
                    startGrepTask();
                }
            } else {
                mPendingSearch = false;
                showPermissionDenied();
                finish();
            }
        }
    }


    class GrepTask extends AsyncTask<String, GrepView.Data, Boolean>
    {
        private ProgressDialog mProgressDialog;
        private int mFileCount=0;
        private int mFoundcount=0;
        private boolean mCancelled;

        @Override
        protected void onPreExecute() {
            mCancelled=false;
            mProgressDialog = new ProgressDialog(Search.this);
            mProgressDialog.setTitle(R.string.grep_spinner);
            mProgressDialog.setMessage(mQuery);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog)
                {
                    mCancelled=true;
                    cancel(false);
                }
            });
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params)
        {
            return grepRoot( params[0] );
        }


        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
            synchronized( mData ){
                Collections.sort( mData , new GrepView.Data() );
                mAdapter.notifyDataSetChanged();
            }
            mGrepView.setSelection(0);
            Toast.makeText(getApplicationContext(),result?R.string.grep_finished:R.string.grep_canceled, Toast.LENGTH_LONG).show();
            mData = null;
            mAdapter = null;
            mTask = null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            onPostExecute(false);
        }

        @Override
        protected void onProgressUpdate(GrepView.Data... progress)
        {
            if ( isCancelled() ){
                return;
            }
            mProgressDialog.setMessage( Search.this.getString(R.string.progress ,mQuery,mFileCount));
            if ( progress != null ){
                synchronized( mData ){
                    for( GrepView.Data data : progress ){
                        mData.add(data);
                    }
                    mAdapter.notifyDataSetChanged();
                    mGrepView.setSelection(mData.size()-1);
                }
            }
        }


        boolean grepRoot( String text )
        {
            for( CheckedString dir : mPrefs.mDirList ){
                if (isCancelled()){
                    return false;
                }
                if ( dir.checked && dir.hasValue() ){
                    Uri uri = Uri.parse(dir.string);
                    DocumentFile root = DocumentFile.fromTreeUri(Search.this, uri);
                    if (root != null && root.isDirectory()) {
                        String base = dir.getDisplayName();
                        if (base == null) {
                            base = safeName(root);
                        }
                        if (!grepDocumentTree(root, base != null ? base : "")) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        boolean grepDocumentTree(DocumentFile dir, String basePath )
        {
            if ( isCancelled() ){
                return false;
            }
            DocumentFile[] children = dir.listFiles();
            if (children != null) {
                for (DocumentFile child : children) {
                    if (isCancelled()) {
                        return false;
                    }
                    String childName = safeName(child);
                    String displayPath = basePath != null && basePath.length() > 0 ? basePath + "/" + childName : childName;
                    boolean res;
                    if (child.isDirectory()) {
                        res = grepDocumentTree(child, displayPath);
                    } else {
                        res = grepDocument(child, displayPath);
                    }
                    if (!res) {
                        return false;
                    }
                }
            }
            return true;
        }


        boolean grepDocument( DocumentFile document , String displayPath )
        {
            if ( isCancelled() ){
                return false;
            }
            if ( document==null ){
                return false;
            }

            String name = safeName(document);
            if (!matchesExtension(name)) {
                return true;
            }

            InputStream is;
            try {
                is = new BufferedInputStream( getContentResolver().openInputStream(document.getUri()) , 65536 );
                is.mark(65536);

                //  文字コードの判定
                String encode = null;
                try{
                    UniversalDetector detector = new UniversalDetector();
                    try{
                        int nread;
                        byte[] buff = new byte[4096];
                        if ((nread = is.read(buff)) > 0 ) {
                            detector.handleData(buff, 0,nread);
                        }
                        detector.dataEnd();
                    }
                    catch( FileNotFoundException e ){
                        e.printStackTrace();
                        is.close();
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        is.close();
                        return true;
                    }
                    encode = detector.getCharset();
                    detector.reset();
                    detector.destroy();
                }
                catch( UniversalDetector.DetectorException e){
                }
                is.reset();

                BufferedReader br=null;
                try {
                    if ( encode != null ){
                        br = new BufferedReader( new InputStreamReader( is , encode ) , 8192 );

                    }else{
                        br = new BufferedReader( new InputStreamReader( is ) , 8192 );
                    }

                    String text;
                    int line = 0;
                    boolean found = false;
                    Pattern pattern = mPattern;
                    Matcher m = null;
                    ArrayList<GrepView.Data>    data  = null ;
                    mFileCount++;
                    while(  ( text = br.readLine() )!=null ){
                        line ++;
                        if ( m== null ){
                            m = pattern.matcher( text );
                        }else{
                            m.reset( text );
                        }
                        if ( m.find() ){
                            found = true;

                            synchronized( mData ){
                                mFoundcount++;
                                if ( data == null ){
                                    data = new ArrayList<GrepView.Data>();
                                }
                                data.add(new GrepView.Data(document.getUri(), displayPath, line, text));

                                if ( mFoundcount < 10 ){
                                    publishProgress(data.toArray(new GrepView.Data[0]));
                                    data = null;
                                }
                            }
                            if ( mCancelled ){
                                break;
                            }
                        }
                    }
                    br.close();
                    is.close();
                    if ( data != null ){
                        publishProgress(data.toArray(new GrepView.Data[0]));
                        data=null;
                    }
                    if ( !found ) {
                        if ( mFileCount % 10 == 0 ){
                            publishProgress((GrepView.Data[])null);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
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
                name = uri != null ? uri.getLastPathSegment() : "";
            }
            return name != null ? name : "";
        }

        private boolean matchesExtension(String fileName) {
            if (fileName == null) {
                return false;
            }
            String lower = fileName.toLowerCase();
            boolean allow = false;
            boolean hasEnabled = false;
            for( CheckedString ext : mPrefs.mExtList ){
                if ( ext.checked ){
                    hasEnabled = true;
                    if ( "*".equals(ext.string) ){
                        if ( lower.indexOf('.')== -1 ){
                            allow = true;
                            break;
                        }
                    }else if ( lower.endsWith("."+ext.string.toLowerCase())){
                        allow = true;
                        break;
                    }
                }
            }
            return hasEnabled ? allow : true;
        }
    }

    public static SpannableString highlightKeyword(CharSequence text, Pattern p, int fgcolor, int bgcolor)
    {
        SpannableString ss = new SpannableString(text);

        int start = 0;
        int end;
        Matcher m = p.matcher( text );
        while (m.find(start)) {
            start = m.start();
            end = m.end();

            BackgroundColorSpan bgspan = new BackgroundColorSpan(bgcolor);
            ss.setSpan(bgspan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ForegroundColorSpan fgspan = new ForegroundColorSpan(fgcolor);
            ss.setSpan(fgspan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            start = end;
        }
        return ss;
    }

    @Override
    public void onGrepItemClicked(int position)
    {
        GrepView.Data data = (GrepView.Data) mGrepView.getAdapter().getItem(position);

        Intent it = new Intent(this,TextViewer.class);

        if (data.mUri != null) {
            it.putExtra(TextViewer.EXTRA_URI , data.mUri.toString() );
        }
        it.putExtra(TextViewer.EXTRA_DISPLAY_NAME, data.mDisplayName);
        it.putExtra(TextViewer.EXTRA_QUERY, mQuery);
        it.putExtra(TextViewer.EXTRA_LINE, data.mLinenumber );

        startActivity(it);
    }

    @Override
    public boolean onGrepItemLongClicked(int position)
    {
        return false;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
}
