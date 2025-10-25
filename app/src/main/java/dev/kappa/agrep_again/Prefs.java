package dev.kappa.agrep_again;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;


public class Prefs
{
    public static final String KEY_IGNORE_CASE = "IgnoreCase";
    public static final String KEY_REGULAR_EXPRESSION = "RegularExpression";
    public static final String KEY_TARGET_EXTENSIONS_OLD = "TargetExtensions";
    public static final String KEY_TARGET_DIRECTORIES_OLD = "TargetDirectories";
    public static final String KEY_TARGET_EXTENSIONS_NEW = "TargetExtensionsNew";
    public static final String KEY_TARGET_DIRECTORIES_NEW = "TargetDirectoriesNew";
    public static final String KEY_TARGET_DIRECTORIES_TREE = "TargetDirectoriesTree";
    public static final String KEY_FONTSIZE = "FontSize";
    public static final String KEY_HIGHLIGHTFG = "HighlightFg";
    public static final String KEY_HIGHLIGHTBG = "HighlightBg";
    public static final String KEY_ADD_LINENUMBER = "AddLineNumber";

    private static final String PREF_RECENT= "recent";
    private static final String KEY_DIRECTORY_MIGRATION_PROMPTED = "DirectoryMigrationPrompted";

    boolean mRegularExpression = false;
    boolean mIgnoreCase = true;
    int mFontSize = 16;
    int mHighlightBg = 0xFF00FFFF;
    int mHighlightFg = 0xFF000000;
    boolean addLineNumber=false;
    ArrayList<CheckedString> mDirList = new ArrayList<>();
    ArrayList<CheckedString> mExtList = new ArrayList<>();
    ArrayList<String> mLegacyDirectories = new ArrayList<>();
    boolean needsDirectoryMigration = false;
    boolean shouldPromptDirectoryMigration = false;

    static public Prefs loadPrefs(Context ctx)
    {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

        Prefs prefs = new Prefs();

        // target directory
        prefs.loadDirectories(sp);

        // target extensions
        prefs.loadExtensions(sp);

        prefs.mRegularExpression = sp.getBoolean(KEY_REGULAR_EXPRESSION, false );
        prefs.mIgnoreCase = sp.getBoolean(KEY_IGNORE_CASE, true );

        try {
            prefs.mFontSize = Integer.parseInt( sp.getString( KEY_FONTSIZE , "-1" ) );
        } catch (NumberFormatException e) {
            prefs.mFontSize = 16;
        }
        prefs.mHighlightFg = sp.getInt( KEY_HIGHLIGHTFG , 0xFF000000 );
        prefs.mHighlightBg = sp.getInt( KEY_HIGHLIGHTBG , 0xFF00FFFF );

        prefs.addLineNumber = sp.getBoolean(KEY_ADD_LINENUMBER, false);
        return prefs;
    }

