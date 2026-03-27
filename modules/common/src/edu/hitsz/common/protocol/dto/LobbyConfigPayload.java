package edu.hitsz.common.protocol.dto;

public class LobbyConfigPayload {

    private final String difficulty;

    public LobbyConfigPayload(String difficulty) {
        this.difficulty = difficulty;
    }

    public LobbyConfigPayload(String difficulty, String selectedSkill) {
        this(difficulty);
    }

    public String getDifficulty() {
        return difficulty;
    }
}
