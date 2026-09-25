package xyz.drreub.weighday.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import xyz.drreub.weighday.R;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.ViewHolder> {

    private List<WeightEntryEntity> entries = new ArrayList<>();

    public void setEntries(List<WeightEntryEntity> entries) {
        this.entries = entries;
        notifyDataSetChanged();
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
        WeightEntryEntity entry = entries.get(position);
        holder.textWeight.setText(
                holder.itemView.getContext().getString(R.string.goal_weight_text, entry.weight)
        );

        if (entry.date != null) {
            holder.textDate.setText(entry.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDate;
        TextView textWeight;

        ViewHolder(View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.text_date);
            textWeight = itemView.findViewById(R.id.text_weight);
        }
    }
}
