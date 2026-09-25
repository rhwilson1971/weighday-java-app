package xyz.drreub.weighday.ui.viewmodel;

import static org.junit.Assert.assertEquals;
import static xyz.drreub.weighday.testutil.LiveDataTestUtil.getValue;

import android.app.Application;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;

import java.time.LocalDateTime;
import java.util.List;

public class WeightHistoryViewModelTest extends BaseDbTest {

    @Test
    public void getAllEntries_returnsNewestFirst() {
        WeightHistoryViewModel vm =
                new WeightHistoryViewModel((Application) ApplicationProvider.getApplicationContext());
        LocalDateTime t = LocalDateTime.of(2026, 1, 1, 8, 0);
        db.weightEntryDao().insert(new WeightEntryEntity(200, "", t, USER, null));
        db.weightEntryDao().insert(new WeightEntryEntity(199, "", t.plusDays(1), USER, null));

        List<WeightEntryEntity> all = getValue(vm.getAllEntries());
        assertEquals(2, all.size());
        assertEquals(199, all.get(0).weight, 0.0);
    }
}
