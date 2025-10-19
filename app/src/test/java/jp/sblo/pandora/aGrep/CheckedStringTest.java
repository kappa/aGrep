package jp.sblo.pandora.aGrep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CheckedStringTest {

    @Test
    public void singleArgConstructor_defaultsToChecked() {
        CheckedString cs = new CheckedString("test");

        assertTrue(cs.checked);
        assertEquals("test", cs.string);
        assertEquals("test", cs.displayName);
    }

    @Test
    public void twoArgConstructor_usesValueAsDisplayName() {
        CheckedString cs = new CheckedString(false, "value");

        assertFalse(cs.checked);
        assertEquals("value", cs.string);
        assertEquals("value", cs.displayName);
    }

    @Test
    public void twoArgConstructor_preservesCheckedState() {
        CheckedString checkedTrue = new CheckedString(true, "test");
        CheckedString checkedFalse = new CheckedString(false, "test");

        assertTrue(checkedTrue.checked);
        assertFalse(checkedFalse.checked);
    }

    @Test
    public void threeArgConstructor_setsAllFields() {
        CheckedString cs = new CheckedString(true, "value", "label");

        assertTrue(cs.checked);
        assertEquals("value", cs.string);
        assertEquals("label", cs.displayName);
    }

    @Test
    public void threeArgConstructor_handlesNullLabel() {
        CheckedString cs = new CheckedString(true, "value", null);

        assertTrue(cs.checked);
        assertEquals("value", cs.string);
        assertEquals("value", cs.displayName);
    }

    @Test
    public void threeArgConstructor_preservesAllStates() {
        CheckedString cs1 = new CheckedString(false, "uri123", "My Folder");

        assertFalse(cs1.checked);
        assertEquals("uri123", cs1.string);
        assertEquals("My Folder", cs1.displayName);
    }

    @Test
    public void getDisplayName_returnsDisplayNameWhenSet() {
        CheckedString cs = new CheckedString(true, "value", "label");

        assertEquals("label", cs.getDisplayName());
    }

    @Test
    public void getDisplayName_fallsBackToStringWhenDisplayNameIsNull() {
        CheckedString cs = new CheckedString(true, "value", "label");
        cs.displayName = null;

        assertEquals("value", cs.getDisplayName());
    }

    @Test
    public void getDisplayName_fallsBackToStringWhenBothNull() {
        CheckedString cs = new CheckedString(true, "value", "label");
        cs.displayName = null;
        cs.string = null;

        assertEquals(null, cs.getDisplayName());
    }

    @Test
    public void setDisplayName_updatesDisplayName() {
        CheckedString cs = new CheckedString("test");

        cs.setDisplayName("new label");

        assertEquals("new label", cs.displayName);
        assertEquals("new label", cs.getDisplayName());
    }

    @Test
    public void setDisplayName_canSetToNull() {
        CheckedString cs = new CheckedString(true, "value", "label");

        cs.setDisplayName(null);

        assertEquals(null, cs.displayName);
        assertEquals("value", cs.getDisplayName()); // Falls back to string
    }

    @Test
    public void hasValue_returnsTrueForNonEmptyString() {
        CheckedString cs = new CheckedString("test");

        assertTrue(cs.hasValue());
    }

    @Test
    public void hasValue_returnsFalseForEmptyString() {
        CheckedString cs = new CheckedString("");

        assertFalse(cs.hasValue());
    }

    @Test
    public void hasValue_returnsFalseForNullString() {
        CheckedString cs = new CheckedString(true, null, "label");

        assertFalse(cs.hasValue());
    }

    @Test
    public void hasValue_returnsTrueForWhitespaceString() {
        CheckedString cs = new CheckedString("   ");

        // hasValue() only checks null and isEmpty(), not trimmed
        assertTrue(cs.hasValue());
    }

    @Test
    public void hasValue_returnsTrueForSingleSpace() {
        CheckedString cs = new CheckedString(" ");

        assertTrue(cs.hasValue());
    }

    @Test
    public void handlesSpecialCharactersInStrings() {
        CheckedString cs = new CheckedString(true, "test@#$%^&*()", "label!@#$");

        assertEquals("test@#$%^&*()", cs.string);
        assertEquals("label!@#$", cs.displayName);
        assertTrue(cs.hasValue());
    }

    @Test
    public void handlesUnicodeCharacters() {
        CheckedString cs = new CheckedString(true, "日本語", "한글");

        assertEquals("日本語", cs.string);
        assertEquals("한글", cs.displayName);
        assertTrue(cs.hasValue());
    }

    @Test
    public void handlesEmojis() {
        CheckedString cs = new CheckedString(true, "😀🎉", "🔥💯");

        assertEquals("😀🎉", cs.string);
        assertEquals("🔥💯", cs.displayName);
        assertTrue(cs.hasValue());
    }

    @Test
    public void handlesVeryLongStrings() {
        String longString = "a".repeat(10000);
        CheckedString cs = new CheckedString(longString);

        assertEquals(longString, cs.string);
        assertTrue(cs.hasValue());
    }

    @Test
    public void handlesNewlinesAndTabs() {
        CheckedString cs = new CheckedString("line1\nline2\tcolumn");

        assertEquals("line1\nline2\tcolumn", cs.string);
        assertTrue(cs.hasValue());
    }

    // Real-world use cases based on actual app usage

    @Test
    public void directoryUri_withFriendlyName() {
        // Simulates SAF directory URI with friendly display name
        String uri = "content://com.android.externalstorage.documents/tree/primary:Download";
        String displayName = "Downloads";

        CheckedString dir = new CheckedString(true, uri, displayName);

        assertTrue(dir.checked);
        assertEquals(uri, dir.string);
        assertEquals("Downloads", dir.getDisplayName());
        assertTrue(dir.hasValue());
    }

    @Test
    public void fileExtension_checked() {
        CheckedString ext = new CheckedString(true, "txt");

        assertTrue(ext.checked);
        assertEquals("txt", ext.string);
        assertTrue(ext.hasValue());
    }

    @Test
    public void fileExtension_unchecked() {
        CheckedString ext = new CheckedString(false, "java");

        assertFalse(ext.checked);
        assertEquals("java", ext.string);
        assertTrue(ext.hasValue());
    }

    @Test
    public void wildcardExtension_noExtension() {
        // Special case: "*" means files with no extension
        CheckedString ext = new CheckedString("*");

        assertTrue(ext.checked);
        assertEquals("*", ext.string);
        assertTrue(ext.hasValue());
    }

    @Test
    public void legacyDirectory_noUri() {
        // Legacy directories have no URI (null), only display name
        CheckedString dir = new CheckedString(true, null, "/storage/emulated/0/Download");

        assertTrue(dir.checked);
        assertEquals(null, dir.string);
        assertEquals("/storage/emulated/0/Download", dir.getDisplayName());
        assertFalse(dir.hasValue()); // No URI means needs migration
    }

    @Test
    public void toggleCheckedState() {
        CheckedString cs = new CheckedString(true, "test");

        assertTrue(cs.checked);

        cs.checked = false;
        assertFalse(cs.checked);

        cs.checked = true;
        assertTrue(cs.checked);
    }

    @Test
    public void modifyString_afterCreation() {
        CheckedString cs = new CheckedString("original");

        cs.string = "modified";

        assertEquals("modified", cs.string);
        assertTrue(cs.hasValue());
    }

    @Test
    public void modifyString_toNull() {
        CheckedString cs = new CheckedString("original");

        cs.string = null;

        assertEquals(null, cs.string);
        assertFalse(cs.hasValue());
    }

    @Test
    public void modifyString_toEmpty() {
        CheckedString cs = new CheckedString("original");

        cs.string = "";

        assertEquals("", cs.string);
        assertFalse(cs.hasValue());
    }
}
