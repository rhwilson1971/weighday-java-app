package net.cynreub.weighday.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.time.LocalDate;

@Entity(tableName = "weight_goals")
public class WeightGoalEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public double goalWeight;
    public double startWeight;
    public LocalDate startDate;
    public LocalDate achievedDate;
    public String userId;

    public WeightGoalEntity(double goalWeight, double startWeight, LocalDate startDate, LocalDate achievedDate, String userId) {
        this.goalWeight = goalWeight;
        this.startWeight = startWeight;
        this.startDate = startDate;
        this.achievedDate = achievedDate;
        this.userId = userId;
    }
}
