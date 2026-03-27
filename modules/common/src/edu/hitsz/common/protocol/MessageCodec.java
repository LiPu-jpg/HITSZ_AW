package edu.hitsz.common.protocol;

public interface MessageCodec {

    String encode(ProtocolMessage message);

    ProtocolMessage decode(String raw);
}
