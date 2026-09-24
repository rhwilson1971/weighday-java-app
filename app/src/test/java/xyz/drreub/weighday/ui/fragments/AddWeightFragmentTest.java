package xyz.drreub.weighday.ui.fragments;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;
import static xyz.drreub.weighday.testutil.LiveDataTestUtil.getValue;

import android.os.Looper;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;

import xyz.drreub.weighday.R;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.data.local.entity.WeightGoalEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;
import xyz.drreub.weighday.testutil.WriteExecutorUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AddWeightFragmentTest extends BaseDbTest {

    private TestNavHostController nav;
    private FragmentScenario<AddWeightFragment> scenario;

    @Before
    public void setUpNav() {
        nav = new TestNavHostController(ApplicationProvider.getApplicationContext());
    }

    private void launch() {
        scenario = FragmentScenario.launchInContainer(AddWeightFragment.class, null,
                R.style.Theme_Weighday, (FragmentFactory) null);
        scenario.onFragment(f -> {
            nav.setGraph(R.navigation.nav_graph);
            nav.setCurrentDestination(R.id.AddWeightFragment);
            Navigation.setViewNavController(f.requireView(), nav);
        });
        shadowOf(Looper.getMainLooper()).idle();
    }

    private void seedEntry(double weight) {
        db.weightEntryDao().insert(new WeightEntryEntity(weight, "", LocalDateTime.now(), USER, null));
    }

    private int whole() { return picker(R.id.picker_weight_whole).getValue(); }
    private int decimal() { return picker(R.id.picker_weight_decimal).getValue(); }

    private NumberPicker picker(int id) {
        final NumberPicker[] p = new NumberPicker[1];
        scenario.onFragment(f -> p[0] = f.requireView().findViewById(id));
        return p[0];
    }

    @Test
    public void noPreviousEntry_pickersUseDefaultsAndRanges() {
        launch();
        assertEquals(150, whole());
        assertEquals(0, decimal());
        assertEquals(500, picker(R.id.picker_weight_whole).getMaxValue());
        assertEquals(9, picker(R.id.picker_weight_decimal).getMaxValue());
    }

    @Test
    public void previousEntry_prefillsPickers() {
        seedEntry(182.4);
        launch();
        assertEquals(182, whole());
        assertEquals(4, decimal());
    }

    @Test
    public void previousEntryNearNextWhole_roundsUpInsteadOfClamping() {
        seedEntry(72.96); // bug #2: decimal used to become 10 and clamp to 9 -> 72.9
        launch();
        assertEquals(73, whole());
        assertEquals(0, decimal());
    }

    @Test
    public void save_storesEntryWithPickerValueAndNote_thenPopsBackStack() throws Exception {
        db.weightGoalDao().insert(new WeightGoalEntity(180, 200, LocalDate.now(), null, USER));
        launch();
        scenario.onFragment(f -> {
            ((NumberPicker) f.requireView().findViewById(R.id.picker_weight_whole)).setValue(187);
            ((NumberPicker) f.requireView().findViewById(R.id.picker_weight_decimal)).setValue(3);
            ((EditText) f.requireView().findViewById(R.id.edit_note)).setText("after run");
            f.requireView().findViewById(R.id.button_save).performClick();
        });
        WriteExecutorUtil.flush();

        WeightEntryEntity saved = getValue(db.weightEntryDao().getMostRecentEntry(USER));
        assertEquals(187.3, saved.weight, 0.0001);
        assertEquals("after run", saved.note);
        assertEquals(Integer.valueOf(db.weightGoalDao().getMostRecentGoalSync(USER).id), saved.goalId);
    }
}
