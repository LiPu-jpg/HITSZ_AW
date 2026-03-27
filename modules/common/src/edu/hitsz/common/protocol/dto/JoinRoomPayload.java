package edu.hitsz.common.protocol.dto;

public class JoinRoomPayload {

    private final String roomCode;

    public JoinRoomPayload(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomCode() {
        return roomCode;
    }
}
