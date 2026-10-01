package xyz.drreub.weighday.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import xyz.drreub.weighday.databinding.FragmentWeightHistoryBinding;
import xyz.drreub.weighday.ui.adapters.WeightHistoryAdapter;
import xyz.drreub.weighday.ui.viewmodel.WeightHistoryViewModel;

public class WeightHistoryFragment extends Fragment {

    private FragmentWeightHistoryBinding binding;
    private WeightHistoryAdapter adapter;

    /**
     * Inflates and retains the binding for the history screen.
     *
     * @param inflater the inflater used to create the view
     * @param container the parent the view will be attached to, if any
     * @param savedInstanceState previously saved fragment state, if available
     * @return the history screen's root view
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentWeightHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Configures the history list and observes direction-aware items for the view lifecycle.
     *
     * @param view the fragment's newly created view
     * @param savedInstanceState previously saved fragment state, if available
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        WeightHistoryViewModel viewModel = new ViewModelProvider(this).get(WeightHistoryViewModel.class);

        adapter = new WeightHistoryAdapter();
        binding.recyclerHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerHistory.setAdapter(adapter);

        viewModel.getHistoryItems().observe(getViewLifecycleOwner(), items -> {
            adapter.setHistoryItems(items);
        });
    }

    /**
     * Releases the binding when the fragment view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
