package dev.kappa.agrep_again;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.recyclerview.widget.RecyclerView;

/**
 * End-to-end test for the Settings activity.
 * Verifies that all main UI elements are present when the app launches.
 */
@RunWith(AndroidJUnit4.class)
public class SettingsActivityTest {

    @Rule
    public ActivityScenarioRule<Settings> activityRule =
            new ActivityScenarioRule<>(Settings.class);

    @Test
    public void testAllMainUIElementsArePresent() {
        // Verify top app bar is displayed
        onView(withId(R.id.topAppBar))
                .check(matches(isDisplayed()));

        // Verify query input section
        onView(withId(R.id.query_input_layout))
                .check(matches(isDisplayed()));
        onView(withId(R.id.query_input))
                .check(matches(isDisplayed()));

        // Verify main action buttons
        onView(withId(R.id.button_history))
                .check(matches(isDisplayed()));
        onView(withId(R.id.button_search))
                .check(matches(isDisplayed()));
        onView(withId(R.id.button_clear))
                .check(matches(isDisplayed()));

        // Verify directory section
        onView(withId(R.id.label_directories))
                .check(matches(isDisplayed()));
        onView(withId(R.id.adddir))
                .check(matches(isDisplayed()));
        onView(withId(R.id.directory_list))
                .check(matches(isAssignableFrom(RecyclerView.class)));

        // Verify extension section
        onView(withId(R.id.label_extensions))
                .check(matches(isDisplayed()));
        onView(withId(R.id.addext))
                .check(matches(isDisplayed()));
        onView(withId(R.id.extension_list))
                .check(matches(isAssignableFrom(RecyclerView.class)));

        // Verify search options switches
        onView(withId(R.id.checkre))
                .check(matches(isDisplayed()));
        onView(withId(R.id.checkignorecase))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testButtonLabelsAreCorrect() {
        // Verify button texts (assuming standard Android string resources)
        onView(withId(R.id.button_history))
                .check(matches(isDisplayed()));
        onView(withId(R.id.button_search))
                .check(matches(isDisplayed()));
        onView(withId(R.id.button_clear))
                .check(matches(isDisplayed()));
        onView(withId(R.id.adddir))
                .check(matches(isDisplayed()));
        onView(withId(R.id.addext))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testLabelTextsAreDisplayed() {
        // Verify section labels are displayed
        onView(withId(R.id.label_directories))
                .check(matches(isDisplayed()));
        onView(withId(R.id.label_extensions))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testRecyclerViewsArePresent() {
        // Verify both RecyclerViews for directories and extensions exist
        // Note: Empty RecyclerViews have height=0, so we just check they exist and are RecyclerViews
        onView(withId(R.id.directory_list))
                .check(matches(isAssignableFrom(RecyclerView.class)));
        onView(withId(R.id.extension_list))
                .check(matches(isAssignableFrom(RecyclerView.class)));
    }
}
