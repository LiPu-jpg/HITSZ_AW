package edu.hitsz.common.protocol.json;

import edu.hitsz.common.protocol.dto.LobbyConfigPayload;

public class LobbyConfigPayloadJsonMapper {

    public String toJson(LobbyConfigPayload payload) {
        return "{\"difficulty\":"
                + SimpleJsonSupport.quote(payload.getDifficulty())
                + "}";
    }

    public LobbyConfigPayload fromJson(String json) {
        return new LobbyConfigPayload(SimpleJsonSupport.extractString(json, "difficulty"));
    }
}
