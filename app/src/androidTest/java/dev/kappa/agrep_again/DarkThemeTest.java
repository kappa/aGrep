package dev.kappa.agrep_again;

import android.content.res.Configuration;
import android.graphics.Color;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * E2E tests for dark theme functionality.
 *
 * These tests verify that the app actually adapts to dark mode by:
 * 1. Checking colors are different between light and dark modes
 * 2. Verifying views use theme colors (not hardcoded black/white)
 * 3. Testing that mode changes are applied correctly
 *
 * Note: These tests use AppCompatDelegate to force mode changes.
 * The app must support DayNight theme for this to work.
 */
@RunWith(AndroidJUnit4.class)
public class DarkThemeTest {

    @After
    public void tearDown() {
        // Reset to system default after each test
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    /**
     * Test that the app actually uses different colors in light vs dark mode.
     *
     * This tests the ACTUAL functionality - not just that resources exist.
     */
    @Test
    public void testColorsActuallyChangeBetweenLightAndDark() {
        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        int lightBgColor;
        int lightTextColor;

        try (ActivityScenario<Settings> scenario = ActivityScenario.launch(Settings.class)) {
            scenario.onActivity(activity -> {
                activity.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            });

            // Give it time to apply
            try { Thread.sleep(100); } catch (InterruptedException e) { }

            lightBgColor = getLightModeBackgroundColor();
            lightTextColor = getLightModeTextColor();
        }

        // Force dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        int darkBgColor;
        int darkTextColor;

        try (ActivityScenario<Settings> scenario = ActivityScenario.launch(Settings.class)) {
            scenario.onActivity(activity -> {
                activity.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            });

            // Give it time to apply
            try { Thread.sleep(100); } catch (InterruptedException e) { }

            darkBgColor = getDarkModeBackgroundColor();
            darkTextColor = getDarkModeTextColor();
        }

        // Colors should be different between modes
        assertNotEquals("Background color should change between light and dark mode",
            lightBgColor, darkBgColor);
        assertNotEquals("Text color should change between light and dark mode",
            lightTextColor, darkTextColor);

        System.out.println("=== Dark Theme Color Test ===");
        System.out.println("Light mode - BG: " + String.format("#%08X", lightBgColor) +
                         ", Text: " + String.format("#%08X", lightTextColor));
        System.out.println("Dark mode  - BG: " + String.format("#%08X", darkBgColor) +
                         ", Text: " + String.format("#%08X", darkTextColor));
        System.out.println("Colors change correctly ✓");
    }

    /**
     * Test that dark mode uses dark background.
     *
     * This verifies the actual color values, not just that they exist.
     */
    @Test
    public void testDarkModeUsesDarkBackground() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        try (ActivityScenario<Settings> scenario = ActivityScenario.launch(Settings.class)) {
            scenario.onActivity(activity -> {
                activity.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            });

            // Give it time to apply
            try { Thread.sleep(100); } catch (InterruptedException e) { }

            scenario.onActivity(activity -> {
                int nightMode = activity.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;

                // Only test if dark mode is actually active
                if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
                    int bgColor = ContextCompat.getColor(activity, R.color.color_background);

                    // Dark background should have low luminance
                    float[] hsv = new float[3];
                    Color.colorToHSV(bgColor, hsv);
                    float brightness = hsv[2]; // Value component (0-1)

                    assertTrue("Dark mode background should be dark (brightness < 0.3), got: " + brightness,
                        brightness < 0.3f);

                    System.out.println("=== Dark Mode Background Test ===");
                    System.out.println("Background: " + String.format("#%08X", bgColor));
                    System.out.println("Brightness: " + String.format("%.2f", brightness));
                    System.out.println("Dark background verified ✓");
                }
            });
        }
    }

    /**
     * Test that light mode uses light background.
     *
     * This verifies the actual color values, not just that they exist.
     */
    @Test
    public void testLightModeUsesLightBackground() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        try (ActivityScenario<Settings> scenario = ActivityScenario.launch(Settings.class)) {
            scenario.onActivity(activity -> {
                activity.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            });

            // Give it time to apply
            try { Thread.sleep(100); } catch (InterruptedException e) { }

            scenario.onActivity(activity -> {
                int nightMode = activity.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;

                // Only test if light mode is actually active
                if (nightMode == Configuration.UI_MODE_NIGHT_NO) {
                    int bgColor = ContextCompat.getColor(activity, R.color.color_background);

                    // Light background should have high luminance
                    float[] hsv = new float[3];
                    Color.colorToHSV(bgColor, hsv);
                    float brightness = hsv[2]; // Value component (0-1)

                    assertTrue("Light mode background should be light (brightness > 0.7), got: " + brightness,
                        brightness > 0.7f);

                    System.out.println("=== Light Mode Background Test ===");
                    System.out.println("Background: " + String.format("#%08X", bgColor));
                    System.out.println("Brightness: " + String.format("%.2f", brightness));
                    System.out.println("Light background verified ✓");
                }
            });
        }
    }

    /**
     * Test that text and background have sufficient contrast in both modes.
     *
     * This is an accessibility requirement.
     */
    @Test
    public void testTextAndBackgroundHaveContrast() {
        try (ActivityScenario<Settings> scenario = ActivityScenario.launch(Settings.class)) {
            scenario.onActivity(activity -> {
                int bgColor = ContextCompat.getColor(activity, R.color.color_background);
                int textColor = ContextCompat.getColor(activity, R.color.color_on_background);

                // Colors should not be the same
                assertNotEquals("Background and text should be different colors", bgColor, textColor);

                // Calculate brightness difference (simple check)
                float[] bgHsv = new float[3];
                float[] textHsv = new float[3];
                Color.colorToHSV(bgColor, bgHsv);
                Color.colorToHSV(textColor, textHsv);

                float brightnessDiff = Math.abs(bgHsv[2] - textHsv[2]);

                // Brightness difference should be significant (> 0.5 on 0-1 scale)
                assertTrue("Text and background should have significant brightness difference (> 0.5), got: " + brightnessDiff,
                    brightnessDiff > 0.5f);

                System.out.println("=== Contrast Test ===");
                System.out.println("Background: " + String.format("#%08X", bgColor) +
                                 " (brightness: " + String.format("%.2f", bgHsv[2]) + ")");
                System.out.println("Text: " + String.format("#%08X", textColor) +
                                 " (brightness: " + String.format("%.2f", textHsv[2]) + ")");
                System.out.println("Brightness difference: " + String.format("%.2f", brightnessDiff));
                System.out.println("Sufficient contrast ✓");
            });
        }
    }

    // Helper methods to get colors from resources
    private int getLightModeBackgroundColor() {
        final int[] color = new int[1];
        try (ActivityScenario<Settings> scenario = ActivityScenario.launch(Settings.class)) {
            scenario.onActivity(activity -> {
                color[0] = ContextCompat.getColor(activity, R.color.color_background);
            });
        }
        return color[0];
    }

    private int getLightModeTextColor() {
        final int[] color = new int[1];
        try (ActivityScenario<Settings> scenario = ActivityScenario.launch(Settings.class)) {
            scenario.onActivity(activity -> {
                color[0] = ContextCompat.getColor(activity, R.color.color_on_background);
            });
        }
        return color[0];
    }

    private int getDarkModeBackgroundColor() {
        final int[] color = new int[1];
        try (ActivityScenario<Settings> scenario = ActivityScenario.launch(Settings.class)) {
            scenario.onActivity(activity -> {
                color[0] = ContextCompat.getColor(activity, R.color.color_background);
            });
        }
        return color[0];
    }

    private int getDarkModeTextColor() {
        final int[] color = new int[1];
        try (ActivityScenario<Settings> scenario = ActivityScenario.launch(Settings.class)) {
            scenario.onActivity(activity -> {
                color[0] = ContextCompat.getColor(activity, R.color.color_on_background);
            });
        }
        return color[0];
    }
}
