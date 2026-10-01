package xyz.drreub.weighday.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WeightHistoryMapper {

    private static final double EPSILON = 0.001;

    private WeightHistoryMapper() {
        // Utility class
    }

    @NonNull
    public static List<WeightHistoryItem> toHistoryItems(@Nullable List<WeightEntryEntity> entries) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }

        List<WeightHistoryItem> items = new ArrayList<>(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            WeightEntryEntity current = entries.get(i);
            WeightDirection direction;

            if (i == entries.size() - 1) {
                direction = WeightDirection.NONE;
            } else {
                WeightEntryEntity previous = entries.get(i + 1);
                double diff = current.weight - previous.weight;
                if (Math.abs(diff) < EPSILON) {
                    direction = WeightDirection.UNCHANGED;
                } else if (diff > 0) {
                    direction = WeightDirection.UP;
                } else {
                    direction = WeightDirection.DOWN;
                }
            }
            items.add(new WeightHistoryItem(current, direction));
        }

        return items;
    }
}
