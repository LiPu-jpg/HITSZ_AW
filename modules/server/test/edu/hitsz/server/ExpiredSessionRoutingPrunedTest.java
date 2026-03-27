package edu.hitsz.server;

import edu.hitsz.common.Difficulty;

public class ExpiredSessionRoutingPrunedTest {

    public static void main(String[] args) {
        RoomRegistry roomRegistry = new RoomRegistry();
        RoomRuntime roomRuntime = roomRegistry.createRoom(
                "host-session",
                "host-player",
                Difficulty.NORMAL,
                0L
        );
        roomRegistry.joinRoom("guest-session", "guest-player", roomRuntime.getRoomCode(), 0L);

        roomRuntime.markDisconnected("guest-session", 100L);
        roomRuntime.tick(10_200L, 10_000L);

        assert roomRuntime.findSession("guest-session") == null
                : "Precondition failed: expired session should be removed from the room";
        assert roomRegistry.findBySession("guest-session") == null
                : "Expired sessions should also be pruned from room routing";
    }
}
