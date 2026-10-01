package xyz.drreub.weighday.domain.model;

import androidx.annotation.NonNull;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;

import java.util.Objects;

public class WeightHistoryItem {
    @NonNull
    private final WeightEntryEntity entity;
    @NonNull
    private final WeightDirection direction;

    public WeightHistoryItem(@NonNull WeightEntryEntity entity, @NonNull WeightDirection direction) {
        this.entity = Objects.requireNonNull(entity, "entity must not be null");
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
    }

    @NonNull
    public WeightEntryEntity getEntity() {
        return entity;
    }

    @NonNull
    public WeightDirection getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeightHistoryItem that = (WeightHistoryItem) o;
        return entity.equals(that.entity) && direction == that.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity, direction);
    }
}
