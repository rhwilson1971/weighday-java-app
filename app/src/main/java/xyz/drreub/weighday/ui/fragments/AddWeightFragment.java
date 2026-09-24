package xyz.drreub.weighday.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import xyz.drreub.weighday.data.local.entity.WeightEntryEntity;
import xyz.drreub.weighday.databinding.FragmentAddWeightBinding;
import xyz.drreub.weighday.ui.viewmodel.AddWeightViewModel;

public class AddWeightFragment extends Fragment {

    // With fragments, the binding gets generated automatically
    private FragmentAddWeightBinding binding;

    // Assign a viewmodel per fragment, some fragments can share the same viewmodel
    private AddWeightViewModel viewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // This method override to inflate the layout for this fragment
        binding = FragmentAddWeightBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Instantiate the Viewmodel and bind it to the fragment
        viewModel = new ViewModelProvider(this).get(AddWeightViewModel.class);

        // Setup Pickers
        binding.pickerWeightWhole.setMinValue(0);
        binding.pickerWeightWhole.setMaxValue(500);
        binding.pickerWeightWhole.setValue(150); // Default

        binding.pickerWeightDecimal.setMinValue(0);
        binding.pickerWeightDecimal.setMaxValue(9);
        binding.pickerWeightDecimal.setValue(0);

        // Use live data to update the UI
        viewModel.getLastWeight().observe(getViewLifecycleOwner(), entry -> {
            if (entry != null) {
                int whole = (int) entry.weight;
                int decimal = (int) Math.round((entry.weight - whole) * 10);
                binding.pickerWeightWhole.setValue(whole);
                binding.pickerWeightDecimal.setValue(decimal);
            }
        });

        // Set a handler to save the weight
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
