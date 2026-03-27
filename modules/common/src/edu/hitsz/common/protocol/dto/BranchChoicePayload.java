package edu.hitsz.common.protocol.dto;

public class BranchChoicePayload {

    private final String branch;

    public BranchChoicePayload(String branch) {
        this.branch = branch;
    }

    public String getBranch() {
        return branch;
    }
}
