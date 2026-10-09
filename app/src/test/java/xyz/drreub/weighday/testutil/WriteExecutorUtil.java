package xyz.drreub.weighday.testutil;

import static org.junit.Assert.assertTrue;

import xyz.drreub.weighday.data.local.AppDatabase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * Blocks until every task queued on AppDatabase.databaseWriteExecutor (4 threads) has finished.
 * All 4 barrier tasks can only run together once every thread is idle.
 */
public final class WriteExecutorUtil {
    private static final int THREADS = 4;

    private WriteExecutorUtil() {}

    public static void flush() throws InterruptedException {
        CyclicBarrier barrier = new CyclicBarrier(THREADS);
        CountDownLatch done = new CountDownLatch(THREADS);
        for (int i = 0; i < THREADS; i++) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    done.countDown();
                }
            });
        }
        assertTrue("write executor did not drain", done.await(10, TimeUnit.SECONDS));
    }
}
