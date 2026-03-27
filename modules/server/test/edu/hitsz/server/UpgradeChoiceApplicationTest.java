package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;
import edu.hitsz.common.UpgradeChoice;

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
        roomRuntime.tick(nowMillis + 150L, 10_000L);

        assert roomRuntime.getGamePhase() == GamePhase.UPGRADE_SELECTION
                : "Boss defeat should open the upgrade selection phase";
        assert session.getPlayerState().getAvailableUpgradeChoices().contains(UpgradeChoice.BULLET_POWER)
                : "Player should receive upgrade choices during upgrade selection";

        roomRuntime.handleUpgradeChoice(
                "host-session",
                UpgradeChoice.BULLET_POWER.name(),
                1L,
                roomRuntime.getChapterProgressionState().getFlashUntilMillis()
        );

        assert session.getPlayerState().getSelectedUpgradeChoice() == UpgradeChoice.BULLET_POWER
                : "Submitted upgrade choice should be stored on the player state";
        assert session.getPlayerState().getBulletPowerUpgradeLevel() == 1
                : "Choosing BULLET_POWER should modify the player's build";
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
