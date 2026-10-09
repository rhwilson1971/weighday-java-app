package net.cynreub.weighday.ui.viewmodel;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import net.cynreub.weighday.data.local.entity.WeightEntryEntity;
import net.cynreub.weighday.data.repository.WeightRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddWeightViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private WeightRepository mockRepository;

    @Mock
    private Application mockApplication;

    private AddWeightViewModel viewModel;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        viewModel = new AddWeightViewModel(mockApplication, mockRepository);
    }

    @Test
    public void testSaveWeight_callsRepository() {
        double weight = 80.5;
        String note = "Feeling good";
        
        viewModel.saveWeight(weight, note);
        
        verify(mockRepository).saveWeightEntryWithGoal(weight, note, "testUser");
    }

    @Test
    public void testGetLastWeight_returnsLiveData() {
        MutableLiveData<WeightEntryEntity> liveData = new MutableLiveData<>();
        when(mockRepository.getMostRecentEntry("testUser")).thenReturn(liveData);

        assertEquals(liveData, viewModel.getLastWeight());
        verify(mockRepository).getMostRecentEntry("testUser");
    }
}
