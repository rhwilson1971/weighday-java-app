package xyz.drreub.weighday.domain.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WeightHistoryMapper {

    private static final double EPSILON = 0.001;

    /**
     * Prevents instantiation of this utility class.
     */
    private WeightHistoryMapper() {
        // Utility class
    }

    /**
     * Maps entries in newest-first order to history items without reordering the input.
     * Each entry is compared with the next, older entry; an absolute weight difference
     * below {@code 0.001} is {@code UNCHANGED}. The last (oldest) entry is {@code NONE}.
     *
     * @param entries entries already ordered newest first, with no null elements;
     *         may be null or empty
     * @return items in input order retaining the original entities, or an empty list
     *         when the input is null or empty
     */
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
