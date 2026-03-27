package edu.hitsz.server.command;

public class MoveCommand {

    private final String sessionId;
    private final int x;
    private final int y;
    private final long sequence;
    private final long timestamp;

    public MoveCommand(String sessionId, int x, int y, long sequence, long timestamp) {
        this.sessionId = sessionId;
        this.x = x;
        this.y = y;
        this.sequence = sequence;
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public long getSequence() {
        return sequence;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
