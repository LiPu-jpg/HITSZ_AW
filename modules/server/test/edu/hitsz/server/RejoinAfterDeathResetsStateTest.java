package edu.hitsz.server;

import edu.hitsz.common.protocol.MessageType;
import edu.hitsz.common.protocol.ProtocolMessage;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Timer;

public class RejoinAfterDeathResetsStateTest {

    public static void main(String[] args) throws Exception {
        LocalAuthorityServer server = new LocalAuthorityServer(0);
        server.start();
        cancelTimer(server);

        invokeHandleMessage(server, new ProtocolMessage(
                MessageType.CREATE_ROOM,
                "session-local",
                1L,
                900L,
                "{\"difficulty\":\"NORMAL\",\"selectedSkill\":\"FREEZE\"}"
        ));

        PlayerSession session = findSession(server, "session-local");
        session.getPlayerState().setHp(0);
        session.getPlayerState().setScore(77);
        session.setReady(true);

        invokeHandleMessage(server, new ProtocolMessage(
                MessageType.HELLO,
                "session-local",
                1L,
                1000L,
                "{}"
        ));

        server.stop();

        assert session.getPlayerState().getHp() == 1000 : "Rejoining after death should respawn the player";
        assert session.getPlayerState().getScore() == 0 : "Rejoining after death should reset the round score";
        assert !session.isReady() : "Rejoining should put the player back into the lobby-ready flow";
    }

    private static PlayerSession findSession(LocalAuthorityServer server, String sessionId) throws Exception {
        Field roomRegistryField = LocalAuthorityServer.class.getDeclaredField("roomRegistry");
        roomRegistryField.setAccessible(true);
        RoomRegistry roomRegistry = (RoomRegistry) roomRegistryField.get(server);
        RoomRuntime room = roomRegistry.findBySession(sessionId);
        return room.findSession(sessionId);
    }

    private static void invokeHandleMessage(LocalAuthorityServer server, ProtocolMessage message) throws Exception {
        Method method = LocalAuthorityServer.class.getDeclaredMethod("handleMessage", ProtocolMessage.class);
        method.setAccessible(true);
        method.invoke(server, message);
    }

    private static void cancelTimer(LocalAuthorityServer server) throws Exception {
        Field timerField = LocalAuthorityServer.class.getDeclaredField("timer");
        timerField.setAccessible(true);
        ((Timer) timerField.get(server)).cancel();
    }
}
