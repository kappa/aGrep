package jp.sblo.pandora.aGrep;

import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class FileSelectorActivity extends AppCompatActivity {
    public static final String INTENT_EXTENSION = "ext";
    public static final String INTENT_FILENAME = "filename";
    public static final String INTENT_FILEPATH = "filepath";

    private String m_strDirPath;
    private ArrayAdapter<String> mAdapter;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fileselector);

        // キャンセルの戻り値の指定
        setResult(RESULT_CANCELED);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mListView = findViewById(android.R.id.list);
        if (mListView != null) {
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
                    String strItem = getItemAt(position);

                    if (strItem == null) {
                        return false;
                    }

                    if (strItem.equals("..")) {
                        if (m_strDirPath.lastIndexOf("/") <= 0) {
                            m_strDirPath = m_strDirPath.substring(0, m_strDirPath.lastIndexOf("/") + 1);
                        } else {
                            m_strDirPath = m_strDirPath.substring(0, m_strDirPath.lastIndexOf("/"));
                        }
                        fillList();
                    } else  {
                        setResult(RESULT_OK, getIntent().putExtra(INTENT_FILENAME, strItem)
                                .putExtra(INTENT_FILEPATH, m_strDirPath + "/" + strItem));
                        finish();
                    }
                    return true;
                }

            });

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    handleItemClick(position);
                }
            });
        }

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(FileSelectorActivity.this, R.string.label_unable_access, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            m_strDirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            fillList();
            Toast.makeText(FileSelectorActivity.this, R.string.label_long_press, Toast.LENGTH_LONG).show();
        }

    }

    private void handleItemClick(int position) {
        String strItem = getItemAt(position);
        if (strItem == null) {
            return;
        }

        if (strItem.equals("..")) {
            if (m_strDirPath.lastIndexOf("/") <= 0) {
                m_strDirPath = m_strDirPath.substring(0, m_strDirPath.lastIndexOf("/") + 1);
            } else {
                m_strDirPath = m_strDirPath.substring(0, m_strDirPath.lastIndexOf("/"));
            }
            fillList();
        } else if (strItem.substring(strItem.length() - 1).equals("/")) {
            if (m_strDirPath.equals("/")) {
                m_strDirPath += strItem;
            } else {
                m_strDirPath = m_strDirPath + "/" + strItem;
            }
            m_strDirPath = m_strDirPath.substring(0, m_strDirPath.length() - 1);
            fillList();
        }
    }

    private String getItemAt(int position) {
        if (mAdapter == null || position < 0 || position >= mAdapter.getCount()) {
            return null;
        }
        return mAdapter.getItem(position);
    }

    // ファイルリスト構築
    private void fillList() {
        File[] files = new File(m_strDirPath).listFiles();
        if (files == null) {
            Toast.makeText(FileSelectorActivity.this, R.string.label_unable_access, Toast.LENGTH_SHORT).show();
            return;
        }

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File object1, File object2) {
                final boolean isdir1 = object1.isDirectory();
                final boolean isdir2 = object2.isDirectory();

                if (isdir1 ^ isdir2) {
                    if (isdir1) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
                return object1.getName().compareToIgnoreCase(object2.getName());
            }
        });

        TextView txtDirName = (TextView) findViewById(R.id.txtDirName);
        txtDirName.setText(m_strDirPath);

        ArrayList<String> items = new ArrayList<String>();

        if (!m_strDirPath.equals("/")) {
            items.add("..");
        }

        for (File file : files) {
            if (file.isDirectory()) {
                items.add(file.getName() + "/");
            }
        }

        mAdapter = new ArrayAdapter<String>(this,
                R.layout.file_row, items);
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
