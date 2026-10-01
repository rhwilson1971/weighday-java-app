package xyz.drreub.weighday.ui.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import android.widget.ImageView;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.RecyclerView;

import org.junit.Test;

import xyz.drreub.weighday.R;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.testutil.BaseDbTest;

import java.time.LocalDateTime;

public class WeightHistoryFragmentTest extends BaseDbTest {

    /**
     * Launches the history screen and reads its row count after pending main-thread work.
     *
     * @return the number of items reported by the history adapter
     */
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

    /**
     * Verifies that the history screen has no rows when the database is empty.
     */
    @Test
    public void emptyDatabase_showsNoRows() {
        assertEquals(0, rowCount());
    }

    /**
     * Verifies that stored weigh-ins appear with a decrease indicator on the newest row.
     */
    @Test
    public void entries_areListedInRecyclerWithDirectionIndicators() {
        LocalDateTime t = LocalDateTime.of(2026, 1, 1, 8, 0);
        db.weightEntryDao().insert(new WeightEntryEntity(200, "", t, USER, null));
        db.weightEntryDao().insert(new WeightEntryEntity(199, "", t.plusDays(1), USER, null));
        assertEquals(2, rowCount());

        FragmentScenario<WeightHistoryFragment> scenario = FragmentScenario.launchInContainer(
                WeightHistoryFragment.class, null, R.style.Theme_Weighday, (FragmentFactory) null);
        shadowOf(Looper.getMainLooper()).idle();
        scenario.onFragment(f -> {
            RecyclerView rv = f.requireView().findViewById(R.id.recycler_history);
            RecyclerView.ViewHolder holder = rv.findViewHolderForAdapterPosition(0);
            assertNotNull(holder);
            ImageView iv = holder.itemView.findViewById(R.id.image_direction);
            assertNotNull(iv);
            assertNotNull(iv.getDrawable());
            assertEquals("Weight decreased", iv.getContentDescription().toString());
        });
    }

    /**
     * Exercises destruction of the history view to check that lifecycle cleanup completes.
     */
    @Test
    public void destroyingView_releasesBinding() {
        FragmentScenario<WeightHistoryFragment> scenario = FragmentScenario.launchInContainer(
                WeightHistoryFragment.class, null, R.style.Theme_Weighday, (FragmentFactory) null);
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.DESTROYED);
    }
}
