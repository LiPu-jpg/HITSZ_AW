package edu.hitsz.common.protocol.json;

import edu.hitsz.common.protocol.dto.UpgradeChoicePayload;

public class UpgradeChoicePayloadJsonMapper {

    public String toJson(UpgradeChoicePayload payload) {
        return "{\"choice\":" + SimpleJsonSupport.quote(payload.getChoice()) + "}";
    }

    public UpgradeChoicePayload fromJson(String json) {
        return new UpgradeChoicePayload(SimpleJsonSupport.extractString(json, "choice"));
    }
}
