package xyz.drreub.weighday.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.data.local.entity.WeightGoalEntity;
import xyz.drreub.weighday.data.repository.WeightRepository;
import xyz.drreub.weighday.data.repository.WeightRepository.OnGoalWeightUpdated;
import java.time.LocalDate;

public class WeightViewModel extends AndroidViewModel {

    private final WeightRepository repository;
    private final String userId = "testUser";

    public WeightViewModel(@NonNull Application application) {
        super(application);
        repository = new WeightRepository(application);
    }

    // For testing purposes
    public WeightViewModel(@NonNull Application application, WeightRepository repository) {
        super(application);
        this.repository = repository;
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
            boolean achieved = false;
            if (goal.startWeight > goal.goalWeight) {
                // Weight loss goal
                achieved = entry.weight <= goal.goalWeight;
            } else if (goal.startWeight < goal.goalWeight) {
                // Weight gain goal
                achieved = entry.weight >= goal.goalWeight;
            }

            if (achieved) {
                goal.achievedDate = LocalDate.now();
                repository.update(goal);
            }
        }
    }
}
