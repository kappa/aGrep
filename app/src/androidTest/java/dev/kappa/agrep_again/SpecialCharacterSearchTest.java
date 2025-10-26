package dev.kappa.agrep_again;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * E2E tests for special character search functionality.
 *
 * These tests verify that searching for regex metacharacters doesn't crash
 * when regex mode is OFF. They test the actual search functionality with:
 * - Backslash (\)
 * - Brackets and braces: ( ) [ ] { }
 * - Quantifiers: * + ? .
 * - Anchors and alternation: ^ $ |
 *
 * This addresses the critical bug where searching for special characters
 * causes crashes (documented in PRIORITY_EASY_WINS_TOP20.md #2).
 */
@RunWith(AndroidJUnit4.class)
public class SpecialCharacterSearchTest {

    private Context context;
    private File testDir;
    private ExecutorGrepEngine engine;

    @Before
    public void setUp() throws IOException {
        context = ApplicationProvider.getApplicationContext();
        testDir = new File(context.getFilesDir(), "special_char_test");
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        engine = new ExecutorGrepEngine(context);
    }

    @After
    public void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
        if (testDir != null && testDir.exists()) {
            deleteRecursive(testDir);
        }
    }

    /**
     * Test searching for backslash character.
     * This is the most common crash case - Windows paths like C:\Users\...
     */
    @Test
    public void testSearchForBackslash() throws Exception {
        // Create test file with backslash
        File testFile = createTestFile("backslash.txt",
            "This line has a backslash: \\\n" +
            "Windows path: C:\\Users\\Documents\n" +
            "No backslash here\n");

        // Search for backslash with regex OFF
        SearchResult result = searchForPattern("\\", false, false);

        // Should find 2 lines containing backslash
        assertEquals("Should find 2 lines with backslash", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    /**
     * Test searching for opening parenthesis.
     */
    @Test
    public void testSearchForOpenParenthesis() throws Exception {
        File testFile = createTestFile("paren.txt",
            "Function call: doSomething(param)\n" +
            "Another function: test(\n" +
            "No paren here\n");

        SearchResult result = searchForPattern("(", false, false);

        assertEquals("Should find 2 lines with open paren", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    /**
     * Test searching for closing parenthesis.
     */
    @Test
    public void testSearchForCloseParenthesis() throws Exception {
        File testFile = createTestFile("paren.txt",
            "Function call: doSomething(param)\n" +
            "Another function: test)\n" +
            "No paren here\n");

        SearchResult result = searchForPattern(")", false, false);

        assertEquals("Should find 2 lines with close paren", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    /**
     * Test searching for opening square bracket.
     */
    @Test
    public void testSearchForOpenBracket() throws Exception {
        File testFile = createTestFile("bracket.txt",
            "Array access: array[0]\n" +
            "Opening bracket: [\n" +
            "No bracket here\n");

        SearchResult result = searchForPattern("[", false, false);

        assertEquals("Should find 2 lines with open bracket", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    /**
     * Test searching for closing square bracket.
     */
    @Test
    public void testSearchForCloseBracket() throws Exception {
        File testFile = createTestFile("bracket.txt",
            "Array access: array[0]\n" +
            "Closing bracket: ]\n" +
            "No bracket here\n");

        SearchResult result = searchForPattern("]", false, false);

        assertEquals("Should find 2 lines with close bracket", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    /**
     * Test searching for opening curly brace.
     */
    @Test
    public void testSearchForOpenBrace() throws Exception {
        File testFile = createTestFile("brace.txt",
            "Code block: if (true) {\n" +
            "Opening brace: {\n" +
            "No brace here\n");

        SearchResult result = searchForPattern("{", false, false);

        assertEquals("Should find 2 lines with open brace", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    /**
     * Test searching for closing curly brace.
     */
    @Test
    public void testSearchForCloseBrace() throws Exception {
        File testFile = createTestFile("brace.txt",
            "Code block end: }\n" +
            "Closing brace: }\n" +
            "No brace here\n");

        SearchResult result = searchForPattern("}", false, false);

        assertEquals("Should find 2 lines with close brace", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    /**
     * Test searching for asterisk (star).
     */
    @Test
    public void testSearchForAsterisk() throws Exception {
        File testFile = createTestFile("asterisk.txt",
            "Wildcard: *.txt\n" +
            "Multiply: 5 * 3\n" +
            "No asterisk here\n");

        SearchResult result = searchForPattern("*", false, false);

        assertEquals("Should find 2 lines with asterisk", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    /**
     * Test searching for plus sign.
     */
    @Test
    public void testSearchForPlus() throws Exception {
        File testFile = createTestFile("plus.txt",
            "Addition: 5 + 3\n" +
            "Concatenate: str1 + str2\n" +
            "No plus here\n");

        SearchResult result = searchForPattern("+", false, false);

        assertEquals("Should find 2 lines with plus", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    /**
     * Test searching for question mark.
     */
    @Test
    public void testSearchForQuestionMark() throws Exception {
        File testFile = createTestFile("question.txt",
            "Is this true?\n" +
            "What?\n" +
            "No question mark here\n");

        SearchResult result = searchForPattern("?", false, false);

        assertEquals("Should find 2 lines with question mark", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    /**
     * Test searching for dot/period.
     */
    @Test
    public void testSearchForDot() throws Exception {
        File testFile = createTestFile("dot.txt",
            "End of sentence.\n" +
            "File extension: .txt\n" +
            "No dot here\n");

        SearchResult result = searchForPattern(".", false, false);

        assertEquals("Should find 2 lines with dot", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    /**
     * Test searching for caret (^).
     */
    @Test
    public void testSearchForCaret() throws Exception {
        File testFile = createTestFile("caret.txt",
            "Power: 2^8\n" +
            "XOR: a ^ b\n" +
            "No caret here\n");

        SearchResult result = searchForPattern("^", false, false);

        assertEquals("Should find 2 lines with caret", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    /**
     * Test searching for dollar sign.
     */
    @Test
    public void testSearchForDollarSign() throws Exception {
        File testFile = createTestFile("dollar.txt",
            "Price: $50\n" +
            "Variable: $myVar\n" +
            "No dollar here\n");

        SearchResult result = searchForPattern("$", false, false);

        assertEquals("Should find 2 lines with dollar sign", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    /**
     * Test searching for pipe (|).
     */
    @Test
    public void testSearchForPipe() throws Exception {
        File testFile = createTestFile("pipe.txt",
            "OR operator: a | b\n" +
            "Pipeline: cmd1 | cmd2\n" +
            "No pipe here\n");

        SearchResult result = searchForPattern("|", false, false);

        assertEquals("Should find 2 lines with pipe", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    /**
     * Test that escapeMetaChar function works correctly.
     */
    @Test
    public void testEscapeMetaCharFunction() {
        // Test individual characters
        assertEquals("\\\\", Search.escapeMetaChar("\\"));
        assertEquals("\\(", Search.escapeMetaChar("("));
        assertEquals("\\)", Search.escapeMetaChar(")"));
        assertEquals("\\[", Search.escapeMetaChar("["));
        assertEquals("\\]", Search.escapeMetaChar("]"));
        assertEquals("\\{", Search.escapeMetaChar("{"));
        assertEquals("\\}", Search.escapeMetaChar("}"));
        assertEquals("\\*", Search.escapeMetaChar("*"));
        assertEquals("\\+", Search.escapeMetaChar("+"));
        assertEquals("\\?", Search.escapeMetaChar("?"));
        assertEquals("\\.", Search.escapeMetaChar("."));
        assertEquals("\\^", Search.escapeMetaChar("^"));
        assertEquals("\\$", Search.escapeMetaChar("$"));
        assertEquals("\\|", Search.escapeMetaChar("|"));

        // Test combinations
        assertEquals("C:\\\\Users\\\\Documents", Search.escapeMetaChar("C:\\Users\\Documents"));
        assertEquals("\\(test\\)", Search.escapeMetaChar("(test)"));
        assertEquals("file\\.txt", Search.escapeMetaChar("file.txt"));
    }

    /**
     * Test that escaped patterns compile successfully.
     */
    @Test
    public void testEscapedPatternsCompile() {
        String[] specialChars = {"\\", "(", ")", "[", "]", "{", "}", "*", "+", "?", ".", "^", "$", "|"};

        for (String specialChar : specialChars) {
            String escaped = Search.escapeMetaChar(specialChar);
            try {
                Pattern pattern = Pattern.compile(escaped);
                assertNotNull("Pattern should compile for: " + specialChar, pattern);
            } catch (PatternSyntaxException e) {
                fail("Pattern compilation failed for special char '" + specialChar +
                     "' after escaping to '" + escaped + "': " + e.getMessage());
            }
        }
    }

    /**
     * Test that regex mode still works when enabled.
     */
    @Test
    public void testRegexModeStillWorks() throws Exception {
        File testFile = createTestFile("regex.txt",
            "test123\n" +
            "test456\n" +
            "nodigits\n");

        // Search with regex enabled - should work as regex
        SearchResult result = searchForPattern("test\\d+", true, false);

        assertEquals("Should find 2 lines matching regex", 2, result.matches.size());
        assertFalse("Search should not error", result.hadError);
    }

    // Helper methods

    private File createTestFile(String filename, String content) throws IOException {
        File file = new File(testDir, filename);
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
        return file;
    }

    private SearchResult searchForPattern(String query, boolean useRegex, boolean ignoreCase)
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        SearchResult result = new SearchResult();

        // Create preferences
        Prefs prefs = new Prefs();
        prefs.mRegularExpression = useRegex;
        prefs.mIgnoreCase = ignoreCase;

        // Add test directory
        CheckedString dir = new CheckedString(testDir.getAbsolutePath(), true);
        dir.checked = true;
        prefs.mDirList = new ArrayList<>();
        prefs.mDirList.add(dir);

        // Add all extensions
        CheckedString allExt = new CheckedString("txt", true);
        allExt.checked = true;
        prefs.mExtList = new ArrayList<>();
        prefs.mExtList.add(allExt);

        // Prepare pattern
        String patternText = query;
        if (!useRegex) {
            patternText = Search.escapeMetaChar(patternText);
        }

        Pattern pattern;
        try {
            if (ignoreCase) {
                pattern = Pattern.compile(patternText, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
            } else {
                pattern = Pattern.compile(patternText);
            }
        } catch (PatternSyntaxException e) {
            result.hadError = true;
            result.errorMessage = "Pattern compilation failed: " + e.getMessage();
            return result;
        }

        SearchRequest request = new SearchRequest(query, pattern, prefs);

        // Execute search
        engine.search(request, new GrepEngine.ProgressCallback() {
            @Override
            public void onProgress(SearchProgress progress) {
                result.matches.addAll(progress.currentMatches);
            }

            @Override
            public void onComplete(SearchSummary summary) {
                result.matches.addAll(summary.allMatches);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                result.hadError = true;
                result.errorMessage = error;
                latch.countDown();
            }
        });

        assertTrue("Search should complete within 5 seconds", latch.await(5, TimeUnit.SECONDS));
        return result;
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }

    private static class SearchResult {
        List<GrepView.Data> matches = new ArrayList<>();
        boolean hadError = false;
        String errorMessage = null;
    }
}
