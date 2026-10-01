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

    public void setHistoryItems(@Nullable List<WeightHistoryItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setEntries(@Nullable List<WeightEntryEntity> entries) {
        setHistoryItems(WeightHistoryMapper.toHistoryItems(entries));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weight_entry, parent, false);
        return new ViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageDirection;
        TextView textDate;
        TextView textWeight;

        ViewHolder(View itemView) {
            super(itemView);
            imageDirection = itemView.findViewById(R.id.image_direction);
            textDate = itemView.findViewById(R.id.text_date);
            textWeight = itemView.findViewById(R.id.text_weight);
        }
    }
}
