package xyz.drreub.weighday.domain.model;

import androidx.annotation.NonNull;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;

import java.util.Objects;

public class WeightHistoryItem {
    @NonNull
    private final WeightEntryEntity entity;
    @NonNull
    private final WeightDirection direction;

    /**
     * Pairs a weigh-in with its direction relative to the preceding weigh-in.
     *
     * @param entity the non-null weigh-in entity, retained by reference
     * @param direction the non-null direction associated with the weigh-in
     * @throws NullPointerException if either argument is null
     */
    public WeightHistoryItem(@NonNull WeightEntryEntity entity, @NonNull WeightDirection direction) {
        this.entity = Objects.requireNonNull(entity, "entity must not be null");
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
    }

    /**
     * Returns the weigh-in entity retained by this item.
     *
     * @return the original entity, without a defensive copy
     */
    @NonNull
    public WeightEntryEntity getEntity() {
        return entity;
    }

    /**
     * Returns the direction associated with this weigh-in.
     *
     * @return the weight change direction, or {@code NONE} for the baseline
     */
    @NonNull
    public WeightDirection getDirection() {
        return direction;
    }

    /**
     * Compares items using the entity's equality and the direction value.
     *
     * @param o the object to compare with this item
     * @return whether both objects are history items with equal entities and directions
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeightHistoryItem that = (WeightHistoryItem) o;
        return entity.equals(that.entity) && direction == that.direction;
    }

    /**
     * Computes a hash from the entity and direction used by {@link #equals(Object)}.
     *
     * @return the combined hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(entity, direction);
    }
}
