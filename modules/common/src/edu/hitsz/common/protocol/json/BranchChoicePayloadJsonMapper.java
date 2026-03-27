package edu.hitsz.common.protocol.json;

import edu.hitsz.common.protocol.dto.BranchChoicePayload;

public class BranchChoicePayloadJsonMapper {

    public String toJson(BranchChoicePayload payload) {
        return "{\"branch\":" + SimpleJsonSupport.quote(payload.getBranch()) + "}";
    }

    public BranchChoicePayload fromJson(String json) {
        return new BranchChoicePayload(SimpleJsonSupport.extractString(json, "branch"));
    }
}
