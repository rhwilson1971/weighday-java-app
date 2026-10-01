package xyz.drreub.weighday.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import xyz.drreub.weighday.R;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.domain.model.WeightDirection;
import xyz.drreub.weighday.domain.model.WeightHistoryItem;
import xyz.drreub.weighday.domain.model.WeightHistoryMapper;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.ViewHolder> {

    private List<WeightHistoryItem> items = new ArrayList<>();

    /**
     * Replaces the displayed history and notifies the adapter that the data changed.
     *
     * @param items history items to retain by reference, or null to clear the list
     */
    public void setHistoryItems(@Nullable List<WeightHistoryItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Maps raw weigh-ins to direction-aware items and refreshes the displayed history.
     *
     * @param entries weigh-ins ordered newest first, or null to clear the list
     */
    public void setEntries(@Nullable List<WeightEntryEntity> entries) {
        setHistoryItems(WeightHistoryMapper.toHistoryItems(entries));
    }

    /**
     * Inflates a history row and creates a holder for its views.
     *
     * @param parent the recycler that will contain the row
     * @param viewType the requested view type; all rows use the same layout
     * @return a holder for the inflated row
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weight_entry, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Displays an item's weight, date when present, and accessible direction indicator.
     *
     * @param holder the row holder to populate
     * @param position the item's position in the current history
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeightHistoryItem item = items.get(position);
        WeightEntryEntity entry = item.getEntity();
        Context context = holder.itemView.getContext();

        holder.textWeight.setText(
                context.getString(R.string.goal_weight_text, entry.weight)
        );

        if (entry.date != null) {
            holder.textDate.setText(entry.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        }

        bindDirection(holder.imageDirection, item.getDirection(), context);
    }

    /**
     * Applies the direction's icon, semantic color, and accessibility description.
     *
     * @param imageView the indicator to update
     * @param direction the direction to display
     * @param context the context used to resolve colors and descriptions
     */
    private void bindDirection(@NonNull ImageView imageView, @NonNull WeightDirection direction, @NonNull Context context) {
        int iconRes;
        int colorRes;
        int descRes;

        switch (direction) {
            case UP:
                iconRes = R.drawable.ic_arrow_upward;
                colorRes = R.color.direction_up;
                descRes = R.string.direction_up_description;
                break;
            case DOWN:
                iconRes = R.drawable.ic_arrow_downward;
                colorRes = R.color.direction_down;
                descRes = R.string.direction_down_description;
                break;
            case UNCHANGED:
                iconRes = R.drawable.ic_remove;
                colorRes = R.color.direction_neutral;
                descRes = R.string.direction_unchanged_description;
                break;
            case NONE:
            default:
                iconRes = R.drawable.ic_remove;
                colorRes = R.color.direction_neutral;
                descRes = R.string.direction_none_description;
                break;
        }

        imageView.setImageResource(iconRes);
        imageView.setColorFilter(ContextCompat.getColor(context, colorRes));
        imageView.setContentDescription(context.getString(descRes));
    }

    /**
     * Returns the number of history rows currently held by the adapter.
     *
     * @return the current history size
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageDirection;
        TextView textDate;
        TextView textWeight;

        /**
         * Caches the direction, date, and weight views in a history row.
         *
         * @param itemView the inflated history row
         */
        ViewHolder(View itemView) {
            super(itemView);
            imageDirection = itemView.findViewById(R.id.image_direction);
            textDate = itemView.findViewById(R.id.text_date);
            textWeight = itemView.findViewById(R.id.text_weight);
        }
    }
}
