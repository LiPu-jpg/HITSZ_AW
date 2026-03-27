package edu.hitsz.common.protocol.socket;

public interface ServerConnectionListener {

    void onClientDisconnected(String sessionId);
}
