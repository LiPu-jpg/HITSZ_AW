package edu.hitsz.server;

import edu.hitsz.common.Difficulty;

public class MidRoundJoinRejectedTest {

    public static void main(String[] args) {
        RoomRegistry roomRegistry = new RoomRegistry();
        RoomRuntime roomRuntime = roomRegistry.createRoom(
                "host-session",
                "host-player",
                Difficulty.NORMAL,
                0L
        );
        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");

        RoomRuntime joinedRoom = roomRegistry.joinRoom(
                "late-session",
                "late-player",
                roomRuntime.getRoomCode(),
                100L
        );

        assert joinedRoom == null : "Room registry should reject new joins after the round has started";
        assert roomRegistry.findBySession("late-session") == null
                : "Rejected mid-round joins should not be bound into room routing";
    }
}
