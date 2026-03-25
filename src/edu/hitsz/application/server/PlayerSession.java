package edu.hitsz.application.server;

public class PlayerSession {

    private final String sessionId;
    private final String playerId;
    private final PlayerRuntimeState playerState;

    public PlayerSession(String sessionId, String playerId) {
        this.sessionId = sessionId;
        this.playerId = playerId;
        this.playerState = new PlayerRuntimeState(playerId);
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
}
