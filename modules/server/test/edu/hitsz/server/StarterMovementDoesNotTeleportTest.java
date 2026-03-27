package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GameConstants;

public class StarterMovementDoesNotTeleportTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);
        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");

        PlayerSession session = roomRuntime.findSession("host-session");
        PlayerRuntimeState playerState = session.getPlayerState();
        int initialX = playerState.getX();
        int initialY = playerState.getY();
        int targetX = Math.min(GameConstants.WINDOW_WIDTH - 60, initialX + 160);
        int targetY = Math.max(60, initialY - 120);

        roomRuntime.handleMove("host-session", targetX, targetY, 1L, 1000L);

        assert playerState.getX() == initialX
                : "Move input should not teleport the player immediately";
        assert playerState.getY() == initialY
                : "Move input should not teleport the player immediately";

        roomRuntime.tick(1040L, 10_000L);

        assert playerState.getX() != targetX || playerState.getY() != targetY
                : "The plane should advance toward the target during the authoritative tick, not jump there";
        assert playerState.getX() > initialX
                : "The plane should move toward the requested target";
        assert playerState.getY() < initialY
                : "The plane should move toward the requested target";
    }
}
