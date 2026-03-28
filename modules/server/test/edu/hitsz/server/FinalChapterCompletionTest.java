package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.BranchUpgradeChoice;
import edu.hitsz.common.ChapterId;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;

import java.lang.reflect.Field;

public class FinalChapterCompletionTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);
        roomRuntime.findSession("host-session").getPlayerState().setSelectedSkill("FREEZE");
        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");

        ServerWorldState worldState = extractWorldState(roomRuntime);
        worldState.getChapterProgressionState().markFirstBossBranchSelectionCompleted();
        worldState.getChapterProgressionState().advanceToNextChapter();
        worldState.getChapterProgressionState().advanceToNextChapter();
        worldState.getChapterProgressionState().advanceToNextChapter();
        worldState.getChapterProgressionState().advanceToNextChapter();
        assert roomRuntime.getChapterId() == ChapterId.CH5 : "Precondition failed: room should be at the terminal chapter";

        PlayerSession session = roomRuntime.findSession("host-session");
        session.getPlayerState().applyBranchChoice(AircraftBranch.RED_SPEED);
        long nowMillis = System.currentTimeMillis();
        session.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 0));

        worldState.syncProgressionState(nowMillis);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(nowMillis + 100L);

        long flashUntilMillis = roomRuntime.getChapterProgressionState().getFlashUntilMillis();
        roomRuntime.tick(flashUntilMillis, 10_000L);
        assert roomRuntime.getGamePhase() == GamePhase.UPGRADE_SELECTION
                : "Terminal chapter should also wait for upgrade submission before ending the round";

        roomRuntime.handleUpgradeChoice("host-session", BranchUpgradeChoice.LASER_DAMAGE.name(), 1L, flashUntilMillis + 10L);
        roomRuntime.tick(flashUntilMillis + 10L, 10_000L);

        assert !roomRuntime.isGameStarted() : "Room should return to lobby after the terminal chapter completes";
        assert roomRuntime.getGamePhase() == GamePhase.LOBBY : "Terminal chapter completion should end the round";
        assert roomRuntime.getChapterId() == ChapterId.CH1 : "Lobby reset should return chapter selection to CH1";
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
