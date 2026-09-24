package xyz.drreub.weighday.data.repository;

import android.app.Application;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;

import xyz.drreub.weighday.data.local.AppDatabase;
import xyz.drreub.weighday.data.local.dao.WeightEntryDao;
import xyz.drreub.weighday.data.local.dao.WeightGoalDao;
import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.data.local.entity.WeightGoalEntity;

import java.util.List;
import java.util.concurrent.Executor;

public class WeightRepository {

    public interface OnGoalWeightUpdated {
        public void onGoalWeightUpdated(long goalId);
    }

    private final WeightEntryDao weightEntryDao;
    private final WeightGoalDao weightGoalDao;
    private final Executor writeExecutor;

    public WeightRepository(Application application) {
        this(AppDatabase.getDatabase(application));
    }

    private WeightRepository(AppDatabase db) {
        this(db.weightEntryDao(), db.weightGoalDao(), AppDatabase.databaseWriteExecutor);
    }

    @VisibleForTesting
    public WeightRepository(WeightEntryDao weightEntryDao, WeightGoalDao weightGoalDao, Executor writeExecutor) {
        this.weightEntryDao = weightEntryDao;
        this.weightGoalDao = weightGoalDao;
        this.writeExecutor = writeExecutor;
    }

    // Weight Entry Operations
    public LiveData<List<WeightEntryEntity>> getAllEntries(String userId) {
        return weightEntryDao.getAllEntries(userId);
    }

    public LiveData<WeightEntryEntity> getMostRecentEntry(String userId) {
        return weightEntryDao.getMostRecentEntry(userId);
    }

    public void insert(WeightEntryEntity weightEntry) {
        writeExecutor.execute(() -> {
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
        writeExecutor.execute(() -> {
            long id = weightGoalDao.insert(weightGoal);

        });
    }

    public void insertWeightGoal(WeightGoalEntity weightGoal, OnGoalWeightUpdated callback) {
        writeExecutor.execute(() -> {
            long id = weightGoalDao.insert(weightGoal);
            callback.onGoalWeightUpdated(id);
        });
    }

    public void update(WeightGoalEntity weightGoal) {
        writeExecutor.execute(() -> {
            weightGoalDao.update(weightGoal);
        });
    }

    public void saveWeightEntryWithGoal(double weight, String note, String userId) {
        writeExecutor.execute(() -> {
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

