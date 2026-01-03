package net.cynreub.weighday.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import net.cynreub.weighday.databinding.FragmentAddWeightBinding;
import net.cynreub.weighday.ui.viewmodel.AddWeightViewModel;

public class AddWeightFragment extends Fragment {

    private FragmentAddWeightBinding binding;
    private AddWeightViewModel viewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAddWeightBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AddWeightViewModel.class);

        // Setup Pickers
        binding.pickerWeightWhole.setMinValue(0);
        binding.pickerWeightWhole.setMaxValue(500);
        binding.pickerWeightWhole.setValue(150); // Default

        binding.pickerWeightDecimal.setMinValue(0);
        binding.pickerWeightDecimal.setMaxValue(9);
        binding.pickerWeightDecimal.setValue(0);

        binding.buttonSave.setOnClickListener(v -> {
            int whole = binding.pickerWeightWhole.getValue();
            int decimal = binding.pickerWeightDecimal.getValue();
            double weight = whole + (decimal / 10.0);
            
            String note = binding.editNote.getText().toString();
            
            viewModel.saveWeight(weight, note);
            NavHostFragment.findNavController(AddWeightFragment.this).popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
