package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.ChapterId;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;

import java.lang.reflect.Method;

public class AllPlayersMustChooseBranchTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-host", 0L);
        roomRuntime.addOrReconnectPlayer("guest-session", "player-guest", 0L);
        roomRuntime.updateReady("host-session", true);
        roomRuntime.updateReady("guest-session", true);
        roomRuntime.startRoundIfHost("host-session");

        ServerWorldState worldState = extractWorldState(roomRuntime);
        long nowMillis = 2_000L;

        roomRuntime.findSession("host-session").getPlayerState()
                .setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 0));
        worldState.syncProgressionState(nowMillis);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(nowMillis + 100L);

        assert roomRuntime.getGamePhase() == GamePhase.BRANCH_SELECTION
                : "Precondition failed: first boss defeat should pause in branch selection";

        invokeHandleBranchChoice(roomRuntime, "host-session", AircraftBranch.RED_SPEED.name(), 1L, nowMillis + 200L);
        roomRuntime.tick(nowMillis + 200L, 10_000L);

        assert roomRuntime.getGamePhase() == GamePhase.BRANCH_SELECTION
                : "Battle should stay blocked until every alive player chooses a branch";
        assert roomRuntime.findSession("host-session").getPlayerState().isBranchUnlocked()
                : "Submitting a branch should unlock the choosing player immediately";
        assert roomRuntime.findSession("host-session").getPlayerState().getAircraftBranch() == AircraftBranch.RED_SPEED
                : "Submitting a branch should lock the choosing player to that branch";
        assert !roomRuntime.findSession("guest-session").getPlayerState().isBranchUnlocked()
                : "Players who have not chosen yet should remain locked on starter-blue";

        invokeHandleBranchChoice(roomRuntime, "guest-session", AircraftBranch.GREEN_DEFENSE.name(), 2L, nowMillis + 300L);
        roomRuntime.tick(nowMillis + 300L, 10_000L);

        assert roomRuntime.getGamePhase() == GamePhase.BATTLE
                : "Battle should resume after every alive player has chosen a branch";
        assert roomRuntime.getChapterId() == ChapterId.CH2
                : "Completing first-boss branch selection should still advance the chapter";
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
