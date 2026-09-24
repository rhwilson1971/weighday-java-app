package xyz.drreub.weighday;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.core.app.ActivityScenario;

import org.junit.Test;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.fakes.RoboMenuItem;

import xyz.drreub.weighday.testutil.BaseDbTest;

public class MainActivityTest extends BaseDbTest {

    @Test
    public void launch_setsUpToolbarAndStartsAtTracker() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.getSupportActionBar());
                NavController nav = Navigation.findNavController(activity, R.id.nav_host_fragment_content_main);
                assertEquals(R.id.WeightTrackerFragment, nav.getCurrentDestination().getId());
            });
        }
    }

    @Test
    public void onCreateOptionsMenu_inflatesMenu() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                RoboMenu menu = new RoboMenu(activity);
                assertTrue(activity.onCreateOptionsMenu(menu));
                assertNotNull(menu.findItem(R.id.action_settings));
            });
        }
    }

    @Test
    public void onOptionsItemSelected_settingsIsConsumed_othersFallThrough() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertTrue(activity.onOptionsItemSelected(new RoboMenuItem(R.id.action_settings)));
                assertFalse(activity.onOptionsItemSelected(new RoboMenuItem(0)));
            });
        }
    }

    @Test
    public void onSupportNavigateUp_atStartDestination_returnsFalse_andWorksAfterNavigating() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertFalse(activity.onSupportNavigateUp());

                NavController nav = Navigation.findNavController(activity, R.id.nav_host_fragment_content_main);
                nav.navigate(R.id.action_Tracker_to_History);
                assertEquals(R.id.WeightHistoryFragment, nav.getCurrentDestination().getId());

                assertTrue(activity.onSupportNavigateUp());
                assertEquals(R.id.WeightTrackerFragment, nav.getCurrentDestination().getId());
            });
        }
    }
}
