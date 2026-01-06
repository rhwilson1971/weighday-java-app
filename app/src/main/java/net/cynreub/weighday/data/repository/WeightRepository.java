package net.cynreub.weighday.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import net.cynreub.weighday.data.local.AppDatabase;
import net.cynreub.weighday.data.local.dao.WeightEntryDao;
import net.cynreub.weighday.data.local.dao.WeightGoalDao;
import net.cynreub.weighday.data.local.entity.WeightEntryEntity;
import net.cynreub.weighday.data.local.entity.WeightGoalEntity;

import java.util.List;

public class WeightRepository {

    public interface OnGoalWeightUpdated {
        public void onGoalWeightUpdated(long goalId);
    }

    private WeightEntryDao weightEntryDao;
    private WeightGoalDao weightGoalDao;

    public WeightRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        weightEntryDao = db.weightEntryDao();
        weightGoalDao = db.weightGoalDao();
    }

    // Weight Entry Operations
    public LiveData<List<WeightEntryEntity>> getAllEntries(String userId) {
        return weightEntryDao.getAllEntries(userId);
    }

    public LiveData<WeightEntryEntity> getMostRecentEntry(String userId) {
        return weightEntryDao.getMostRecentEntry(userId);
    }

    public void insert(WeightEntryEntity weightEntry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            weightEntryDao.insert(weightEntry);
        });
    }

    // Weight Goal Operations
    public LiveData<List<WeightGoalEntity>> getAllGoals(String userId) {
        return weightGoalDao.getAllGoals(userId);
    }

    public LiveData<WeightGoalEntity> getMostRecentGoal(String userId) {
        return weightGoalDao.getMostRecentGoal(userId);
    }

    public void insert(WeightGoalEntity weightGoal) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = weightGoalDao.insert(weightGoal);

        });
    }

    public void insertWeightGoal(WeightGoalEntity weightGoal, OnGoalWeightUpdated callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long id = weightGoalDao.insert(weightGoal);
            callback.onGoalWeightUpdated(id);
        });
    }

    public void update(WeightGoalEntity weightGoal) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            weightGoalDao.update(weightGoal);
        });
    }

    public void saveWeightEntryWithGoal(double weight, String note, String userId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            WeightGoalEntity goal = weightGoalDao.getMostRecentGoalSync(userId);
            Integer goalId = (goal != null) ? goal.id : null;
            WeightEntryEntity entry = new WeightEntryEntity(
                    weight,
                    note,
                    java.time.LocalDateTime.now(),
                    userId,
                    goalId
            );
            weightEntryDao.insert(entry);
        });
    }
}

