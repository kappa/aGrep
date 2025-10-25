package dev.kappa.agrep_again;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.net.Uri;
import android.view.MenuItem;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

/**
 * Hosts application options backed by {@link androidx.preference.PreferenceFragmentCompat}.
 */
public class OptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        final Toolbar toolbar = findViewById(R.id.option_toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new OptionFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class OptionFragment extends PreferenceFragmentCompat {

        private static final String KEY_VERSION = "pref_version";
        private static final String KEY_COLOR_FOREGROUND = Prefs.KEY_HIGHLIGHTFG;
        private static final String KEY_COLOR_BACKGROUND = Prefs.KEY_HIGHLIGHTBG;

        private ActivityResultLauncher<Intent> colorPickerLauncher;
        private String pendingColorPreferenceKey;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_options, rootKey);

            final Context context = requireContext();
            colorPickerLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    this::handleColorPickerResult);
            bindVersionPreference(context);
            bindFontSizePreference();
            bindHighlightPreference(KEY_COLOR_BACKGROUND, R.string.label_highlight_bg);
            bindHighlightPreference(KEY_COLOR_FOREGROUND, R.string.label_highlight_fg);
            bindAddLineNumberPreference();
            bindIconCreditPreference();
        }

        private void bindVersionPreference(Context context) {
            final Preference preference = findPreference(KEY_VERSION);
            if (preference == null) {
                return;
            }

            try {
                final String versionName = context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0)
                        .versionName;
                preference.setTitle(getString(R.string.version, versionName));
            } catch (PackageManager.NameNotFoundException e) {
                preference.setVisible(false);
                return;
            }

            preference.setSummary(R.string.link);
            preference.setOnPreferenceClickListener(pref -> {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=jp.sblo.pandora.aGrep"));
                startActivity(intent);
                return true;
            });
        }

        private void bindFontSizePreference() {
            final ListPreference preference = findPreference(Prefs.KEY_FONTSIZE);
            if (preference == null) {
                return;
            }
            preference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        }

        private void bindHighlightPreference(String key, int titleResId) {
            final Preference preference = findPreference(key);
            if (preference == null) {
                return;
            }
            preference.setTitle(titleResId);
            updateColorSummary(preference, key);
            preference.setOnPreferenceClickListener(pref -> {
                pendingColorPreferenceKey = key;
                final Intent intent = new Intent(requireContext(), ColorPickerActivity.class);
                intent.putExtra(ColorPickerActivity.EXTRA_TITLE, pref.getTitle());
                colorPickerLauncher.launch(intent);
                return true;
            });
        }

        private void bindAddLineNumberPreference() {
            final SwitchPreferenceCompat preference = findPreference(Prefs.KEY_ADD_LINENUMBER);
            if (preference == null) {
                return;
            }
            preference.setSummary(R.string.summary_add_linenumber);
        }

        private void bindIconCreditPreference() {
            final Preference preference = findPreference("pref_icon_credit");
            if (preference == null) {
                return;
            }
            preference.setOnPreferenceClickListener(pref -> {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.iconlink)));
                startActivity(intent);
                return true;
            });
        }

        private void handleColorPickerResult(ActivityResult result) {
            if (result.getResultCode() != Activity.RESULT_OK || pendingColorPreferenceKey == null) {
                pendingColorPreferenceKey = null;
                return;
            }
            final Intent data = result.getData();
            if (data == null) {
                pendingColorPreferenceKey = null;
                return;
            }
            final int fallbackColor = Prefs.KEY_HIGHLIGHTFG.equals(pendingColorPreferenceKey)
                    ? 0xFF000000
                    : OptionActivity.DefaultHighlightColor;
            final int color = data.getIntExtra(ColorPickerActivity.EXTRA_COLOR, fallbackColor);
            final SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(requireContext());
            sharedPreferences.edit().putInt(pendingColorPreferenceKey, color).apply();

            final Preference preference = findPreference(pendingColorPreferenceKey);
            if (preference != null) {
                updateColorSummary(preference, pendingColorPreferenceKey);
            }
            pendingColorPreferenceKey = null;
        }

        private void updateColorSummary(@NonNull Preference preference, @NonNull String key) {
            final SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(requireContext());
            final int color = sharedPreferences.getInt(key,
                    key.equals(Prefs.KEY_HIGHLIGHTFG) ? 0xFF000000 : OptionActivity.DefaultHighlightColor);
            final String summary = getString(R.string.summary_color_value,
                    String.format("#%08X", color));
            preference.setSummary(summary);
        }
    }

    static final int DefaultHighlightColor = 0xFF00FFFF;
}
