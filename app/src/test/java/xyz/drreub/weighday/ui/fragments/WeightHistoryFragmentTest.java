package xyz.drreub.weighday.ui.fragments;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.RecyclerView;

import org.junit.Test;

import xyz.drreub.weighday.R;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;

import java.time.LocalDateTime;

public class WeightHistoryFragmentTest extends BaseDbTest {

    private int rowCount() {
        FragmentScenario<WeightHistoryFragment> scenario = FragmentScenario.launchInContainer(
                WeightHistoryFragment.class, null, R.style.Theme_Weighday, (FragmentFactory) null);
        shadowOf(Looper.getMainLooper()).idle();
        final int[] count = new int[1];
        scenario.onFragment(f -> {
            RecyclerView rv = f.requireView().findViewById(R.id.recycler_history);
            count[0] = rv.getAdapter().getItemCount();
        });
        return count[0];
    }

    @Test
    public void emptyDatabase_showsNoRows() {
        assertEquals(0, rowCount());
    }

    @Test
    public void entries_areListedInRecycler() {
        LocalDateTime t = LocalDateTime.of(2026, 1, 1, 8, 0);
        db.weightEntryDao().insert(new WeightEntryEntity(200, "", t, USER, null));
        db.weightEntryDao().insert(new WeightEntryEntity(199, "", t.plusDays(1), USER, null));
        assertEquals(2, rowCount());
    }

    @Test
    public void destroyingView_releasesBinding() {
        FragmentScenario<WeightHistoryFragment> scenario = FragmentScenario.launchInContainer(
                WeightHistoryFragment.class, null, R.style.Theme_Weighday, (FragmentFactory) null);
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.DESTROYED);
    }
}
