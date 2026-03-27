package edu.hitsz.protocol;

import edu.hitsz.common.protocol.MessageType;
import edu.hitsz.common.protocol.ProtocolMessage;

public class ProtocolEnvelopeTest {

    public static void main(String[] args) {
        ProtocolMessage message = new ProtocolMessage(
                MessageType.INPUT_MOVE,
                "session-123",
                7L,
                1000L,
                "{\"x\":12,\"y\":34}"
        );

        assert "session-123".equals(message.getSessionId());
        assert message.getMessageType() == MessageType.INPUT_MOVE;
    }
}
