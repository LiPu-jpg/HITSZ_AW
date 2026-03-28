package edu.hitsz.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AudioManager {

    private static final AudioManager INSTANCE = new AudioManager(
            new ClipAudioPlaybackBackend(),
            createDefaultScheduler()
    );

    private final AudioPlaybackBackend backend;
    private final AudioTaskScheduler scheduler;

    AudioManager(AudioPlaybackBackend backend, AudioTaskScheduler scheduler) {
        this.backend = backend;
        this.scheduler = scheduler;
    }

    public static AudioManager getInstance() {
        return INSTANCE;
    }

    public void apply(AudioSnapshotDecision decision) {
        scheduler.execute(() -> backend.apply(decision));
    }

    public void shutdown() {
        backend.shutdown();
    }

    private static AudioTaskScheduler createDefaultScheduler() {
        ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "aw-audio");
            thread.setDaemon(true);
            return thread;
        });
        return executorService::execute;
    }
}
