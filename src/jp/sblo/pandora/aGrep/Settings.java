package jp.sblo.pandora.aGrep;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Settings extends AppCompatActivity {

    private Prefs mPrefs;
    private CheckedStringAdapter mDirAdapter;
    private CheckedStringAdapter mExtAdapter;
    private MaterialAutoCompleteTextView mQueryInput;
    private ArrayAdapter<String> mRecentAdapter;
    private SwitchMaterial mRegularExpressionSwitch;
    private SwitchMaterial mIgnoreCaseSwitch;
    private ActivityResultLauncher<Intent> directoryPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = Prefs.loadPrefes(this);
        setContentView(R.layout.main);

        final Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }

        directoryPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleDirectoryResult);

        mQueryInput = findViewById(R.id.query_input);
        mQueryInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mQueryInput.setOnEditorActionListener(this::onQueryEditorAction);
        mRecentAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<String>());
        mQueryInput.setAdapter(mRecentAdapter);

        final MaterialButton historyButton = findViewById(R.id.button_history);
        historyButton.setOnClickListener(v -> mQueryInput.showDropDown());

        final MaterialButton searchButton = findViewById(R.id.button_search);
        searchButton.setOnClickListener(v -> startSearch(mQueryInput.getText()));

        final MaterialButton clearButton = findViewById(R.id.button_clear);
        clearButton.setOnClickListener(v -> {
            if (mQueryInput.getText() != null) {
                mQueryInput.getText().clear();
            }
            mQueryInput.requestFocus();
        });

        final MaterialButton addDirButton = findViewById(R.id.adddir);
        addDirButton.setOnClickListener(v -> {
            final Intent intent = new Intent(Settings.this, FileSelectorActivity.class);
            directoryPickerLauncher.launch(intent);
        });

        final MaterialButton addExtButton = findViewById(R.id.addext);
        addExtButton.setOnClickListener(v -> showAddExtensionDialog());

        mRegularExpressionSwitch = findViewById(R.id.checkre);
        mIgnoreCaseSwitch = findViewById(R.id.checkignorecase);

        mRegularExpressionSwitch.setChecked(mPrefs.mRegularExrpression);
        mIgnoreCaseSwitch.setChecked(mPrefs.mIgnoreCase);

        mRegularExpressionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPrefs.mRegularExrpression = isChecked;
            mPrefs.savePrefs(Settings.this);
        });
        mIgnoreCaseSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPrefs.mIgnoreCase = isChecked;
            mPrefs.savePrefs(Settings.this);
        });

        final RecyclerView dirRecycler = findViewById(R.id.directory_list);
        dirRecycler.setLayoutManager(new LinearLayoutManager(this));
        mDirAdapter = new CheckedStringAdapter(mPrefs.mDirList,
                this::confirmDirectoryRemoval,
                false);
        dirRecycler.setAdapter(mDirAdapter);

        final RecyclerView extRecycler = findViewById(R.id.extension_list);
        extRecycler.setLayoutManager(new LinearLayoutManager(this));
        mExtAdapter = new CheckedStringAdapter(mPrefs.mExtList,
                this::confirmExtensionRemoval,
                true);
        extRecycler.setAdapter(mExtAdapter);

        refreshLists();
    }

    private boolean onQueryEditorAction(TextView textView, int actionId, android.view.KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH
                || (keyEvent != null && keyEvent.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER
                && keyEvent.getAction() == android.view.KeyEvent.ACTION_UP)) {
            startSearch(textView.getText());
            return true;
        }
        return false;
    }

    private void startSearch(CharSequence query) {
        if (query == null) {
            return;
        }
        final String text = query.toString();
        final Intent intent = new Intent(this, Search.class);
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(android.app.SearchManager.QUERY, text);
        startActivity(intent);
    }

    private void handleDirectoryResult(ActivityResult result) {
        if (result.getResultCode() != RESULT_OK) {
            return;
        }
        final Intent data = result.getData();
        if (data == null || data.getExtras() == null) {
            return;
        }
        final String dirname = data.getExtras().getString(FileSelectorActivity.INTENT_FILEPATH);
        if (dirname == null || dirname.length() == 0) {
            return;
        }
        for (CheckedString checkedString : mPrefs.mDirList) {
            if (checkedString.string.equalsIgnoreCase(dirname)) {
                return;
            }
        }
        mPrefs.mDirList.add(new CheckedString(dirname));
        persistDirectoryChanges();
    }

    private void showAddExtensionDialog() {
        final TextInputLayout inputLayout = new TextInputLayout(this);
        inputLayout.setHint(getString(R.string.label_addext));
        final TextInputEditText editText = new TextInputEditText(inputLayout.getContext());
        editText.setSingleLine(true);
        inputLayout.addView(editText);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.label_addext)
                .setView(inputLayout)
                .setPositiveButton(R.string.label_OK, (dialog, which) -> {
                    final String ext = editText.getText() != null ? editText.getText().toString() : "";
                    addExtension(ext);
                })
                .setNeutralButton(R.string.label_no_extension, (dialog, which) -> addExtension("*"))
                .setNegativeButton(R.string.label_CANCEL, null)
                .show();
    }

    private void addExtension(@NonNull String ext) {
        if (ext.length() == 0) {
            return;
        }
        for (CheckedString checkedString : mPrefs.mExtList) {
            if (checkedString.string.equalsIgnoreCase(ext)) {
                return;
            }
        }
        mPrefs.mExtList.add(new CheckedString(ext));
        persistExtensionChanges();
    }

    private void confirmDirectoryRemoval(CheckedString item) {
        showRemovalDialog(item.string, () -> {
            mPrefs.mDirList.remove(item);
            persistDirectoryChanges();
        });
    }

    private void confirmExtensionRemoval(CheckedString item) {
        final String label = "*".equals(item.string)
                ? getString(R.string.label_no_extension)
                : item.string;
        showRemovalDialog(label, () -> {
            mPrefs.mExtList.remove(item);
            persistExtensionChanges();
        });
    }

    private void showRemovalDialog(String text, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.label_remove_item_title)
                .setMessage(getString(R.string.label_remove_item, text))
                .setPositiveButton(R.string.label_OK, (dialog, which) -> onConfirm.run())
                .setNegativeButton(R.string.label_CANCEL, null)
                .show();
    }

    private void persistDirectoryChanges() {
        sortCheckedStrings(mPrefs.mDirList);
        refreshLists();
        mPrefs.savePrefs(this);
    }

    private void persistExtensionChanges() {
        sortCheckedStrings(mPrefs.mExtList);
        refreshLists();
        mPrefs.savePrefs(this);
    }

    private void refreshLists() {
        mDirAdapter.updateItems(mPrefs.mDirList);
        mExtAdapter.updateItems(mPrefs.mExtList);
    }

    private void sortCheckedStrings(List<CheckedString> list) {
        if (list == null) {
            return;
        }
        Collections.sort(list, new Comparator<CheckedString>() {
            @Override
            public int compare(CheckedString o1, CheckedString o2) {
                return o1.string.compareToIgnoreCase(o2.string);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_option) {
            startActivity(new Intent(this, OptionActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPrefs = Prefs.loadPrefes(this);
        mRegularExpressionSwitch.setChecked(mPrefs.mRegularExrpression);
        mIgnoreCaseSwitch.setChecked(mPrefs.mIgnoreCase);
        refreshLists();

        final List<String> recent = mPrefs.getRecent(this);
        mRecentAdapter.clear();
        mRecentAdapter.addAll(recent);
        mRecentAdapter.notifyDataSetChanged();
    }

    private class CheckedStringAdapter extends RecyclerView.Adapter<CheckedStringAdapter.ViewHolder> {

        private final RemovalCallback removalCallback;
        private final boolean labelNoExtension;
        private final List<CheckedString> items = new ArrayList<>();

        CheckedStringAdapter(List<CheckedString> initial,
                             RemovalCallback removalCallback,
                             boolean labelNoExtension) {
            this.removalCallback = removalCallback;
            this.labelNoExtension = labelNoExtension;
            updateItems(initial);
        }

        void updateItems(List<CheckedString> newItems) {
            items.clear();
            if (newItems != null) {
                items.addAll(newItems);
                Collections.sort(items, new Comparator<CheckedString>() {
                    @Override
                    public int compare(CheckedString o1, CheckedString o2) {
                        return o1.string.compareToIgnoreCase(o2.string);
                    }
                });
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view = getLayoutInflater().inflate(R.layout.item_checked_row, parent, false);
            return new ViewHolder((MaterialCheckBox) view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final MaterialCheckBox checkBox;

            ViewHolder(MaterialCheckBox checkBox) {
                super(checkBox);
                this.checkBox = checkBox;
            }

            void bind(CheckedString data) {
                checkBox.setOnCheckedChangeListener(null);
                final String display = labelNoExtension && "*".equals(data.string)
                        ? getString(R.string.label_no_extension)
                        : data.string;
                checkBox.setText(display);
                checkBox.setChecked(data.checked);
                checkBox.setContentDescription(display);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    data.checked = isChecked;
                    mPrefs.savePrefs(Settings.this);
                });
                checkBox.setOnLongClickListener(v -> {
                    removalCallback.onRemove(data);
                    return true;
                });
            }
        }
    }

    private interface RemovalCallback {
        void onRemove(CheckedString item);
    }
}
