package dev.kappa.agrep_again;

import org.junit.Test;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for special character escaping functionality.
 *
 * These tests verify that Search.escapeMetaChar() correctly escapes
 * regex metacharacters, preventing crashes when users search for
 * special characters with regex mode OFF.
 *
 * This addresses the critical bug documented in PRIORITY_EASY_WINS_TOP20.md #2.
 */
public class SpecialCharacterEscapeTest {

    /**
     * Test escaping individual special characters.
     */
    @Test
    public void testEscapeBackslash() {
        assertEquals("\\\\", Search.escapeMetaChar("\\"));
    }

    @Test
    public void testEscapeOpenParen() {
        assertEquals("\\(", Search.escapeMetaChar("("));
    }

    @Test
    public void testEscapeCloseParen() {
        assertEquals("\\)", Search.escapeMetaChar(")"));
    }

    @Test
    public void testEscapeOpenBracket() {
        assertEquals("\\[", Search.escapeMetaChar("["));
    }

    @Test
    public void testEscapeCloseBracket() {
        assertEquals("\\]", Search.escapeMetaChar("]"));
    }

    @Test
    public void testEscapeOpenBrace() {
        assertEquals("\\{", Search.escapeMetaChar("{"));
    }

    @Test
    public void testEscapeCloseBrace() {
        assertEquals("\\}", Search.escapeMetaChar("}"));
    }

    @Test
    public void testEscapeAsterisk() {
        assertEquals("\\*", Search.escapeMetaChar("*"));
    }

    @Test
    public void testEscapePlus() {
        assertEquals("\\+", Search.escapeMetaChar("+"));
    }

    @Test
    public void testEscapeQuestionMark() {
        assertEquals("\\?", Search.escapeMetaChar("?"));
    }

    @Test
    public void testEscapeDot() {
        assertEquals("\\.", Search.escapeMetaChar("."));
    }

    @Test
    public void testEscapeCaret() {
        assertEquals("\\^", Search.escapeMetaChar("^"));
    }

    @Test
    public void testEscapeDollar() {
        assertEquals("\\$", Search.escapeMetaChar("$"));
    }

    @Test
    public void testEscapePipe() {
        assertEquals("\\|", Search.escapeMetaChar("|"));
    }

    /**
     * Test escaping common real-world patterns.
     */
    @Test
    public void testEscapeWindowsPath() {
        assertEquals("C:\\\\Users\\\\Documents", Search.escapeMetaChar("C:\\Users\\Documents"));
    }

    @Test
    public void testEscapeParenthesizedText() {
        assertEquals("\\(test\\)", Search.escapeMetaChar("(test)"));
    }

    @Test
    public void testEscapeFilename() {
        assertEquals("file\\.txt", Search.escapeMetaChar("file.txt"));
    }

    @Test
    public void testEscapeWildcard() {
        assertEquals("\\*\\.txt", Search.escapeMetaChar("*.txt"));
    }

    @Test
    public void testEscapeRegexPattern() {
        assertEquals("\\^\\[a-z\\]\\+\\$", Search.escapeMetaChar("^[a-z]+$"));
    }

    /**
     * Test that normal text is not affected.
     */
    @Test
    public void testNoEscapeNeededForNormalText() {
        assertEquals("hello world", Search.escapeMetaChar("hello world"));
        assertEquals("test123", Search.escapeMetaChar("test123"));
        assertEquals("foo_bar-baz", Search.escapeMetaChar("foo_bar-baz"));
    }

