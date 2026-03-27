package edu.hitsz.protocol;

import edu.hitsz.common.protocol.MessageType;
import edu.hitsz.common.protocol.ProtocolMessage;
import edu.hitsz.common.protocol.json.JsonMessageCodec;

public class JsonCodecSmokeTest {

    public static void main(String[] args) {
        JsonMessageCodec codec = new JsonMessageCodec();
        ProtocolMessage original = new ProtocolMessage(
                MessageType.INPUT_MOVE,
                "session-123",
                9L,
                1001L,
                "{\"x\":12,\"y\":34}"
        );

        String encoded = codec.encode(original);
        ProtocolMessage decoded = codec.decode(encoded);

        assert decoded.getMessageType() == MessageType.INPUT_MOVE;
        assert "session-123".equals(decoded.getSessionId());
        assert decoded.getSequence() == 9L;
        assert decoded.getTimestamp() == 1001L;
        assert "{\"x\":12,\"y\":34}".equals(decoded.getPayload());
    }
}
