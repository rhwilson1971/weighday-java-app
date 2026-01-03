package net.cynreub.weighday.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.time.LocalDateTime;

@Entity(tableName = "weight_entries")
public class WeightEntryEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public double weight;
    public String note;
    public LocalDateTime date;
    public String userId;
    public Integer goalId;

    public WeightEntryEntity(double weight, String note, LocalDateTime date, String userId, Integer goalId) {
        this.weight = weight;
        this.note = note;
        this.date = date;
        this.userId = userId;
        this.goalId = goalId;
    }
}
