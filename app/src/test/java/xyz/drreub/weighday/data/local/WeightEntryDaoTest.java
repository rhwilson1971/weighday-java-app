package xyz.drreub.weighday.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static xyz.drreub.weighday.testutil.LiveDataTestUtil.getValue;

import org.junit.Test;

import xyz.drreub.weighday.data.local.dao.WeightEntryDao;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;

import java.time.LocalDateTime;
import java.util.List;

public class WeightEntryDaoTest extends BaseDbTest {

    private final LocalDateTime t0 = LocalDateTime.of(2026, 1, 1, 8, 0);

    private WeightEntryEntity entry(double w, LocalDateTime d, String user) {
        return new WeightEntryEntity(w, "n" + w, d, user, null);
    }

    @Test
    public void getAllEntries_emptyByDefault() {
        assertEquals(0, getValue(db.weightEntryDao().getAllEntries(USER)).size());
    }

    @Test
    public void getAllEntries_ordersNewestFirst() {
        WeightEntryDao dao = db.weightEntryDao();
        dao.insert(entry(200, t0, USER));
        dao.insert(entry(198, t0.plusDays(2), USER));
        dao.insert(entry(199, t0.plusDays(1), USER));

        List<WeightEntryEntity> all = getValue(dao.getAllEntries(USER));
        assertEquals(3, all.size());
        assertEquals(198, all.get(0).weight, 0.0);
        assertEquals(199, all.get(1).weight, 0.0);
        assertEquals(200, all.get(2).weight, 0.0);
    }

    @Test
    public void getAllEntries_onlyReturnsRequestedUser() {
        WeightEntryDao dao = db.weightEntryDao();
        dao.insert(entry(200, t0, USER));
        dao.insert(entry(150, t0, "someoneElse"));

        List<WeightEntryEntity> all = getValue(dao.getAllEntries(USER));
        assertEquals(1, all.size());
        assertEquals(200, all.get(0).weight, 0.0);
    }

    @Test
    public void getMostRecentEntry_returnsLatestByDate() {
        WeightEntryDao dao = db.weightEntryDao();
        dao.insert(entry(200, t0, USER));
        dao.insert(entry(195, t0.plusDays(5), USER));
        dao.insert(entry(199, t0.plusDays(1), USER));

        assertEquals(195, getValue(dao.getMostRecentEntry(USER)).weight, 0.0);
    }

    @Test
    public void getMostRecentEntry_nullWhenNone() {
        assertNull(getValue(db.weightEntryDao().getMostRecentEntry(USER)));
    }

    @Test
    public void insert_persistsAllFieldsIncludingConvertedDate() {
        WeightEntryEntity e = new WeightEntryEntity(180.5, "hello", t0, USER, 3);
        db.weightEntryDao().insert(e);

        WeightEntryEntity read = getValue(db.weightEntryDao().getMostRecentEntry(USER));
        assertEquals(180.5, read.weight, 0.0);
        assertEquals("hello", read.note);
        assertEquals(t0, read.date);
        assertEquals(USER, read.userId);
        assertEquals(Integer.valueOf(3), read.goalId);
        assertEquals(1, read.id); // auto-generated
    }

    @Test
    public void insert_sameIdReplacesRow() {
        WeightEntryDao dao = db.weightEntryDao();
        WeightEntryEntity first = entry(200, t0, USER);
        first.id = 42;
        dao.insert(first);
        WeightEntryEntity second = entry(190, t0, USER);
        second.id = 42;
        dao.insert(second);

        List<WeightEntryEntity> all = getValue(dao.getAllEntries(USER));
        assertEquals(1, all.size());
        assertEquals(190, all.get(0).weight, 0.0);
    }
}
