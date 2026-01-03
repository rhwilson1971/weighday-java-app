package net.cynreub.weighday.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import net.cynreub.weighday.data.local.entity.WeightEntryEntity;

import java.util.List;

@Dao
public interface WeightEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WeightEntryEntity weightEntry);

    @Query("SELECT * FROM weight_entries WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<WeightEntryEntity>> getAllEntries(String userId);

    @Query("SELECT * FROM weight_entries WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    LiveData<WeightEntryEntity> getMostRecentEntry(String userId);
}
