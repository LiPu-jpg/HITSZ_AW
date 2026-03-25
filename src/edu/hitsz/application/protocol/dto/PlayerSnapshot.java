package edu.hitsz.application.protocol.dto;

public class PlayerSnapshot {

    private final String sessionId;
    private final String playerId;
    private final boolean localPlayer;
    private final int x;
    private final int y;
    private final int hp;
    private final int score;

    public PlayerSnapshot(String sessionId, String playerId, boolean localPlayer, int x, int y, int hp, int score) {
        this.sessionId = sessionId;
        this.playerId = playerId;
        this.localPlayer = localPlayer;
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.score = score;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public boolean isLocalPlayer() {
        return localPlayer;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHp() {
        return hp;
    }

    public int getScore() {
        return score;
    }
}
