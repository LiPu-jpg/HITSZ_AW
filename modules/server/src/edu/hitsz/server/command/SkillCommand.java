package edu.hitsz.server.command;

public class SkillCommand {

    private final String sessionId;
    private final String skillType;
    private final long sequence;
    private final long timestamp;

    public SkillCommand(String sessionId, String skillType, long sequence, long timestamp) {
        this.sessionId = sessionId;
        this.skillType = skillType;
        this.sequence = sequence;
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getSkillType() {
        return skillType;
    }

    public long getSequence() {
        return sequence;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
