package edu.hitsz.common.protocol.dto;

public class CreateRoomPayload {

    private final String difficulty;

    public CreateRoomPayload(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficulty() {
        return difficulty;
    }
}
