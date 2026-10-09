package xyz.drreub.weighday.testutil;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicReference;

/** Reads the current value of a LiveData synchronously (use with InstantTaskExecutorRule). */
public final class LiveDataTestUtil {
    private LiveDataTestUtil() {}

    public static <T> T getValue(LiveData<T> liveData) {
        AtomicReference<T> ref = new AtomicReference<>();
        Observer<T> observer = ref::set;
        liveData.observeForever(observer);
        liveData.removeObserver(observer);
        return ref.get();
    }
}
