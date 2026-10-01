package xyz.drreub.weighday.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.data.repository.WeightRepository;
import xyz.drreub.weighday.domain.model.WeightHistoryItem;
import xyz.drreub.weighday.domain.model.WeightHistoryMapper;

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

    public LiveData<List<WeightHistoryItem>> getHistoryItems() {
        return Transformations.map(getAllEntries(), WeightHistoryMapper::toHistoryItems);
    }
}
