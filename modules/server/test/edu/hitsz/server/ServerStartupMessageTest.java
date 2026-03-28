package edu.hitsz.server;

import java.net.BindException;

public class ServerStartupMessageTest {

    public static void main(String[] args) {
        bindFailureMessageUsesChineseAndPortHint();
    }

    private static void bindFailureMessageUsesChineseAndPortHint() {
        String message = ServerStartupMessage.formatStartFailure(20123, new IllegalStateException("x", new BindException("Address already in use")));
        assert message.contains("端口 20123 已被占用")
                : "Bind failure message should explain the occupied port in Chinese";
        assert message.contains("ServerMain 20124")
                : "Bind failure message should suggest using another port";
    }
}
