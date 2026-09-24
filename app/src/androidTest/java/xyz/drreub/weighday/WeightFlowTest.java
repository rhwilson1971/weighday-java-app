package xyz.drreub.weighday;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import xyz.drreub.weighday.data.local.AppDatabase;

@RunWith(AndroidJUnit4.class)
public class WeightFlowTest {

    private AppDatabase db;

    @Before
    public void useInMemoryDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        AppDatabase.setInstanceForTesting(db);
    }

    @After
    public void tearDown() {
        AppDatabase.setInstanceForTesting(null);
        db.close();
    }

    @Test
    public void setGoal_addWeight_thenSeeItInHistory() {
        try (ActivityScenario<MainActivity> ignored = ActivityScenario.launch(MainActivity.class)) {
            // Fresh install: nothing recorded, add button disabled
            onView(withText("SET GOAL")).check(matches(isDisplayed()));
            onView(withId(R.id.button_add_weight)).check(matches(not(isEnabled())));

            // Set a goal
            onView(withId(R.id.text_goal_label)).perform(click());
            onView(withId(R.id.edit_start_weight)).perform(replaceText("200"));
            onView(withId(R.id.edit_goal_weight)).perform(replaceText("180"));
            onView(withText("Save")).perform(click());

            onView(withText("GOAL")).check(matches(isDisplayed()));
            onView(withText("180.0 lbs")).check(matches(isDisplayed()));
            onView(withText("Last recorded weight: 200.0 lbs")).check(matches(isDisplayed()));

            // Add another weight (pickers are prefilled with the last weight, 200.0)
            onView(withId(R.id.button_add_weight)).perform(click());
            onView(withId(R.id.edit_note)).perform(replaceText("second entry"));
            onView(withId(R.id.button_save)).perform(click());

            // History shows both entries
            openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
            onView(withText("History")).perform(click());
            onView(withId(R.id.recycler_history)).check(matches(isDisplayed()));
            onView(withText("200.0 lbs")).check(matches(isDisplayed()));
        }
    }
}
