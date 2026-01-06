package net.cynreub.weighday.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import net.cynreub.weighday.data.local.entity.WeightEntryEntity;
import net.cynreub.weighday.data.repository.WeightRepository;

import java.time.LocalDateTime;

public class AddWeightViewModel extends AndroidViewModel {

    private final WeightRepository repository;
    private final String userId = "testUser";

    public AddWeightViewModel(@NonNull Application application) {
        super(application);
        repository = new WeightRepository(application);
    }

    public void saveWeight(double weight, String note) {
        repository.saveWeightEntryWithGoal(weight, note, userId);
    }

    public LiveData<WeightEntryEntity> getLastWeight() {
        return repository.getMostRecentEntry(userId);
    }
}
