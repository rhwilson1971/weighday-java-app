package xyz.drreub.weighday.ui.adapters;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import xyz.drreub.weighday.R;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class WeightHistoryAdapterTest {

    private Context context;
    private FrameLayout parent;
    private WeightHistoryAdapter adapter;

    @Before
    public void setUp() {
        context = new androidx.appcompat.view.ContextThemeWrapper(
                ApplicationProvider.getApplicationContext(), R.style.Theme_Weighday);
        parent = new FrameLayout(context);
        adapter = new WeightHistoryAdapter();
    }

    private WeightEntryEntity entry(double weight, LocalDateTime date) {
        return new WeightEntryEntity(weight, "", date, "u", null);
    }

    @Test
    public void itemCount_isZeroInitially() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void setEntries_updatesItemCount() {
        adapter.setEntries(Arrays.asList(
                entry(200, LocalDateTime.of(2026, 1, 1, 8, 0)),
                entry(199, LocalDateTime.of(2026, 1, 2, 8, 0))));
        assertEquals(2, adapter.getItemCount());

        adapter.setEntries(Collections.emptyList());
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void bind_showsFormattedWeightAndDate() {
        adapter.setEntries(Collections.singletonList(entry(185.5, LocalDateTime.of(2026, 3, 5, 8, 0))));
        WeightHistoryAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        assertEquals("185.5 lbs", ((TextView) holder.itemView.findViewById(R.id.text_weight)).getText().toString());
        assertEquals("Mar 05, 2026", ((TextView) holder.itemView.findViewById(R.id.text_date)).getText().toString());
    }

    @Test
    public void bind_withNullDate_leavesDateTextUntouched() {
        adapter.setEntries(Collections.singletonList(entry(185.5, null)));
        WeightHistoryAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        TextView dateView = holder.itemView.findViewById(R.id.text_date);
        CharSequence before = dateView.getText();

        adapter.onBindViewHolder(holder, 0);

        assertEquals(before.toString(), dateView.getText().toString());
    }
}
