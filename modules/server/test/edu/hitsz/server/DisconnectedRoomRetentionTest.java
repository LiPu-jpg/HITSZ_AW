package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;

public class DisconnectedRoomRetentionTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);
        roomRuntime.findSession("host-session").getPlayerState().setSelectedSkill("FREEZE");
        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");

        roomRuntime.markDisconnected("host-session", 100L);
        roomRuntime.tick(200L, 10_000L);

        assert roomRuntime.isGameStarted() : "Short full-room disconnects should not immediately wipe the round";
        assert roomRuntime.getGamePhase() == GamePhase.BATTLE
                : "Retained disconnected rooms should preserve the in-round phase";
    }
}
