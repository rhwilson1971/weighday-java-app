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

    /**
     * Creates the repository used to observe the configured user's history.
     *
     * @param application the application context used by the repository
     */
    public WeightHistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new WeightRepository(application);
    }

    /**
     * Observes the configured user's weigh-ins in newest-first order.
     *
     * @return live weigh-in data from the repository
     */
    public LiveData<List<WeightEntryEntity>> getAllEntries() {
        return repository.getAllEntries(userId);
    }

    /**
     * Maps observed weigh-ins to history items with directions relative to older entries.
     *
     * @return live history items in newest-first order
     */
    public LiveData<List<WeightHistoryItem>> getHistoryItems() {
        return Transformations.map(getAllEntries(), WeightHistoryMapper::toHistoryItems);
    }
}
