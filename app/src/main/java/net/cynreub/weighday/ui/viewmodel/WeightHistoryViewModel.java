package net.cynreub.weighday.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import net.cynreub.weighday.data.local.entity.WeightEntryEntity;
import net.cynreub.weighday.data.repository.WeightRepository;

import java.util.List;

public class WeightHistoryViewModel extends AndroidViewModel {

    private final WeightRepository repository;
    private final String userId = "testUser";

    public WeightHistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new WeightRepository(application);
    }

    public LiveData<List<WeightEntryEntity>> getAllEntries() {
        return repository.getAllEntries(userId);
    }
}
