package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;

import java.lang.reflect.Field;

public class UpgradeSelectionBlocksSkillTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);
        roomRuntime.findSession("host-session").getPlayerState().setSelectedSkill("FREEZE");
        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");

        ServerWorldState worldState = extractWorldState(roomRuntime);
        PlayerSession session = roomRuntime.findSession("host-session");
        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 0));

        worldState.syncProgressionState(1000L);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(1100L);

        assert roomRuntime.getGamePhase() == GamePhase.BRANCH_SELECTION
                : "Precondition failed: first boss defeat should open branch selection";
        roomRuntime.handleBranchChoice("host-session", AircraftBranch.RED_SPEED.name(), 1L, 1120L);
        roomRuntime.tick(1120L, 10_000L);

        session.getPlayerState().setSelectedSkill("FREEZE");
        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 1));
        worldState.syncProgressionState(1200L);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(1300L);

        assert roomRuntime.getGamePhase() == GamePhase.UPGRADE_SELECTION
                : "Precondition failed: room should be in upgrade selection";

        roomRuntime.handleSkill("host-session", "FREEZE", 1L, 1200L);

        assert !worldState.getWorldEffectState().isFrozen(1200L)
                : "Skills should be blocked while the room is waiting for upgrade choices";
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
