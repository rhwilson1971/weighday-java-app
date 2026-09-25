package xyz.drreub.weighday.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static xyz.drreub.weighday.testutil.LiveDataTestUtil.getValue;

import org.junit.Test;

import xyz.drreub.weighday.data.local.dao.WeightGoalDao;
import xyz.drreub.weighday.data.local.entity.WeightGoalEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;

import java.time.LocalDate;
import java.util.List;

public class WeightGoalDaoTest extends BaseDbTest {

    private final LocalDate d0 = LocalDate.of(2026, 1, 1);

    private WeightGoalEntity goal(double goal, double start, String user) {
        return new WeightGoalEntity(goal, start, d0, null, user);
    }

    @Test
    public void insert_returnsGeneratedIdsInIncreasingOrder() {
        WeightGoalDao dao = db.weightGoalDao();
        long a = dao.insert(goal(180, 200, USER));
        long b = dao.insert(goal(170, 190, USER));
        assertTrue(a > 0);
        assertTrue(b > a);
    }

    @Test
    public void getAllGoals_filtersByUser() {
        WeightGoalDao dao = db.weightGoalDao();
        dao.insert(goal(180, 200, USER));
        dao.insert(goal(120, 150, "other"));

        List<WeightGoalEntity> all = getValue(dao.getAllGoals(USER));
        assertEquals(1, all.size());
        assertEquals(180, all.get(0).goalWeight, 0.0);
    }

    @Test
    public void getMostRecentGoal_returnsHighestId() {
        WeightGoalDao dao = db.weightGoalDao();
        dao.insert(goal(180, 200, USER));
        dao.insert(goal(170, 190, USER));

        assertEquals(170, getValue(dao.getMostRecentGoal(USER)).goalWeight, 0.0);
    }

    @Test
    public void getMostRecentGoal_nullWhenNone() {
        assertNull(getValue(db.weightGoalDao().getMostRecentGoal(USER)));
    }

    @Test
    public void getMostRecentGoalSync_returnsHighestIdOrNull() {
        WeightGoalDao dao = db.weightGoalDao();
        assertNull(dao.getMostRecentGoalSync(USER));
        dao.insert(goal(180, 200, USER));
        dao.insert(goal(170, 190, USER));
        assertEquals(170, dao.getMostRecentGoalSync(USER).goalWeight, 0.0);
    }

    @Test
    public void insert_persistsDatesAndNullAchievedDate() {
        db.weightGoalDao().insert(goal(180, 200, USER));
        WeightGoalEntity read = db.weightGoalDao().getMostRecentGoalSync(USER);
        assertEquals(d0, read.startDate);
        assertNull(read.achievedDate);
        assertEquals(200, read.startWeight, 0.0);
    }

    @Test
    public void update_persistsAchievedDate() {
        WeightGoalDao dao = db.weightGoalDao();
        dao.insert(goal(180, 200, USER));
        WeightGoalEntity stored = dao.getMostRecentGoalSync(USER);
        stored.achievedDate = d0.plusDays(10);
        dao.update(stored);

        assertEquals(d0.plusDays(10), dao.getMostRecentGoalSync(USER).achievedDate);
    }
}
