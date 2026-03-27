package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;

import java.lang.reflect.Method;

public class InvalidSelectedSkillFallbackTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", edu.hitsz.common.Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", "NOT_A_SKILL", 0L);

        assert roomRuntime.findSession("host-session").getPlayerState().getSelectedSkill() == null
                : "Unknown selected skills should be ignored once lobby skill selection is removed";
        assert AircraftBranch.STARTER_BLUE == invokeAircraftBranch(roomRuntime.findSession("host-session").getPlayerState())
                : "Unknown selected skills should still leave the player on starter-blue";
        assert roomRuntime.buildSnapshot().getPlayerSnapshots().get(0).getSelectedSkill() == null
                : "Snapshot creation should remain safe when clients send unknown skill values";
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
