package net.cynreub.weighday.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import net.cynreub.weighday.R;
import net.cynreub.weighday.data.local.entity.WeightEntryEntity;
import net.cynreub.weighday.data.local.entity.WeightGoalEntity;
import net.cynreub.weighday.databinding.FragmentWeightTrackerBinding;
import net.cynreub.weighday.ui.viewmodel.WeightViewModel;

public class WeightTrackerFragment extends Fragment {

    private FragmentWeightTrackerBinding binding;
    private WeightViewModel viewModel;
    private WeightEntryEntity currentEntry;
    private WeightGoalEntity currentGoal;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentWeightTrackerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(WeightViewModel.class);

        binding.buttonAddWeight.setOnClickListener(v ->
                NavHostFragment.findNavController(WeightTrackerFragment.this)
                        .navigate(R.id.action_Tracker_to_AddWeight)
        );

        binding.textGoalLabel.setOnClickListener(v -> showSetGoalDialog());
        binding.textGoalWeight.setOnClickListener(v -> showSetGoalDialog());
        binding.progressView.setOnClickListener(v -> showSetGoalDialog());

        viewModel.getMostRecentEntry().observe(getViewLifecycleOwner(), entry -> {
            currentEntry = entry;
            updateUI();
            viewModel.checkGoalAchieved(currentEntry, currentGoal);
        });

        viewModel.getMostRecentGoal().observe(getViewLifecycleOwner(), goal -> {
            currentGoal = goal;
            updateUI();
            viewModel.checkGoalAchieved(currentEntry, currentGoal);
        });

        setupMenu();
    }

    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear(); // Clear default main menu if any
                menuInflater.inflate(R.menu.menu_main, menu);
                MenuItem item = menu.findItem(R.id.action_settings);
                if (item != null) {
                    item.setTitle("History");
                    item.setIcon(android.R.drawable.ic_menu_recent_history);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_settings) {
                     NavHostFragment.findNavController(WeightTrackerFragment.this)
                            .navigate(R.id.action_Tracker_to_History);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void updateUI() {
        if (currentEntry != null) {
            binding.textLastWeight.setText(
                    getString(R.string.last_recorded_weight_text, currentEntry.weight)
            );
        } else {
            binding.textLastWeight.setText(R.string.no_weight_recorded_yet_text);
        }

        if (currentGoal != null) {
            binding.textGoalWeight.setText(getString(R.string.goal_weight_text, currentGoal.goalWeight));
            binding.textGoalWeight.setVisibility(View.VISIBLE);
            binding.textGoalLabel.setText(R.string.goal_text);
            binding.buttonAddWeight.setEnabled(true);
        } else {
            binding.textGoalWeight.setVisibility(View.GONE);
            binding.textGoalLabel.setText(R.string.set_goal_text);
            binding.buttonAddWeight.setEnabled(false); // Disable adding weight if no goal set, matching logic? Or maybe allow it. Original: enabled=hasGoal
        }

        updateProgress();
    }

    private void updateProgress() {
        if (currentGoal == null || currentEntry == null) {
            binding.progressView.setProgress(0f);
            return;
        }

        float start = (float) currentGoal.startWeight;
        float target = (float) currentGoal.goalWeight;
        float current = (float) currentEntry.weight;

        float totalDistance = start - target;
        float coveredDistance = start - current;

        float progress = 0f;
        if (totalDistance != 0) {
            progress = coveredDistance / totalDistance;
        }
        
        binding.progressView.setProgress(progress);
    }

    private void showSetGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_goal, null);
        
        final EditText editStart = dialogView.findViewById(R.id.edit_start_weight);
        final EditText editGoal = dialogView.findViewById(R.id.edit_goal_weight);

        // Pre-fill if we have data
        if (currentEntry != null) {
            editStart.setText(String.valueOf(currentEntry.weight));
        } else if (currentGoal != null) {
            editStart.setText(String.valueOf(currentGoal.startWeight));
        }

        if (currentGoal != null) {
            editGoal.setText(String.valueOf(currentGoal.goalWeight));
        }

        builder.setView(dialogView)
                .setPositiveButton("Save", (dialog, id) -> {
                    String startStr = editStart.getText().toString();
                    String goalStr = editGoal.getText().toString();
                    if (!TextUtils.isEmpty(startStr) && !TextUtils.isEmpty(goalStr)) {
                        try {
                            double s = Double.parseDouble(startStr);
                            double g = Double.parseDouble(goalStr);
                            viewModel.setNewGoal(g, s);
                        } catch (NumberFormatException e) {
                            // Handle error
                        }
                    }
                })
                .setNegativeButton(R.string.cancel_text, (dialog, id) -> dialog.cancel());
        builder.create().show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
