package edu.hitsz.common.protocol.json;

import edu.hitsz.common.protocol.dto.JoinRoomPayload;

public class JoinRoomPayloadJsonMapper {

    public String toJson(JoinRoomPayload payload) {
        return "{\"roomCode\":"
                + SimpleJsonSupport.quote(payload.getRoomCode())
                + "}";
    }

    public JoinRoomPayload fromJson(String json) {
        return new JoinRoomPayload(SimpleJsonSupport.extractString(json, "roomCode"));
    }
}
