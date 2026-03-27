package edu.hitsz.common.protocol.json;

import edu.hitsz.common.protocol.dto.ReadyPayload;

public class ReadyPayloadJsonMapper {

    public String toJson(ReadyPayload payload) {
        return "{\"ready\":" + payload.isReady() + "}";
    }

    public ReadyPayload fromJson(String json) {
        return new ReadyPayload(SimpleJsonSupport.extractBoolean(json, "ready"));
    }
}
