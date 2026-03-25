package edu.hitsz.application.protocol;

public class ProtocolMessage {

    private final MessageType messageType;
    private final String sessionId;
    private final long sequence;
    private final long timestamp;
    private final String payload;

    public ProtocolMessage(
            MessageType messageType,
            String sessionId,
            long sequence,
            long timestamp,
            String payload
    ) {
        this.messageType = messageType;
        this.sessionId = sessionId;
        this.sequence = sequence;
        this.timestamp = timestamp;
        this.payload = payload;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getSequence() {
        return sequence;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getPayload() {
        return payload;
    }
}
