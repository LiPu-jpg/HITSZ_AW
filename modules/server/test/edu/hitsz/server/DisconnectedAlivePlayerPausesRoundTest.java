package edu.hitsz.server;

import edu.hitsz.common.Difficulty;

import java.lang.reflect.Field;

public class DisconnectedAlivePlayerPausesRoundTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "host-player", 0L);
        roomRuntime.findSession("host-session").getPlayerState().setSelectedSkill("FREEZE");
        roomRuntime.addOrReconnectPlayer("guest-session", "guest-player", 0L);
        roomRuntime.findSession("guest-session").getPlayerState().setSelectedSkill("BOMB");
        roomRuntime.updateReady("host-session", true);
        roomRuntime.updateReady("guest-session", true);
        roomRuntime.startRoundIfHost("host-session");

        ServerWorldState worldState = extractWorldState(roomRuntime);
        PlayerSession host = roomRuntime.findSession("host-session");
        PlayerSession guest = roomRuntime.findSession("guest-session");

        host.markDisconnected(100L);
        guest.getPlayerState().setHp(0);
        long tickBefore = worldState.getTick();

        roomRuntime.tick(200L, 10_000L);

        assert roomRuntime.isGameStarted() : "Room should stay active while waiting for a retained alive player to reconnect";
        assert worldState.getTick() == tickBefore : "World loop should pause when no connected alive players remain";
    }

    private static ServerWorldState extractWorldState(RoomRuntime roomRuntime) {
        try {
            Field field = RoomRuntime.class.getDeclaredField("worldState");
            field.setAccessible(true);
            return (ServerWorldState) field.get(roomRuntime);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("RoomRuntime should retain a ServerWorldState", e);
        }
    }
}
