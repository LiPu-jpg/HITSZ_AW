package edu.hitsz.common.protocol.dto;

public class InputSkillPayload {

    private final String skillType;

    public InputSkillPayload(String skillType) {
        this.skillType = skillType;
    }

    public String getSkillType() {
        return skillType;
    }
}
