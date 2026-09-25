package xyz.drreub.weighday.ui.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static xyz.drreub.weighday.testutil.LiveDataTestUtil.getValue;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.data.local.entity.WeightGoalEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;
import xyz.drreub.weighday.testutil.WriteExecutorUtil;

import java.time.LocalDate;

public class AddWeightViewModelTest extends BaseDbTest {

    private AddWeightViewModel vm;

    @Before
    public void createViewModel() {
        vm = new AddWeightViewModel((Application) ApplicationProvider.getApplicationContext());
    }

    @Test
    public void saveWeight_storesEntryLinkedToCurrentGoal() throws Exception {
        db.weightGoalDao().insert(new WeightGoalEntity(180, 200, LocalDate.now(), null, USER));
        int goalId = db.weightGoalDao().getMostRecentGoalSync(USER).id;

        vm.saveWeight(195.5, "felt good");
        WriteExecutorUtil.flush();

        WeightEntryEntity saved = getValue(vm.getLastWeight());
        assertEquals(195.5, saved.weight, 0.0);
        assertEquals("felt good", saved.note);
        assertEquals(Integer.valueOf(goalId), saved.goalId);
    }

    @Test
    public void getLastWeight_nullWhenNoEntries() {
        assertNull(getValue(vm.getLastWeight()));
    }
}
