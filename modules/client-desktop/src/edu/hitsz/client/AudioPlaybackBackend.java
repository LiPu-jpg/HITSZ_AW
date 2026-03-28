package edu.hitsz.client;

interface AudioPlaybackBackend {

    void apply(AudioSnapshotDecision decision);

    void shutdown();
}
