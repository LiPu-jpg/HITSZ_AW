package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;

public class InputCommandsDoNotAdvanceWorldTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);

        long initialTick = roomRuntime.buildSnapshot().getTick();
        int initialX = roomRuntime.findSession("host-session").getPlayerState().getX();
        int initialY = roomRuntime.findSession("host-session").getPlayerState().getY();

        roomRuntime.handleMove("host-session", 300, 520, 1L, 1000L);
        roomRuntime.handleSkill("host-session", "FREEZE", 2L, 1100L);

        assert roomRuntime.buildSnapshot().getTick() == initialTick
                : "Input commands should not advance the authoritative world tick";
        assert roomRuntime.getGamePhase() == GamePhase.LOBBY
                : "Input commands should not start the battle loop";
        assert roomRuntime.findSession("host-session").getPlayerState().getX() == initialX
                : "Lobby input should not move the player before battle starts";
        assert roomRuntime.findSession("host-session").getPlayerState().getY() == initialY
                : "Lobby input should not move the player before battle starts";
        assert roomRuntime.findSession("host-session").getPlayerState().getSelectedSkill() == null
                : "Lobby skill input should not create a selected skill";
    }
}
