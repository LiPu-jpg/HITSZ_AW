package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.Difficulty;

import java.lang.reflect.Method;

public class StarterBlueDefaultStateTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);

        PlayerSession session = roomRuntime.findSession("host-session");
        session.getPlayerState().setSelectedSkill("BOMB");
        session.getPlayerState().resetForNewRound(320, 640);

        assert session != null : "Room should retain the connected player";
        assert session.getPlayerState().getSelectedSkill() == null
                : "Resetting to starter-blue should clear any previously selected skill";
        assert AircraftBranch.STARTER_BLUE == invokeAircraftBranch(session.getPlayerState())
                : "Starter-blue should be the only initial branch";
        assert roomRuntime.buildSnapshot().getPlayerSnapshots().get(0).getSelectedSkill() == null
                : "Snapshots should not advertise a preselected skill for starter-blue players";
        assert AircraftBranch.STARTER_BLUE == roomRuntime.buildSnapshot().getPlayerSnapshots().get(0).getAircraftBranch()
                : "Snapshots should serialize the starter-blue branch default";
    }

    private static AircraftBranch invokeAircraftBranch(Object playerState) {
        try {
            Method method = playerState.getClass().getMethod("getAircraftBranch");
            return (AircraftBranch) method.invoke(playerState);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("PlayerRuntimeState should expose an aircraft branch", e);
        }
    }
}
