package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.ChapterId;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.UpgradeChoice;

import java.lang.reflect.Method;

public class BranchSelectionOnlyHappensOnceTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);
        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");

        ServerWorldState worldState = extractWorldState(roomRuntime);
        PlayerSession session = roomRuntime.findSession("host-session");
        long nowMillis = 3_000L;

        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 0));
        worldState.syncProgressionState(nowMillis);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(nowMillis + 100L);

        assert roomRuntime.getGamePhase() == GamePhase.BRANCH_SELECTION
                : "Precondition failed: first boss defeat should open branch selection";

        invokeHandleBranchChoice(roomRuntime, "host-session", AircraftBranch.BLACK_HEAVY.name(), 1L, nowMillis + 200L);
        roomRuntime.tick(nowMillis + 200L, 10_000L);

        assert roomRuntime.getGamePhase() == GamePhase.BATTLE
                : "Battle should resume after branch selection is completed";
        assert roomRuntime.getChapterId() == ChapterId.CH2
                : "Room should advance chapters after first-boss branch selection";
        assert session.getPlayerState().isBranchUnlocked()
                : "Branch selection should unlock the player's branch permanently for the round";
        assert session.getPlayerState().getAircraftBranch() == AircraftBranch.BLACK_HEAVY
                : "Branch selection should replace starter-blue with the chosen branch";

        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 1));
        worldState.syncProgressionState(nowMillis + 500L);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(nowMillis + 600L);

        assert roomRuntime.getGamePhase() == GamePhase.UPGRADE_SELECTION
                : "Later boss defeats should use normal upgrade selection after the first branch choice";
        assert session.getPlayerState().getAvailableUpgradeChoices().contains(UpgradeChoice.BULLET_POWER)
                : "Later boss defeats should still grant the normal upgrade choices";
    }

    private static void invokeHandleBranchChoice(
            RoomRuntime roomRuntime,
            String sessionId,
            String branch,
            long sequence,
            long timestamp
    ) {
        try {
            Method method = RoomRuntime.class.getMethod(
                    "handleBranchChoice",
                    String.class,
                    String.class,
                    long.class,
                    long.class
            );
            method.invoke(roomRuntime, sessionId, branch, sequence, timestamp);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("RoomRuntime should accept branch choices during branch selection", e);
        }
    }

    private static ServerWorldState extractWorldState(RoomRuntime roomRuntime) {
        try {
            java.lang.reflect.Field field = RoomRuntime.class.getDeclaredField("worldState");
            field.setAccessible(true);
            return (ServerWorldState) field.get(roomRuntime);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("RoomRuntime should retain a ServerWorldState", e);
        }
    }
}
