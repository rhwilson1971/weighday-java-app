package xyz.drreub.weighday.ui.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlertDialog;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowAlertDialog;

import xyz.drreub.weighday.R;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.data.local.entity.WeightGoalEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;
import xyz.drreub.weighday.testutil.WriteExecutorUtil;
import xyz.drreub.weighday.ui.components.SegmentedProgressView;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WeightTrackerFragmentTest extends BaseDbTest {

    private TestNavHostController nav;
    private FragmentScenario<WeightTrackerFragment> scenario;

    @Before
    public void setUpNav() {
        nav = new TestNavHostController(ApplicationProvider.getApplicationContext());
    }

    private void seedGoal(double goal, double start) {
        db.weightGoalDao().insert(new WeightGoalEntity(goal, start, LocalDate.now(), null, USER));
    }

    private void seedEntry(double weight) {
        db.weightEntryDao().insert(new WeightEntryEntity(weight, "", LocalDateTime.now(), USER, null));
    }

    private void launch() {
        scenario = FragmentScenario.launchInContainer(WeightTrackerFragment.class, null,
                R.style.Theme_Weighday, (androidx.fragment.app.FragmentFactory) null);
        scenario.onFragment(f -> {
            nav.setGraph(R.navigation.nav_graph);
            Navigation.setViewNavController(f.requireView(), nav);
        });
        shadowOf(Looper.getMainLooper()).idle();
    }

    private <T extends View> T find(int id) {
        final Object[] holder = new Object[1];
        scenario.onFragment(f -> holder[0] = f.requireView().findViewById(id));
        //noinspection unchecked
        return (T) holder[0];
    }

    @Test
    public void noGoalNoEntry_showsSetGoalPromptAndDisablesAdd() {
        launch();
        assertEquals("SET GOAL", ((TextView) find(R.id.text_goal_label)).getText().toString());
        assertEquals(View.GONE, find(R.id.text_goal_weight).getVisibility());
        assertEquals("No weight recorded yet", ((TextView) find(R.id.text_last_weight)).getText().toString());
        assertFalse(find(R.id.button_add_weight).isEnabled());
        assertEquals(0f, ((SegmentedProgressView) find(R.id.progress_view)).getProgress(), 0f);
    }

    @Test
    public void goalAndEntry_showGoalLastWeightAndProgress() {
        seedGoal(180, 200);
        seedEntry(190);
        launch();

        assertEquals("GOAL", ((TextView) find(R.id.text_goal_label)).getText().toString());
        assertEquals("180.0 lbs", ((TextView) find(R.id.text_goal_weight)).getText().toString());
        assertEquals(View.VISIBLE, find(R.id.text_goal_weight).getVisibility());
        assertEquals("Last recorded weight: 190.0 lbs",
                ((TextView) find(R.id.text_last_weight)).getText().toString());
        assertTrue(find(R.id.button_add_weight).isEnabled());
        assertEquals(0.5f, ((SegmentedProgressView) find(R.id.progress_view)).getProgress(), 0.001f);
    }

    @Test
    public void goalWithoutEntry_progressIsZero() {
        seedGoal(180, 200);
        launch();
        assertEquals(0f, ((SegmentedProgressView) find(R.id.progress_view)).getProgress(), 0f);
    }

    @Test
    public void startEqualsGoal_progressIsZeroNotNaN() {
        seedGoal(180, 180);
        seedEntry(180);
        launch();
        assertEquals(0f, ((SegmentedProgressView) find(R.id.progress_view)).getProgress(), 0f);
    }

    @Test
    public void weightAboveStart_progressClampedToZero() {
        seedGoal(180, 200);
        seedEntry(210);
        launch();
        assertEquals(0f, ((SegmentedProgressView) find(R.id.progress_view)).getProgress(), 0f);
    }

    @Test
    public void reachingGoal_marksAchievedAndFillsProgress() throws Exception {
        seedGoal(180, 200);
        seedEntry(179);
        launch();
        WriteExecutorUtil.flush();

        assertEquals(1f, ((SegmentedProgressView) find(R.id.progress_view)).getProgress(), 0f);
        assertNotNull(db.weightGoalDao().getMostRecentGoalSync(USER).achievedDate);
    }

    @Test
    public void addWeightButton_navigatesToAddWeight() {
        seedGoal(180, 200);
        launch();
        find(R.id.button_add_weight).performClick();
        assertEquals(R.id.AddWeightFragment, nav.getCurrentDestination().getId());
    }

    @Test
    public void historyMenuItem_navigatesToHistory() {
        launch();
        scenario.onFragment(f -> {
            android.app.Activity activity = f.requireActivity();
            activity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, new RoboMenuItem(R.id.action_settings));
        });
        assertEquals(R.id.WeightHistoryFragment, nav.getCurrentDestination().getId());
    }

    @Test
    public void menu_relabelsSettingsItemAsHistory() {
        launch();
        final RoboMenu[] menu = new RoboMenu[1];
        scenario.onFragment(f -> {
            menu[0] = new RoboMenu(f.requireContext());
            f.requireActivity().onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, menu[0]);
        });
        assertEquals("History", menu[0].findItem(R.id.action_settings).getTitle().toString());
    }

    @Test
    public void unknownMenuItem_isNotHandled() {
        launch();
        scenario.onFragment(f -> {
            boolean handled = f.requireActivity()
                    .onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, new RoboMenuItem(0));
            assertFalse(handled);
        });
        assertEquals(R.id.WeightTrackerFragment, nav.getCurrentDestination().getId());
    }

    @Test
    public void tappingGoalLabel_opensDialogPrefilledFromEntryAndGoal() {
        seedGoal(180, 200);
        seedEntry(190);
        launch();
        find(R.id.text_goal_label).performClick();

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertTrue(dialog.isShowing());
        assertEquals("190.0", ((EditText) dialog.findViewById(R.id.edit_start_weight)).getText().toString());
        assertEquals("180.0", ((EditText) dialog.findViewById(R.id.edit_goal_weight)).getText().toString());
    }

    @Test
    public void dialogPrefill_usesGoalStartWeightWhenNoEntry() {
        seedGoal(180, 200);
        launch();
        find(R.id.text_goal_weight).performClick();

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertEquals("200.0", ((EditText) dialog.findViewById(R.id.edit_start_weight)).getText().toString());
    }

    @Test
    public void dialogPrefill_blankWhenNothingExists() {
        launch();
        find(R.id.progress_view).performClick();

        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertEquals("", ((EditText) dialog.findViewById(R.id.edit_start_weight)).getText().toString());
        assertEquals("", ((EditText) dialog.findViewById(R.id.edit_goal_weight)).getText().toString());
    }

    @Test
    public void dialogSave_createsGoalAndInitialEntry() throws Exception {
        launch();
        find(R.id.text_goal_label).performClick();
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        ((EditText) dialog.findViewById(R.id.edit_start_weight)).setText("210.5");
        ((EditText) dialog.findViewById(R.id.edit_goal_weight)).setText("185");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        WriteExecutorUtil.flush();
        shadowOf(Looper.getMainLooper()).idle();
        WriteExecutorUtil.flush();
        shadowOf(Looper.getMainLooper()).idle();

        WeightGoalEntity goal = db.weightGoalDao().getMostRecentGoalSync(USER);
        assertEquals(185, goal.goalWeight, 0.0);
        assertEquals(210.5, goal.startWeight, 0.0);
    }

    @Test
    public void dialogSave_withBlankFieldsDoesNothing() throws Exception {
        launch();
        find(R.id.text_goal_label).performClick();
        ShadowAlertDialog.getLatestAlertDialog().getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        WriteExecutorUtil.flush();

        assertEquals(null, db.weightGoalDao().getMostRecentGoalSync(USER));
    }

    @Test
    public void dialogSave_withNonNumericInputDoesNothing() throws Exception {
        launch();
        find(R.id.text_goal_label).performClick();
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        ((EditText) dialog.findViewById(R.id.edit_start_weight)).setText("abc");
        ((EditText) dialog.findViewById(R.id.edit_goal_weight)).setText("1.2.3");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        WriteExecutorUtil.flush();

        assertEquals(null, db.weightGoalDao().getMostRecentGoalSync(USER));
    }

    @Test
    public void dialogCancel_dismissesWithoutSaving() throws Exception {
        launch();
        find(R.id.text_goal_label).performClick();
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();
        shadowOf(Looper.getMainLooper()).idle();
        WriteExecutorUtil.flush();

        assertFalse(dialog.isShowing());
        assertEquals(null, db.weightGoalDao().getMostRecentGoalSync(USER));
    }
}
