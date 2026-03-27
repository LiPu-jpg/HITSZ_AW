package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.GameConstants;

public class OffscreenMoveTargetClampedTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);
        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");

        PlayerSession session = roomRuntime.findSession("host-session");
        PlayerRuntimeState playerState = session.getPlayerState();

        roomRuntime.handleMove("host-session", -100, 900, 1L, 1000L);

        int minX = EntitySizing.HERO_WIDTH / 2;
        int maxX = GameConstants.WINDOW_WIDTH - EntitySizing.HERO_WIDTH / 2;
        int minY = EntitySizing.HERO_HEIGHT / 2;
        int maxY = GameConstants.WINDOW_HEIGHT - EntitySizing.HERO_HEIGHT / 2;

        assert playerState.getTargetX() >= minX
                : "Authoritative target X should be clamped to the arena";
        assert playerState.getTargetY() <= maxY
                : "Authoritative target Y should be clamped to the arena";

        for (int i = 0; i < 200; i++) {
            roomRuntime.tick(1040L + i * 40L, 10_000L);
        }

        assert playerState.getX() >= minX && playerState.getX() <= maxX
                : "Player X should remain inside the arena after target-follow movement";
        assert playerState.getY() >= minY && playerState.getY() <= maxY
                : "Player Y should remain inside the arena after target-follow movement";
    }
}
