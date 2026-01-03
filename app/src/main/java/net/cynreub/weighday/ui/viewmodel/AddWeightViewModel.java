package net.cynreub.weighday.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

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
        WeightEntryEntity entry = new WeightEntryEntity(
                weight,
                note,
                LocalDateTime.now(),
                userId,
                null // goalId
        );
        repository.insert(entry);
    }
}
