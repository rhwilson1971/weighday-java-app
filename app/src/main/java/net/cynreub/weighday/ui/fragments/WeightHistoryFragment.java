package net.cynreub.weighday.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import net.cynreub.weighday.databinding.FragmentWeightHistoryBinding;
import net.cynreub.weighday.ui.adapters.WeightHistoryAdapter;
import net.cynreub.weighday.ui.viewmodel.WeightHistoryViewModel;

public class WeightHistoryFragment extends Fragment {

    private FragmentWeightHistoryBinding binding;
    private WeightHistoryViewModel viewModel;
    private WeightHistoryAdapter adapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentWeightHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(WeightHistoryViewModel.class);

        adapter = new WeightHistoryAdapter();
        binding.recyclerHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerHistory.setAdapter(adapter);

        viewModel.getAllEntries().observe(getViewLifecycleOwner(), entries -> {
            adapter.setEntries(entries);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
