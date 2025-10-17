package jp.sblo.pandora.aGrep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

public class Settings extends AppCompatActivity {

    private Prefs mPrefs;
    private LinearLayout mDirListView;
    private LinearLayout mExtListView;
    private View.OnLongClickListener mDirListener;
    private View.OnLongClickListener mExtListener;
    private CompoundButton.OnCheckedChangeListener mCheckListener;
    private ArrayAdapter<String> mRecentAdapter;
    private Context mContext;
    private ActivityResultLauncher<Uri> mDirPickerLauncher;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        mPrefs = Prefs.loadPrefes(this);

        mDirPickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    handleDirectorySelection(result);
                }
            }
        });

        setContentView(R.layout.main);

        mDirListView = (LinearLayout)findViewById(R.id.listdir);
        mExtListView = (LinearLayout)findViewById(R.id.listext);

        mDirListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view)
            {
                final CheckedString strItem = (CheckedString) view.getTag();
                // Show Dialog
                new AlertDialog.Builder(mContext)
                .setTitle(R.string.label_remove_item_title)
                .setMessage( getString(R.string.label_remove_item , strItem ) )
                .setPositiveButton(R.string.label_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mPrefs.mDirList.remove(strItem);
                        refreshDirList();
                        mPrefs.savePrefs(mContext);
                    }
                })
                .setNegativeButton(R.string.label_CANCEL, null )
                .setCancelable(true)
                .show();
                return true;
            }
        };

        mExtListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view)
            {
                final String strText = (String) ((TextView)view).getText();
                final CheckedString strItem = (CheckedString) view.getTag();
                // Show Dialog
                new AlertDialog.Builder(mContext)
                .setTitle(R.string.label_remove_item_title)
                .setMessage( getString(R.string.label_remove_item , strText ) )
                .setPositiveButton(R.string.label_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mPrefs.mExtList.remove(strItem);
                        refreshExtList();
                        mPrefs.savePrefs(mContext);
                    }
                })
                .setNegativeButton(R.string.label_CANCEL, null )
                .setCancelable(true)
                .show();
                return true;
            }
        };

        mCheckListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final CheckedString strItem = (CheckedString) buttonView.getTag();
                strItem.checked = isChecked;
                mPrefs.savePrefs(mContext);
            }
        };

        refreshDirList();
        refreshExtList();

        if (mPrefs.needsDirectoryMigration) {
            if (mPrefs.shouldPromptDirectoryMigration) {
                showMigrationDialog();
                mPrefs.markMigrationPrompted(this);
            }
        }

        ImageButton btnAddDir = (ImageButton) findViewById(R.id.adddir);
        ImageButton btnAddExt = (ImageButton) findViewById(R.id.addext);

        btnAddDir.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                // ディレクトリ選択
                if (mPrefs.needsDirectoryMigration) {
                    mPrefs.clearLegacyDirectories();
                    refreshDirList();
                }
                mDirPickerLauncher.launch(null);
            }
        });

        btnAddExt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view)
            {
                // Create EditText
                final EditText edtInput = new EditText(mContext);
                edtInput.setSingleLine();
                // Show Dialog
                new AlertDialog.Builder(mContext)
                .setTitle(R.string.label_addext)
                .setView(edtInput)
                .setPositiveButton(R.string.label_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* OKボタンをクリックした時の処理 */

                        String ext = edtInput.getText().toString();
                        if (ext != null && ext.length()>0 ) {
                            // 二重チェック
                            for( CheckedString t : mPrefs.mExtList ){
                                if ( t.string.equalsIgnoreCase(ext)){
                                    return;
                                }
                            }
                            mPrefs.mExtList.add(new CheckedString(ext));
                            refreshExtList();
                            mPrefs.savePrefs(mContext);
                        }
                    }
                })
                .setNeutralButton(R.string.label_no_extension, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* 拡張子無しボタンをクリックした時の処理 */

                        String ext = "*";
                        // 二重チェック
                        for( CheckedString t : mPrefs.mExtList ){
                            if ( t.string.equalsIgnoreCase(ext)){
                                return;
                            }
                        }
                        mPrefs.mExtList.add(new CheckedString(ext));
                        refreshExtList();
                        mPrefs.savePrefs(mContext);
                    }
                })
                .setNegativeButton(R.string.label_CANCEL, null )
                .setCancelable(true)
                .show();
            }
        });


        final CheckBox chkRe = (CheckBox)findViewById(R.id.checkre);
        final CheckBox chkIc = (CheckBox)findViewById(R.id.checkignorecase);

        chkRe.setChecked(mPrefs.mRegularExrpression);
        chkIc.setChecked(mPrefs.mIgnoreCase);

        chkRe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                mPrefs.mRegularExrpression = chkRe.isChecked();
                mPrefs.savePrefs(mContext);
            }
        });
        chkIc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                mPrefs.mIgnoreCase = chkIc.isChecked();
                mPrefs.savePrefs(mContext);
            }
        });

        final AutoCompleteTextView edittext = (AutoCompleteTextView) findViewById(R.id.EditText01);
        edittext.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP ) {
                    String text = edittext.getEditableText().toString();
                    Intent it = new Intent(mContext,Search.class);
                    it.setAction(Intent.ACTION_SEARCH);
                    it.putExtra(SearchManager.QUERY,text );
                    startActivity( it );
                    return true;
                }
                return false;
            }
        });
        mRecentAdapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        edittext.setAdapter(mRecentAdapter);

        ImageButton clrBtn = (ImageButton) findViewById(R.id.ButtonClear);
        clrBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view)
            {
                edittext.setText("");
                edittext.requestFocus();
            }
        });

        ImageButton searchBtn = (ImageButton) findViewById(R.id.ButtonSearch);
        searchBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view)
            {
                String text = edittext.getText().toString();
                Intent it = new Intent(mContext,Search.class);
                it.setAction(Intent.ACTION_SEARCH);
                it.putExtra(SearchManager.QUERY,text );
                startActivity( it );
            }
        });

        ImageButton historyBtn = (ImageButton) findViewById(R.id.ButtonHistory);
        historyBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view)
            {
                edittext.showDropDown();
            }
        });

    }

    private void handleDirectorySelection(Uri uri) {
        if (uri == null) {
            return;
        }

        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        try {
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
        } catch (SecurityException e) {
            android.widget.Toast.makeText(this, R.string.label_unable_access, android.widget.Toast.LENGTH_LONG).show();
            return;
        }

        String uriString = uri.toString();
        for (CheckedString dir : mPrefs.mDirList) {
            if (dir.hasValue() && uriString.equals(dir.string)) {
                return;
            }
        }

        DocumentFile documentFile = DocumentFile.fromTreeUri(this, uri);
        String displayName = documentFile != null ? documentFile.getName() : null;
        if (displayName == null) {
            displayName = uri.getLastPathSegment();
        }
        if (displayName == null) {
            displayName = uriString;
        }

        mPrefs.mDirList.add(new CheckedString(true, uriString, displayName));
        refreshDirList();
        mPrefs.savePrefs(this);
    }

    private void showMigrationDialog() {
        StringBuilder message = new StringBuilder();
        message.append(getString(R.string.label_migrate_directories_message));
        if (!mPrefs.mLegacyDirectories.isEmpty()) {
            message.append("\n\n");
            for (String legacy : mPrefs.mLegacyDirectories) {
                message.append("• ").append(legacy).append('\n');
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.label_migrate_directories_title)
                .setMessage(message.toString())
                .setPositiveButton(R.string.label_adddir, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPrefs.clearLegacyDirectories();
                        refreshDirList();
                        mDirPickerLauncher.launch(null);
                    }
                })
                .setNegativeButton(R.string.label_CANCEL, null)
                .show();
    }

    void setListItem( LinearLayout view ,
            ArrayList<CheckedString> list ,
            View.OnLongClickListener logclicklistener ,
            CompoundButton.OnCheckedChangeListener checkedChangeListener )
    {
        view.removeAllViews();
        Collections.sort(list, new Comparator<CheckedString>() {
            @Override
            public int compare(CheckedString object1, CheckedString object2) {
                String name1 = object1.getDisplayName();
                String name2 = object2.getDisplayName();
                if (name1 == null) {
                    return name2 == null ? 0 : -1;
                }
                if (name2 == null) {
                    return 1;
                }
                return name1.compareToIgnoreCase(name2);
            }
        });
        for( CheckedString s : list ){
            CheckBox v = (CheckBox)View.inflate(this, R.layout.list_dir, null);
            if ( "*".equals(s.string) ){
                v.setText(R.string.label_no_extension);
            }else{
                v.setText(s.getDisplayName());
            }
            v.setChecked( s.checked );
            v.setTag(s);
            v.setOnLongClickListener(logclicklistener);
            v.setOnCheckedChangeListener(checkedChangeListener);
            view.addView(v);
        }
    }

    private void refreshDirList(){
        setListItem( mDirListView , mPrefs.mDirList , mDirListener , mCheckListener);
    }
    private void refreshExtList(){
        setListItem( mExtListView , mPrefs.mExtList , mExtListener , mCheckListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ( item.getItemId() == R.id.menu_option ){
            Intent intent = new Intent( this ,  OptionActivity.class );
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final List<String> recent = mPrefs.getRecent(mContext);
        mRecentAdapter.clear();
        mRecentAdapter.addAll(recent);
        mRecentAdapter.notifyDataSetChanged();
    }
}
