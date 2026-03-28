package edu.hitsz.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AudioManagerAsyncDispatchTest {

    public static void main(String[] args) throws Exception {
        applyDoesNotBlockCallerThread();
    }

    private static void applyDoesNotBlockCallerThread() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        AudioPlaybackBackend backend = new AudioPlaybackBackend() {
            @Override
            public void apply(AudioSnapshotDecision decision) {
                entered.countDown();
                try {
                    release.await(2, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public void shutdown() {
            }
        };

        AudioTaskScheduler scheduler = task -> {
            Thread thread = new Thread(task, "audio-test-worker");
            thread.setDaemon(true);
            thread.start();
        };

        AudioManager manager = new AudioManager(backend, scheduler);

        long startNanos = System.nanoTime();
        manager.apply(new AudioSnapshotDecision(null, false, false, true, false));
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

        assert elapsedMillis < 100 : "AudioManager.apply should not block the caller thread";
        assert entered.await(1, TimeUnit.SECONDS)
                : "Audio work should still be dispatched to the background scheduler";

        release.countDown();
    }
}
