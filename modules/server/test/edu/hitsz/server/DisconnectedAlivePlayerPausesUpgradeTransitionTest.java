package edu.hitsz.server;

import edu.hitsz.common.ChapterId;
import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GamePhase;

import java.lang.reflect.Field;

public class DisconnectedAlivePlayerPausesUpgradeTransitionTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "host-player", 0L);
        roomRuntime.findSession("host-session").getPlayerState().setSelectedSkill("FREEZE");
        roomRuntime.addOrReconnectPlayer("guest-session", "guest-player", 0L);
        roomRuntime.findSession("guest-session").getPlayerState().setSelectedSkill("BOMB");
        roomRuntime.updateReady("host-session", true);
        roomRuntime.updateReady("guest-session", true);
        roomRuntime.startRoundIfHost("host-session");

        ServerWorldState worldState = extractWorldState(roomRuntime);
        PlayerSession host = roomRuntime.findSession("host-session");
        PlayerSession guest = roomRuntime.findSession("guest-session");
        long nowMillis = System.currentTimeMillis();

        host.getPlayerState().setScore(ProgressionPolicy.defaultPolicy().bossThreshold(Difficulty.NORMAL, 0));
        worldState.syncProgressionState(nowMillis);
        worldState.getEnemyAircrafts().clear();
        worldState.syncProgressionState(nowMillis + 100L);

        host.markDisconnected(nowMillis + 150L);
        guest.getPlayerState().setHp(0);
        long flashUntilMillis = roomRuntime.getChapterProgressionState().getFlashUntilMillis();

        roomRuntime.tick(flashUntilMillis, 10_000L);

        assert roomRuntime.isGameStarted() : "Room should remain active while waiting for the disconnected alive player";
        assert roomRuntime.getGamePhase() == GamePhase.UPGRADE_SELECTION
                : "Upgrade transition should pause until a retained alive player reconnects";
        assert roomRuntime.getChapterId() == ChapterId.CH1
                : "Chapter should not advance while the only alive player is offline";
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
