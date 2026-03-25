package edu.hitsz.application.protocol.json;

import edu.hitsz.application.protocol.MessageCodec;
import edu.hitsz.application.protocol.MessageType;
import edu.hitsz.application.protocol.ProtocolMessage;

public class JsonMessageCodec implements MessageCodec {

    @Override
    public String encode(ProtocolMessage message) {
        StringBuilder builder = new StringBuilder();
        builder.append('{')
                .append("\"messageType\":").append(SimpleJsonSupport.quote(message.getMessageType().name())).append(',')
                .append("\"sessionId\":").append(SimpleJsonSupport.quote(message.getSessionId())).append(',')
                .append("\"sequence\":").append(message.getSequence()).append(',')
                .append("\"timestamp\":").append(message.getTimestamp()).append(',')
                .append("\"payload\":").append(SimpleJsonSupport.normalizePayload(message.getPayload()))
                .append('}');
        return builder.toString();
    }

    @Override
    public ProtocolMessage decode(String raw) {
        String payload = SimpleJsonSupport.extractJsonValue(raw, "payload");
        if (payload != null && payload.startsWith("\"")) {
            payload = SimpleJsonSupport.unquote(payload);
        }
        if ("null".equals(payload)) {
            payload = null;
        }
        return new ProtocolMessage(
                MessageType.valueOf(SimpleJsonSupport.extractString(raw, "messageType")),
                SimpleJsonSupport.extractString(raw, "sessionId"),
                SimpleJsonSupport.extractLong(raw, "sequence"),
                SimpleJsonSupport.extractLong(raw, "timestamp"),
                payload
        );
    }
}
