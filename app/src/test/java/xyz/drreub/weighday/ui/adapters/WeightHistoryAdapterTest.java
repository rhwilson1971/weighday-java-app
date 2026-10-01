package xyz.drreub.weighday.ui.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import xyz.drreub.weighday.R;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.domain.model.WeightDirection;
import xyz.drreub.weighday.domain.model.WeightHistoryItem;

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
    public void setHistoryItems_updatesItemCount() {
        WeightHistoryItem item1 = new WeightHistoryItem(entry(200, LocalDateTime.of(2026, 1, 1, 8, 0)), WeightDirection.UP);
        WeightHistoryItem item2 = new WeightHistoryItem(entry(199, LocalDateTime.of(2026, 1, 2, 8, 0)), WeightDirection.NONE);

        adapter.setHistoryItems(Arrays.asList(item1, item2));
        assertEquals(2, adapter.getItemCount());
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

    @Test
    public void bind_directionUp_setsUpIconAndDescription() {
        WeightHistoryItem item = new WeightHistoryItem(entry(185.5, LocalDateTime.of(2026, 3, 5, 8, 0)), WeightDirection.UP);
        adapter.setHistoryItems(Collections.singletonList(item));

        WeightHistoryAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        ImageView imageView = holder.itemView.findViewById(R.id.image_direction);
        assertNotNull(imageView.getDrawable());
        assertEquals(context.getString(R.string.direction_up_description), imageView.getContentDescription().toString());
    }

    @Test
    public void bind_directionDown_setsDownIconAndDescription() {
        WeightHistoryItem item = new WeightHistoryItem(entry(185.5, LocalDateTime.of(2026, 3, 5, 8, 0)), WeightDirection.DOWN);
        adapter.setHistoryItems(Collections.singletonList(item));

        WeightHistoryAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        ImageView imageView = holder.itemView.findViewById(R.id.image_direction);
        assertNotNull(imageView.getDrawable());
        assertEquals(context.getString(R.string.direction_down_description), imageView.getContentDescription().toString());
    }

    @Test
    public void bind_directionUnchanged_setsRemoveIconAndDescription() {
        WeightHistoryItem item = new WeightHistoryItem(entry(185.5, LocalDateTime.of(2026, 3, 5, 8, 0)), WeightDirection.UNCHANGED);
        adapter.setHistoryItems(Collections.singletonList(item));

        WeightHistoryAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        ImageView imageView = holder.itemView.findViewById(R.id.image_direction);
        assertNotNull(imageView.getDrawable());
        assertEquals(context.getString(R.string.direction_unchanged_description), imageView.getContentDescription().toString());
    }

    @Test
    public void bind_directionNone_setsRemoveIconAndDescription() {
        WeightHistoryItem item = new WeightHistoryItem(entry(185.5, LocalDateTime.of(2026, 3, 5, 8, 0)), WeightDirection.NONE);
        adapter.setHistoryItems(Collections.singletonList(item));

        WeightHistoryAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        ImageView imageView = holder.itemView.findViewById(R.id.image_direction);
        assertNotNull(imageView.getDrawable());
        assertEquals(context.getString(R.string.direction_none_description), imageView.getContentDescription().toString());
    }
}