    private void loadDirectories(SharedPreferences sp) {
        String dirsTree = sp.getString(KEY_TARGET_DIRECTORIES_TREE, null);
        if (!TextUtils.isEmpty(dirsTree)) {
            try {
                JSONArray array = new JSONArray(dirsTree);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    boolean checked = object.optBoolean("checked", true);
                    String uri = object.optString("uri", null);
                    String name = object.optString("name", uri);
                    mDirList.add(new CheckedString(checked, uri, name));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (!mDirList.isEmpty()) {
            return;
        }

        String dirs = sp.getString(KEY_TARGET_DIRECTORIES_NEW,"" );
        if (!dirs.isEmpty()){
            String[] dirsarr = dirs.split("\\|");
            int size = dirsarr.length;
            for( int i=0;i<size;i+=2 ){
                boolean c = dirsarr[i].equals("true");
                String legacyPath = dirsarr[i+1];
                mDirList.add(new CheckedString(c,null,legacyPath));
                mLegacyDirectories.add(legacyPath);
            }
        }else{
            dirs = sp.getString(KEY_TARGET_DIRECTORIES_OLD,"" );
            if (!dirs.isEmpty()){
                String[] dirsarr = dirs.split("\\|");
                for (String legacyPath : dirsarr) {
                    mDirList.add(new CheckedString(true, null, legacyPath));
                    mLegacyDirectories.add(legacyPath);
                }
            }
        }
        if (!mLegacyDirectories.isEmpty()){
            needsDirectoryMigration = true;
            shouldPromptDirectoryMigration = !sp.getBoolean(KEY_DIRECTORY_MIGRATION_PROMPTED, false);
        }
    }

    private void loadExtensions(SharedPreferences sp) {
        String exts = sp.getString(KEY_TARGET_EXTENSIONS_NEW,"" );
        if (!exts.isEmpty()){
            String[] arr = exts.split("\\|");
            int size = arr.length;
            for( int i=0;i<size;i+=2 ){
                boolean c = arr[i].equals("true");
                String s = arr[i+1];
                mExtList.add(new CheckedString(c,s));
            }
        }else{
            exts = sp.getString(KEY_TARGET_EXTENSIONS_OLD,"txt" );
            if (!exts.isEmpty()){
                String[] arr = exts.split("\\|");
                for (String s : arr) {
                    mExtList.add(new CheckedString(s));
                }
            }
        }
    }

    public void markMigrationPrompted(Context context) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        editor.putBoolean(KEY_DIRECTORY_MIGRATION_PROMPTED, true);
        editor.apply();
        shouldPromptDirectoryMigration = false;
    }

    public void savePrefs(Context context)
    {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        Editor editor = sp.edit();

        // target directory
        JSONArray dirArray = new JSONArray();
        for( CheckedString t : mDirList ){
            if (!t.hasValue()) {
                continue;
            }
            JSONObject object = new JSONObject();
            try {
                object.put("checked", t.checked);
                object.put("uri", t.string);
                object.put("name", t.getDisplayName());
                dirArray.put(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // target extensions
        StringBuilder exts = new StringBuilder();
        for( CheckedString t : mExtList ){
            exts.append(t.checked);
            exts.append('|');
            exts.append(t.string);
            exts.append('|');
        }
        if ( exts.length() > 0 ){
            exts.deleteCharAt(exts.length()-1);
        }

        if (dirArray.length() > 0) {
            editor.putString(KEY_TARGET_DIRECTORIES_TREE, dirArray.toString() );
            editor.putBoolean(KEY_DIRECTORY_MIGRATION_PROMPTED, true);
            needsDirectoryMigration = false;
            shouldPromptDirectoryMigration = false;
        } else {
            editor.remove(KEY_TARGET_DIRECTORIES_TREE);
        }

        editor.putString(KEY_TARGET_EXTENSIONS_NEW, exts.toString() );
        editor.remove(KEY_TARGET_DIRECTORIES_OLD);
        editor.remove(KEY_TARGET_EXTENSIONS_OLD);
        editor.remove(KEY_TARGET_DIRECTORIES_NEW);
        editor.putBoolean(KEY_REGULAR_EXPRESSION, mRegularExpression);
        editor.putBoolean(KEY_IGNORE_CASE, mIgnoreCase );

        editor.apply();

    }

    public void addRecent(Context context , String searchWord)
    {
        // Write the entry
        final SharedPreferences rsp = context.getSharedPreferences(PREF_RECENT, Context.MODE_PRIVATE);
        Editor reditor = rsp.edit();
        reditor.putLong(searchWord, System.currentTimeMillis());
        reditor.apply();
    }

    public List<String> getRecent(Context context)
    {
        // Load entries
        final SharedPreferences rsp = context.getSharedPreferences(PREF_RECENT, Context.MODE_PRIVATE);
        Map<String,?> all = rsp.getAll();

        // Sort entries
        List<Entry<String,?>> entries = new ArrayList<>(all.entrySet());
        Collections.sort(entries, (e1, e2) -> ((Long)e2.getValue()).compareTo((Long)e1.getValue()));
        // Collect the results
        ArrayList<String> result = new ArrayList<>();
        for (Entry<String,?> entry : entries) {
            result.add(entry.getKey());
        }

        // Remove items beyond the 30th entry
        final int MAX = 30;
        final int size = result.size();
        if ( size > MAX ){
            Editor editor = rsp.edit();
            for( int i=size-1 ; i>=MAX ; i-- ){
                editor.remove(result.get(i));
                result.remove(i);
            }
            editor.apply();
        }
        return result;
    }
}
