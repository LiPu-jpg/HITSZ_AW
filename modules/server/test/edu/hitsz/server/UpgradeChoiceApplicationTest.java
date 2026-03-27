package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.BranchUpgradeChoice;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;

public class UpgradeChoiceApplicationTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);
        roomRuntime.findSession("host-session").getPlayerState().setSelectedSkill("FREEZE");
        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");

        PlayerSession session = roomRuntime.findSession("host-session");
        ServerWorldState worldState = extractWorldState(roomRuntime);
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
        roomRuntime.tick(nowMillis + 300L, 10_000L);

        assert roomRuntime.getGamePhase() == GamePhase.UPGRADE_SELECTION
                : "Later boss defeats should open the upgrade selection phase";
        assert session.getPlayerState().getAvailableUpgradeChoices().contains(BranchUpgradeChoice.LASER_DAMAGE)
                : "Player should receive upgrade choices during later upgrade selection";

        roomRuntime.handleUpgradeChoice(
                "host-session",
                BranchUpgradeChoice.LASER_DAMAGE.name(),
                1L,
                roomRuntime.getChapterProgressionState().getFlashUntilMillis()
        );

        assert session.getPlayerState().getSelectedUpgradeChoice() == BranchUpgradeChoice.LASER_DAMAGE
                : "Submitted upgrade choice should be stored on the player state";
        assert session.getPlayerState().getBulletPowerUpgradeLevel() == 1
                : "Choosing LASER_DAMAGE should modify the player's build";
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
