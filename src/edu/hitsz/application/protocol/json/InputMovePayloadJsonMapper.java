package edu.hitsz.application.protocol.json;

import edu.hitsz.application.protocol.dto.InputMovePayload;

public class InputMovePayloadJsonMapper {

    public String toJson(InputMovePayload payload) {
        return "{\"x\":" + payload.getX() + ",\"y\":" + payload.getY() + "}";
    }

    public InputMovePayload fromJson(String json) {
        return new InputMovePayload(
                SimpleJsonSupport.extractInt(json, "x"),
                SimpleJsonSupport.extractInt(json, "y")
        );
    }
}
