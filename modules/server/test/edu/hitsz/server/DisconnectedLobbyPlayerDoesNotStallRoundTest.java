package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;

public class DisconnectedLobbyPlayerDoesNotStallRoundTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "host-player", 0L);
        roomRuntime.findSession("host-session").getPlayerState().setSelectedSkill("FREEZE");
        roomRuntime.addOrReconnectPlayer("guest-session", "guest-player", 0L);
        roomRuntime.findSession("guest-session").getPlayerState().setSelectedSkill("BOMB");
        roomRuntime.markDisconnected("guest-session", 50L);
        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");

        roomRuntime.findSession("host-session").getPlayerState().setHp(0);
        roomRuntime.tick(200L, 10_000L);

        assert !roomRuntime.isGameStarted() : "Disconnected lobby players should not keep a finished round alive";
        assert roomRuntime.getGamePhase() == GamePhase.LOBBY
                : "Round should return to lobby when no round participants remain alive";
    }
}
