package edu.hitsz.server;

import edu.hitsz.common.Difficulty;

public class DisconnectedHostReassignmentTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "host-player", 0L);
        roomRuntime.findSession("host-session").getPlayerState().setSelectedSkill("FREEZE");
        roomRuntime.addOrReconnectPlayer("guest-session", "guest-player", 0L);
        roomRuntime.findSession("guest-session").getPlayerState().setSelectedSkill("BOMB");

        roomRuntime.markDisconnected("host-session", 100L);
        roomRuntime.tick(200L, 10_000L);

        assert "guest-session".equals(roomRuntime.getHostSessionId())
                : "Connected players should inherit host authority when the current host disconnects";
    }
}
