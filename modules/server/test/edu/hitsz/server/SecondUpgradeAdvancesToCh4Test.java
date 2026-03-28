package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.BranchUpgradeChoice;
import edu.hitsz.common.ChapterId;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;

import java.lang.reflect.Field;

public class SecondUpgradeAdvancesToCh4Test {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);
        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");

        ServerWorldState worldState = extractWorldState(roomRuntime);
        PlayerSession session = roomRuntime.findSession("host-session");
        long nowMillis = 1_000L;

        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 0));
        worldState.syncProgressionState(nowMillis);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(nowMillis + 100L);
        roomRuntime.handleBranchChoice("host-session", AircraftBranch.RED_SPEED.name(), 1L, nowMillis + 200L);
        roomRuntime.tick(nowMillis + 200L, 10_000L);
        assert roomRuntime.getChapterId() == ChapterId.CH2
                : "After first boss branch selection, the room should enter CH2";

        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 1));
        worldState.syncProgressionState(nowMillis + 300L);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(nowMillis + 400L);
        long firstUpgradeFlashUntilMillis = roomRuntime.getChapterProgressionState().getFlashUntilMillis();
        roomRuntime.handleUpgradeChoice("host-session", BranchUpgradeChoice.LASER_DAMAGE.name(), 2L, firstUpgradeFlashUntilMillis + 10L);
        roomRuntime.tick(firstUpgradeFlashUntilMillis + 10L, 10_000L);
        assert roomRuntime.getChapterId() == ChapterId.CH3
                : "After the first upgrade selection, the room should enter CH3";

        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 2));
        worldState.syncProgressionState(nowMillis + 500L);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(nowMillis + 600L);

        assert roomRuntime.getGamePhase() == GamePhase.UPGRADE_SELECTION
                : "Second post-boss pause should still be an upgrade selection";

        long secondUpgradeFlashUntilMillis = roomRuntime.getChapterProgressionState().getFlashUntilMillis();
        roomRuntime.handleUpgradeChoice("host-session", BranchUpgradeChoice.LASER_WIDTH.name(), 3L, secondUpgradeFlashUntilMillis + 10L);
        roomRuntime.tick(secondUpgradeFlashUntilMillis + 10L, 10_000L);

        assert roomRuntime.isGameStarted()
                : "Second upgrade should continue the round instead of returning to lobby";
        assert roomRuntime.getGamePhase() == GamePhase.BATTLE
                : "Second upgrade should resume battle";
        assert roomRuntime.getChapterId() == ChapterId.CH4
                : "After the second upgrade selection, the room should enter CH4";
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
