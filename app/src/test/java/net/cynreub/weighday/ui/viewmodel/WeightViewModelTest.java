package net.cynreub.weighday.ui.viewmodel;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import net.cynreub.weighday.data.local.entity.WeightEntryEntity;
import net.cynreub.weighday.data.local.entity.WeightGoalEntity;
import net.cynreub.weighday.data.repository.WeightRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WeightViewModelTest {

    // Ensures LiveData executes synchronously
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private WeightRepository mockRepository;
    
    @Mock
    private Application mockApplication;

    private WeightViewModel viewModel;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        viewModel = new WeightViewModel(mockApplication, mockRepository);
    }

    @Test
    public void testGetMostRecentEntry_returnsLiveDataFromRepository() {
        MutableLiveData<WeightEntryEntity> liveData = new MutableLiveData<>();
        when(mockRepository.getMostRecentEntry("testUser")).thenReturn(liveData);

        assertEquals(liveData, viewModel.getMostRecentEntry());
        verify(mockRepository).getMostRecentEntry("testUser");
    }

    @Test
    public void testCheckGoalAchieved_weightLossGoal_achieved() {
        // Start weight 100, goal 90. Current 89 -> Achieved
        WeightGoalEntity goal = new WeightGoalEntity(90.0, 100.0, LocalDate.now(), null, "testUser");
        WeightEntryEntity entry = new WeightEntryEntity(89.0, "Check in", null, "testUser", 1);

        viewModel.checkGoalAchieved(entry, goal);

        assertNotNull(goal.achievedDate);
        verify(mockRepository).update(goal);
    }

    @Test
    public void testCheckGoalAchieved_weightGainGoal_achieved() {
        // Start weight 50, goal 60. Current 61 -> Achieved
        WeightGoalEntity goal = new WeightGoalEntity(60.0, 50.0, LocalDate.now(), null, "testUser");
        WeightEntryEntity entry = new WeightEntryEntity(61.0, "Check in", null, "testUser", 1);

        viewModel.checkGoalAchieved(entry, goal);

        assertNotNull(goal.achievedDate);
        verify(mockRepository).update(goal);
    }
}
