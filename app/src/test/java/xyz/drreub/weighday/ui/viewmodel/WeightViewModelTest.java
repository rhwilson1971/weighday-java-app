package xyz.drreub.weighday.ui.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import java.time.LocalDateTime;

public class WeightViewModelTest extends BaseDbTest {

    private WeightViewModel vm;

    @Before
    public void createViewModel() {
        vm = new WeightViewModel((Application) ApplicationProvider.getApplicationContext());
    }

    private WeightEntryEntity entryOf(double weight) {
        return new WeightEntryEntity(weight, "", LocalDateTime.now(), USER, null);
    }

    private WeightGoalEntity storedGoal(double goal, double start) {
        db.weightGoalDao().insert(new WeightGoalEntity(goal, start, LocalDate.of(2026, 1, 1), null, USER));
        return db.weightGoalDao().getMostRecentGoalSync(USER);
    }

    @Test
    public void setNewGoal_insertsGoalAndInitialEntry() throws Exception {
        vm.setNewGoal(180, 200);
        WriteExecutorUtil.flush();

        WeightGoalEntity goal = db.weightGoalDao().getMostRecentGoalSync(USER);
        assertNotNull(goal);
        assertEquals(180, goal.goalWeight, 0.0);
        assertEquals(200, goal.startWeight, 0.0);
        assertEquals(LocalDate.now(), goal.startDate);
        assertNull(goal.achievedDate);
        assertEquals(USER, goal.userId);

        WeightEntryEntity entry = getValue(db.weightEntryDao().getMostRecentEntry(USER));
        assertNotNull(entry);
        assertEquals(200, entry.weight, 0.0);
        assertEquals("Initial", entry.note);
        assertEquals(Integer.valueOf(goal.id), entry.goalId);
    }

    @Test
    public void getMostRecentEntryAndGoal_exposeRepositoryData() {
        assertNull(getValue(vm.getMostRecentEntry()));
        assertNull(getValue(vm.getMostRecentGoal()));

        db.weightEntryDao().insert(entryOf(190));
        storedGoal(180, 200);

        assertEquals(190, getValue(vm.getMostRecentEntry()).weight, 0.0);
        assertEquals(180, getValue(vm.getMostRecentGoal()).goalWeight, 0.0);
    }

    @Test
    public void checkGoalAchieved_marksGoalWhenWeightAtOrBelowTarget() throws Exception {
        WeightGoalEntity goal = storedGoal(180, 200);
        vm.checkGoalAchieved(entryOf(180), goal);
        WriteExecutorUtil.flush();

        assertEquals(LocalDate.now(), db.weightGoalDao().getMostRecentGoalSync(USER).achievedDate);
    }

    @Test
    public void checkGoalAchieved_ignoresWeightAboveTarget() throws Exception {
        WeightGoalEntity goal = storedGoal(180, 200);
        vm.checkGoalAchieved(entryOf(180.1), goal);
        WriteExecutorUtil.flush();

        assertNull(db.weightGoalDao().getMostRecentGoalSync(USER).achievedDate);
    }

    @Test
    public void checkGoalAchieved_doesNotOverwriteExistingAchievedDate() throws Exception {
        WeightGoalEntity goal = storedGoal(180, 200);
        LocalDate earlier = LocalDate.of(2026, 1, 15);
        goal.achievedDate = earlier;
        db.weightGoalDao().update(goal);

        vm.checkGoalAchieved(entryOf(170), goal);
        WriteExecutorUtil.flush();

        assertEquals(earlier, db.weightGoalDao().getMostRecentGoalSync(USER).achievedDate);
    }

    @Test
    public void checkGoalAchieved_nullArgumentsAreNoOps() throws Exception {
        WeightGoalEntity goal = storedGoal(180, 200);
        vm.checkGoalAchieved(null, goal);
        vm.checkGoalAchieved(entryOf(170), null);
        vm.checkGoalAchieved(null, null);
        WriteExecutorUtil.flush();

        assertNull(db.weightGoalDao().getMostRecentGoalSync(USER).achievedDate);
    }
}
