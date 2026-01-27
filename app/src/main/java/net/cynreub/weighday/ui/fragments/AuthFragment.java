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

import net.cynreub.weighday.R;
import net.cynreub.weighday.databinding.FragmentAuthBinding;
import net.cynreub.weighday.ui.viewmodel.AuthViewModel;

public class AuthFragment extends Fragment {

    private FragmentAuthBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAuthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        viewModel.isPinSet().observe(getViewLifecycleOwner(), isSet -> {
            if (Boolean.TRUE.equals(isSet)) {
                binding.tvTitle.setText(R.string.auth_enter_pin);
                binding.tvInstructions.setText(R.string.auth_login_instructions);
                binding.btnAction.setText(R.string.auth_unlock);
            } else {
                binding.tvTitle.setText(R.string.auth_create_pin);
                binding.tvInstructions.setText(R.string.auth_setup_instructions);
                binding.btnAction.setText(R.string.auth_set_pin);
            }
        });

        viewModel.isAuthenticated().observe(getViewLifecycleOwner(), authenticated -> {
            if (Boolean.TRUE.equals(authenticated)) {
                NavHostFragment.findNavController(AuthFragment.this)
                        .navigate(R.id.action_Auth_to_WeightTracker);
            }
        });
        
        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
             if (errorMsg != null) {
                binding.etPin.setError(errorMsg);
             }
        });

        binding.btnAction.setOnClickListener(v -> {
            String pin = binding.etPin.getText().toString();
            viewModel.submitPin(pin);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
