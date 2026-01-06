package net.cynreub.weighday.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import net.cynreub.weighday.data.local.entity.WeightEntryEntity;
import net.cynreub.weighday.data.local.entity.WeightGoalEntity;
import net.cynreub.weighday.data.repository.WeightRepository;
import net.cynreub.weighday.data.repository.WeightRepository.OnGoalWeightUpdated;
import java.time.LocalDate;

public class WeightViewModel extends AndroidViewModel {

    private final WeightRepository repository;
    private final String userId = "testUser";

    public WeightViewModel(@NonNull Application application) {
        super(application);
        repository = new WeightRepository(application);
    }

    public LiveData<WeightEntryEntity> getMostRecentEntry() {
        return repository.getMostRecentEntry(userId);
    }

    public LiveData<WeightGoalEntity> getMostRecentGoal() {
        return repository.getMostRecentGoal(userId);
    }

    public void setNewGoal(double goalWeight, double startWeight) {
        WeightGoalEntity goal = new WeightGoalEntity(
                goalWeight,
                startWeight,
                LocalDate.now(),
                null,
                userId
        );
        // repository.insert(goal);

        repository.insertWeightGoal(goal, goalId -> {
            WeightEntryEntity entry = new WeightEntryEntity(
                    startWeight,
                    "Initial",
                    java.time.LocalDateTime.now(),
                    userId,
                    (int) goalId
            );
            repository.insert(entry);

        });
    }

    public void checkGoalAchieved(WeightEntryEntity entry, WeightGoalEntity goal) {
        if (entry != null && goal != null && goal.achievedDate == null) {
            if (entry.weight <= goal.goalWeight) {
                goal.achievedDate = LocalDate.now();
                repository.update(goal);
            }
        }
    }
}
