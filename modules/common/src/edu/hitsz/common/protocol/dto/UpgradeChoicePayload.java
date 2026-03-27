package edu.hitsz.common.protocol.dto;

public class UpgradeChoicePayload {

    private final String choice;

    public UpgradeChoicePayload(String choice) {
        this.choice = choice;
    }

    public String getChoice() {
        return choice;
    }
}
