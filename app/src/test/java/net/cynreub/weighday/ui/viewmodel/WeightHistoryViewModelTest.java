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

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WeightHistoryViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private WeightRepository mockRepository;

    @Mock
    private Application mockApplication;

    private WeightHistoryViewModel viewModel;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        viewModel = new WeightHistoryViewModel(mockApplication, mockRepository);
    }

    @Test
    public void testGetAllEntries_returnsLiveData() {
        MutableLiveData<List<WeightEntryEntity>> liveData = new MutableLiveData<>();
        when(mockRepository.getAllEntries("testUser")).thenReturn(liveData);

        assertEquals(liveData, viewModel.getAllEntries());
        verify(mockRepository).getAllEntries("testUser");
    }
}
