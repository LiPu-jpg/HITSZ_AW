package edu.hitsz.server;

import edu.hitsz.common.Difficulty;

public class ReconnectDoesNotChangeSkillMidRoundTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);
        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");
        roomRuntime.findSession("host-session").getPlayerState().setSelectedSkill("FREEZE");

        roomRuntime.markDisconnected("host-session", 100L);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 200L);

        assert "FREEZE".equals(roomRuntime.findSession("host-session").getPlayerState().getSelectedSkill())
                : "Reconnect should preserve the in-round skill loadout";
    }
}
