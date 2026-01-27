package net.cynreub.weighday.data.repository;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class AuthRepository {
    private static final String PREFS_FILENAME = "secure_prefs";
    private static final String KEY_PIN = "user_pin";
    private SharedPreferences sharedPreferences;

    public AuthRepository(Application application) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREFS_FILENAME,
                    masterKeyAlias,
                    application,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            // Ideally handle this more gracefully, e.g., by resetting keys if corrupted
        }
    }

    public boolean isPinSet() {
        return sharedPreferences != null && sharedPreferences.contains(KEY_PIN);
    }

    public void savePin(String pin) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_PIN, pin).apply();
        }
    }

    public boolean validatePin(String pin) {
        if (sharedPreferences == null) return false;
        String storedPin = sharedPreferences.getString(KEY_PIN, null);
        return storedPin != null && storedPin.equals(pin);
    }
}
