package edu.hitsz.common.protocol.json;

import edu.hitsz.common.protocol.dto.CreateRoomPayload;

public class CreateRoomPayloadJsonMapper {

    public String toJson(CreateRoomPayload payload) {
        return "{\"difficulty\":"
                + SimpleJsonSupport.quote(payload.getDifficulty())
                + "}";
    }

    public CreateRoomPayload fromJson(String json) {
        return new CreateRoomPayload(SimpleJsonSupport.extractString(json, "difficulty"));
    }
}
