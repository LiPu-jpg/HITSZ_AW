package edu.hitsz.server;

public class PlayerSession {

    private final String sessionId;
    private final String playerId;
    private final PlayerRuntimeState playerState;
    private boolean connected;
    private boolean ready;
    private long lastSeenMillis;
    private long disconnectedAtMillis = -1L;

    public PlayerSession(String sessionId, String playerId) {
        this.sessionId = sessionId;
        this.playerId = playerId;
        this.playerState = new PlayerRuntimeState(playerId);
        markConnected(System.currentTimeMillis());
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public PlayerRuntimeState getPlayerState() {
        return playerState;
    }

    public synchronized boolean isConnected() {
        return connected;
    }

    public synchronized void markConnected(long nowMillis) {
        connected = true;
        lastSeenMillis = nowMillis;
        disconnectedAtMillis = -1L;
    }

    public synchronized void markSeen(long nowMillis) {
        lastSeenMillis = nowMillis;
    }

    public synchronized void markDisconnected(long nowMillis) {
        connected = false;
        ready = false;
        disconnectedAtMillis = nowMillis;
    }

    public synchronized boolean isReady() {
        return ready;
    }

    public synchronized void setReady(boolean ready) {
        this.ready = ready;
    }

    public synchronized boolean isExpired(long nowMillis, long retentionMillis) {
        return !connected
                && disconnectedAtMillis >= 0
                && nowMillis - disconnectedAtMillis >= retentionMillis;
    }
}
