package xyz.drreub.weighday.data.local;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppDatabaseTest {

    @After
    public void resetSingleton() {
        AppDatabase.setInstanceForTesting(null);
    }

    @Test
    public void getDatabase_returnsSameSingletonAndExposesDaos() {
        Context context = ApplicationProvider.getApplicationContext();
        AppDatabase first = AppDatabase.getDatabase(context);
        AppDatabase second = AppDatabase.getDatabase(context);

        assertSame(first, second);
        assertNotNull(first.weightEntryDao());
        assertNotNull(first.weightGoalDao());
        first.close();
    }

    @Test
    public void writeExecutor_isAvailable() {
        assertNotNull(AppDatabase.databaseWriteExecutor);
    }
}
