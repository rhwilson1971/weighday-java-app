package xyz.drreub.weighday.testutil;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import xyz.drreub.weighday.data.local.AppDatabase;

/** Installs a fresh in-memory AppDatabase as the app singleton for each test. */
@RunWith(AndroidJUnit4.class)
public abstract class BaseDbTest {
    public static final String USER = "testUser";

    @Rule public InstantTaskExecutorRule instantExecutor = new InstantTaskExecutorRule();

    protected AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        AppDatabase.setInstanceForTesting(db);
    }

    @After
    public void closeDb() {
        AppDatabase.setInstanceForTesting(null);
        db.close();
    }
}
