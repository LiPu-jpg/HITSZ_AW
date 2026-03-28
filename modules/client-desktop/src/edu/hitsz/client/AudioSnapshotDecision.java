package edu.hitsz.client;

public class AudioSnapshotDecision {

    private final String loopTrack;
    private final boolean playExplosion;
    private final boolean playGameOver;

    public AudioSnapshotDecision(String loopTrack, boolean playExplosion, boolean playGameOver) {
        this.loopTrack = loopTrack;
        this.playExplosion = playExplosion;
        this.playGameOver = playGameOver;
    }

    public String getLoopTrack() {
        return loopTrack;
    }

    public boolean shouldPlayExplosion() {
        return playExplosion;
    }

    public boolean shouldPlayGameOver() {
        return playGameOver;
    }
}
