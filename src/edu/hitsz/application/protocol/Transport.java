package edu.hitsz.application.protocol;

public interface Transport {

    void start();

    void stop();

    void send(ProtocolMessage message);

    void setListener(ProtocolMessageListener listener);
}
