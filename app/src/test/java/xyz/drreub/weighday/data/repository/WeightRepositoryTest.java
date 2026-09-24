package xyz.drreub.weighday.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static xyz.drreub.weighday.testutil.LiveDataTestUtil.getValue;

import org.junit.Before;
import org.junit.Test;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.data.local.entity.WeightGoalEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class WeightRepositoryTest extends BaseDbTest {

    private WeightRepository repo;

    @Before
    public void createRepo() {
        repo = new WeightRepository(db.weightEntryDao(), db.weightGoalDao(), Runnable::run);
    }

    private WeightGoalEntity goal(double goal, double start) {
        return new WeightGoalEntity(goal, start, LocalDate.of(2026, 1, 1), null, USER);
    }

    @Test
    public void insertEntry_thenGetAllAndMostRecent() {
        repo.insert(new WeightEntryEntity(200, "a", LocalDateTime.of(2026, 1, 1, 8, 0), USER, null));
        repo.insert(new WeightEntryEntity(199, "b", LocalDateTime.of(2026, 1, 2, 8, 0), USER, null));

        assertEquals(2, getValue(repo.getAllEntries(USER)).size());
        assertEquals(199, getValue(repo.getMostRecentEntry(USER)).weight, 0.0);
    }

    @Test
    public void insertGoal_thenGetAllAndMostRecent() {
        repo.insert(goal(180, 200));
        repo.insert(goal(170, 190));

        assertEquals(2, getValue(repo.getAllGoals(USER)).size());
        assertEquals(170, getValue(repo.getMostRecentGoal(USER)).goalWeight, 0.0);
    }

    @Test
    public void insertWeightGoal_invokesCallbackWithGeneratedId() {
        AtomicLong received = new AtomicLong(-1);
        repo.insertWeightGoal(goal(180, 200), received::set);

        assertTrue(received.get() > 0);
        assertEquals(received.get(), getValue(repo.getMostRecentGoal(USER)).id);
    }

    @Test
    public void update_persistsChange() {
        repo.insert(goal(180, 200));
        WeightGoalEntity stored = getValue(repo.getMostRecentGoal(USER));
        stored.achievedDate = LocalDate.of(2026, 2, 1);
        repo.update(stored);

        assertEquals(LocalDate.of(2026, 2, 1), getValue(repo.getMostRecentGoal(USER)).achievedDate);
    }

    @Test
    public void saveWeightEntryWithGoal_linksMostRecentGoal() {
        repo.insert(goal(180, 200));
        repo.insert(goal(170, 190));
        int latestGoalId = getValue(repo.getMostRecentGoal(USER)).id;

        repo.saveWeightEntryWithGoal(185.5, "note", USER);

        WeightEntryEntity saved = getValue(repo.getMostRecentEntry(USER));
        assertEquals(185.5, saved.weight, 0.0);
        assertEquals("note", saved.note);
        assertEquals(USER, saved.userId);
        assertEquals(Integer.valueOf(latestGoalId), saved.goalId);
        assertNotNull(saved.date);
    }

    @Test
    public void saveWeightEntryWithGoal_goalIdNullWhenNoGoal() {
        repo.saveWeightEntryWithGoal(185.5, "", USER);
        assertNull(getValue(repo.getMostRecentEntry(USER)).goalId);
    }
}
