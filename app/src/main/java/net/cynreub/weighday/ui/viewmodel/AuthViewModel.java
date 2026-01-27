package net.cynreub.weighday.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import net.cynreub.weighday.data.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<Boolean> _isAuthenticated = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _isPinSet = new MutableLiveData<>();
    private final MutableLiveData<String> _error = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        checkIfPinSet();
    }

    public LiveData<Boolean> isAuthenticated() {
        return _isAuthenticated;
    }

    public LiveData<Boolean> isPinSet() {
        return _isPinSet;
    }
    
    public LiveData<String> getError() {
        return _error;
    }

    private void checkIfPinSet() {
        _isPinSet.setValue(authRepository.isPinSet());
    }

    public void submitPin(String pin) {
        if (pin == null || pin.length() < 4) {
            _error.setValue(getApplication().getString(net.cynreub.weighday.R.string.auth_error_length));
            return;
        }

        if (Boolean.TRUE.equals(_isPinSet.getValue())) {
            // Login mode
            if (authRepository.validatePin(pin)) {
                _isAuthenticated.setValue(true);
            } else {
                _error.setValue(getApplication().getString(net.cynreub.weighday.R.string.auth_error_incorrect));
            }
        } else {
            // Setup mode
            authRepository.savePin(pin);
            _isPinSet.setValue(true);
            _isAuthenticated.setValue(true);
        }
    }
}
