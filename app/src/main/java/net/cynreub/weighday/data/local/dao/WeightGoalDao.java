package net.cynreub.weighday.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import net.cynreub.weighday.data.local.entity.WeightGoalEntity;

import java.util.List;

@Dao
public interface WeightGoalDao {
    @Query("SELECT * FROM weight_goals WHERE userId = :userId")
    LiveData<List<WeightGoalEntity>> getAllGoals(String userId);

    @Query("SELECT * FROM weight_goals WHERE userId = :userId ORDER BY id DESC LIMIT 1")
    LiveData<WeightGoalEntity> getMostRecentGoal(String userId);

    @Query("SELECT * FROM weight_goals WHERE userId = :userId ORDER BY id DESC LIMIT 1")
    WeightGoalEntity getMostRecentGoalSync(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(WeightGoalEntity weightGoal);

    @Update
    void update(WeightGoalEntity weightGoal);
}