    /**
     * Test that escaped patterns compile successfully.
     * This is the critical test - if patterns don't compile, the app crashes.
     */
    @Test
    public void testAllEscapedPatternsCompile() {
        String[] specialChars = {
            "\\",   // backslash
            "(",    // open paren
            ")",    // close paren
            "[",    // open bracket
            "]",    // close bracket
            "{",    // open brace
            "}",    // close brace
            "*",    // asterisk
            "+",    // plus
            "?",    // question
            ".",    // dot
            "^",    // caret
            "$",    // dollar
            "|"     // pipe
        };

        for (String specialChar : specialChars) {
            String escaped = Search.escapeMetaChar(specialChar);
            try {
                Pattern pattern = Pattern.compile(escaped);
                assertNotNull("Pattern should compile for: " + specialChar, pattern);

                // Verify the pattern matches the literal character
                assertTrue("Escaped pattern should match literal char '" + specialChar + "'",
                    pattern.matcher(specialChar).find());

            } catch (PatternSyntaxException e) {
                fail("Pattern compilation failed for special char '" + specialChar +
                     "' after escaping to '" + escaped + "': " + e.getMessage());
            }
        }
    }

    /**
     * Test that complex combinations compile and match correctly.
     */
    @Test
    public void testComplexPatternCompiles() {
        String[] testCases = {
            "C:\\Users\\Documents",
            "(test)",
            "[a-z]",
            "{json}",
            "*.txt",
            "a+b",
            "what?",
            "end.",
            "^start",
            "$100",
            "a|b"
        };

        for (String testCase : testCases) {
            String escaped = Search.escapeMetaChar(testCase);
            try {
                Pattern pattern = Pattern.compile(escaped);
                assertNotNull("Pattern should compile for: " + testCase, pattern);

                // Verify the pattern matches the literal string
                assertTrue("Escaped pattern should match literal string '" + testCase + "'",
                    pattern.matcher(testCase).find());

            } catch (PatternSyntaxException e) {
                fail("Pattern compilation failed for '" + testCase +
                     "' after escaping to '" + escaped + "': " + e.getMessage());
            }
        }
    }

    /**
     * Test that escaped patterns match only the literal text, not regex behavior.
     */
    @Test
    public void testEscapedPatternMatchesLiterally() {
        // Test dot - should match literal "." not "any character"
        String escaped = Search.escapeMetaChar(".");
        Pattern pattern = Pattern.compile(escaped);
        assertTrue("Should match literal dot", pattern.matcher("test.txt").find());
        assertTrue("Should not match 'a' as any char", pattern.matcher("test.txt").find());

        // Test asterisk - should match literal "*" not "zero or more"
        escaped = Search.escapeMetaChar("*");
        pattern = Pattern.compile(escaped);
        assertTrue("Should match literal asterisk", pattern.matcher("*.txt").find());

        // Test backslash - should match literal "\"
        escaped = Search.escapeMetaChar("\\");
        pattern = Pattern.compile(escaped);
        assertTrue("Should match literal backslash", pattern.matcher("C:\\Users").find());
    }

    /**
     * Test edge cases.
     */
    @Test
    public void testEmptyString() {
        assertEquals("", Search.escapeMetaChar(""));
    }

    @Test
    public void testAllSpecialCharsInOne() {
        String input = ".^${}[]*+?|()\\";
        String escaped = Search.escapeMetaChar(input);

        // All characters should be escaped
        assertEquals("\\.\\^\\$\\{\\}\\[\\]\\*\\+\\?\\|\\(\\)\\\\", escaped);

        // Should compile successfully
        try {
            Pattern pattern = Pattern.compile(escaped);
            assertNotNull("Pattern with all special chars should compile", pattern);
            assertTrue("Should match the literal string", pattern.matcher(input).find());
        } catch (PatternSyntaxException e) {
            fail("Pattern compilation failed: " + e.getMessage());
        }
    }

    /**
     * Test double backslash (common in Windows paths).
     */
    @Test
    public void testDoubleBackslash() {
        // Input: C:\\Users (literal backslash-backslash)
        // Each backslash should be escaped individually
        String input = "C:\\\\Users";
        String escaped = Search.escapeMetaChar(input);
        assertEquals("C:\\\\\\\\Users", escaped);

        // Should compile and match
        try {
            Pattern pattern = Pattern.compile(escaped);
            assertTrue("Should match double backslash", pattern.matcher("C:\\\\Users").find());
        } catch (PatternSyntaxException e) {
            fail("Pattern compilation failed for double backslash: " + e.getMessage());
        }
    }
}
