package jp.sblo.pandora.aGrep;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.mozilla.universalchardet.UniversalDetector;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class TextViewer extends AppCompatActivity implements OnItemLongClickListener , OnItemClickListener{
    public  static final String EXTRA_LINE = "line";
    public  static final String EXTRA_QUERY = "query";
    public  static final String EXTRA_PATH = "path";
    public  static final String EXTRA_URI = "uri";
    public  static final String EXTRA_DISPLAY_NAME = "display";

    private TextLoadTask mTask;
    private String mPatternText;
    private int mLine;
    private Prefs mPrefs;
    private Uri mUri;
    private String mDisplayName;
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

            Bundle extra = it.getExtras();
            if ( extra!=null ){
                String uriString = extra.getString(EXTRA_URI);
                if (uriString != null) {
                    mUri = Uri.parse(uriString);
                }
                if (mUri == null) {
                    String path = extra.getString(EXTRA_PATH);
                    if (path != null) {
                        if (path.startsWith("file://")) {
                            mUri = Uri.parse(path);
                        } else {
                            mUri = Uri.fromFile(new File(path));
                        }
                        mDisplayName = path;
                    }
                }
                mDisplayName = extra.getString(EXTRA_DISPLAY_NAME, mDisplayName);
                mPatternText = extra.getString(EXTRA_QUERY);
                mLine = extra.getInt(EXTRA_LINE);

                if (mUri == null) {
                    finish();
                    return;
                }
                if (mDisplayName == null) {
                    mDisplayName = mUri.getLastPathSegment();
                }

                if ( !mPrefs.mRegularExrpression ){
                    mPatternText = Search.escapeMetaChar(mPatternText);
                }

                setTitle( mDisplayName + " - aGrep" );
                mTask = new TextLoadTask();
                mTask.execute(mUri);
            }

        }
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
            Uri target = params[0];
            if ( target != null ){

                InputStream is;
                try {
                    is = new BufferedInputStream( getContentResolver().openInputStream( target ) , 65536 );
                    is.mark(65536);

                    String encode = null;
                    //  文字コードの判定
                    try{
                        UniversalDetector detector = new UniversalDetector();
                        try{
                            int nread;
                            byte[] buff = new byte[4096];
                            if ((nread = is.read(buff)) > 0 ) {
                                detector.handleData(buff, 0, nread);
                            }
                            detector.dataEnd();
                        }
                        catch (FileNotFoundException e) {
                            e.printStackTrace();
                            return false;
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                            is.close();
                            return false;
                        }
                        encode = detector.getCharset();
                        is.reset();
                        detector.reset();
                        detector.destroy();
                    }
                    catch( UniversalDetector.DetectorException e ){
                    }
                    BufferedReader br=null;
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
                        br.close();
                        is.close();
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (SecurityException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result ) {

                TextPreview.Adapter adapter = new TextPreview.Adapter(getApplicationContext(),  R.layout.textpreview_row, R.id.TextPreview, mData);
                mData = null;

                Pattern pattern;

                if ( mPrefs.mIgnoreCase ){
                    pattern = Pattern.compile(mPatternText, Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE|Pattern.MULTILINE );
                }else{
                    pattern = Pattern.compile(mPatternText);
                }

                adapter.setFormat(pattern , mPrefs.mHighlightFg , mPrefs.mHighlightBg , mPrefs.mFontSize );
                mTextPreview.setAdapter( adapter );

                int height = mTextPreview.getHeight();
                mTextPreview.setSelectionFromTop( mLine-1 , height / 4 );
                adapter = null;
                mTextPreview = null;
                mTask = null;
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
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.menu_viewer) {
            // ビュワー呼び出し
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            if (mUri == null) {
                return true;
            }
            Uri viewUri = mUri;
            if ( mPrefs.addLineNumber ){
                TextPreview textPreview = (TextPreview)findViewById(R.id.TextPreview);
                viewUri = buildViewerUri(textPreview.getFirstVisiblePosition());
            }
            intent.setDataAndType(viewUri, "text/plain");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
            return true;
        }else if ( id == android.R.id.home ){
            finish();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        // ビュワー呼び出し
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        if (mUri == null) {
            return false;
        }
        Uri viewUri = mUri;
        if ( mPrefs.addLineNumber ){
            viewUri = buildViewerUri(1+position);
        }
        intent.setDataAndType(viewUri, "text/plain");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
        return true;
    }

    private Uri buildViewerUri(int line) {
        if (mUri == null) {
            return null;
        }
        if (line < 0) {
            return mUri;
        }
        Uri.Builder builder = mUri.buildUpon();
        builder.clearQuery();
        builder.appendQueryParameter("line", String.valueOf(line));
        return builder.build();
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
