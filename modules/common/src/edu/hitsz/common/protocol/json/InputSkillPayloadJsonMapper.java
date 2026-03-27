package edu.hitsz.common.protocol.json;

import edu.hitsz.common.protocol.dto.InputSkillPayload;

public class InputSkillPayloadJsonMapper {

    public String toJson(InputSkillPayload payload) {
        return "{\"skillType\":" + SimpleJsonSupport.quote(payload.getSkillType()) + "}";
    }

    public InputSkillPayload fromJson(String json) {
        return new InputSkillPayload(SimpleJsonSupport.extractString(json, "skillType"));
    }
}
