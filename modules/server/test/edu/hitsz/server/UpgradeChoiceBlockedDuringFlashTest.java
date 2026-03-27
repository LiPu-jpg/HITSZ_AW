package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.BranchUpgradeChoice;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;

import java.lang.reflect.Field;

public class UpgradeChoiceBlockedDuringFlashTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);
        roomRuntime.findSession("host-session").getPlayerState().setSelectedSkill("FREEZE");
        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");

        ServerWorldState worldState = extractWorldState(roomRuntime);
        PlayerSession session = roomRuntime.findSession("host-session");
        long nowMillis = System.currentTimeMillis();
        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 0));

        worldState.syncProgressionState(nowMillis);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(nowMillis + 100L);

        assert roomRuntime.getGamePhase() == GamePhase.BRANCH_SELECTION
                : "Precondition failed: first boss defeat should open branch selection";
        roomRuntime.handleBranchChoice("host-session", AircraftBranch.RED_SPEED.name(), 1L, nowMillis + 120L);
        roomRuntime.tick(nowMillis + 120L, 10_000L);

        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 1));
        worldState.syncProgressionState(nowMillis + 200L);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(nowMillis + 300L);

        long flashUntilMillis = roomRuntime.getChapterProgressionState().getFlashUntilMillis();
        assert roomRuntime.getGamePhase() == GamePhase.UPGRADE_SELECTION
                : "Precondition failed: room should be in upgrade selection";

        roomRuntime.handleUpgradeChoice("host-session", BranchUpgradeChoice.LASER_DAMAGE.name(), 1L, flashUntilMillis - 1L);
        assert session.getPlayerState().getSelectedUpgradeChoice() == null
                : "Server should reject upgrade submissions before the flash window ends";

        roomRuntime.handleUpgradeChoice("host-session", BranchUpgradeChoice.LASER_DAMAGE.name(), 2L, flashUntilMillis);
        assert session.getPlayerState().getSelectedUpgradeChoice() == BranchUpgradeChoice.LASER_DAMAGE
                : "Server should accept upgrade submissions once the flash window has ended";
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
