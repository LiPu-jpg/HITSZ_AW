package edu.hitsz.server;

import edu.hitsz.common.Difficulty;
import edu.hitsz.common.GameConstants;

import java.lang.reflect.Field;

public class TargetFollowMovementTest {

    public static void main(String[] args) {
        RoomRuntime roomRuntime = new RoomRuntime("ROOM-1", "host-session", Difficulty.NORMAL);
        roomRuntime.addOrReconnectPlayer("host-session", "player-local", 0L);
        roomRuntime.updateReady("host-session", true);
        roomRuntime.startRoundIfHost("host-session");

        PlayerSession session = roomRuntime.findSession("host-session");
        PlayerRuntimeState playerState = session.getPlayerState();
        int startX = playerState.getX();
        int startY = playerState.getY();
        int targetX = Math.min(GameConstants.WINDOW_WIDTH - 60, startX + 220);
        int targetY = Math.max(60, startY - 160);

        roomRuntime.handleMove("host-session", targetX, targetY, 1L, 1000L);

        assert playerState.getX() == startX
                : "Target-based input should not instantly change the player position";
        assert playerState.getY() == startY
                : "Target-based input should not instantly change the player position";
        assert readIntField(playerState, "targetX") == targetX
                : "Move input should update the authoritative target point";
        assert readIntField(playerState, "targetY") == targetY
                : "Move input should update the authoritative target point";

        roomRuntime.tick(1040L, 10_000L);
        int afterFirstTickX = playerState.getX();
        int afterFirstTickY = playerState.getY();

        assert afterFirstTickX > startX
                : "The authoritative tick should move the player toward the target";
        assert afterFirstTickY < startY
                : "The authoritative tick should move the player toward the target";

        int stopRadius = readIntConstant("PLAYER_STOP_RADIUS");
        int moveSpeed = readIntConstant("PLAYER_BASE_MOVE_SPEED");
        for (int i = 0; i < 120; i++) {
            roomRuntime.tick(1080L + i * 40L, 10_000L);
        }

        int endX = playerState.getX();
        int endY = playerState.getY();
        int distanceX = Math.abs(targetX - endX);
        int distanceY = Math.abs(targetY - endY);

        assert distanceX <= stopRadius && distanceY <= stopRadius
                : "The player should settle within the configured stop radius";

        roomRuntime.tick(6000L, 10_000L);
        assert playerState.getX() == endX && playerState.getY() == endY
                : "Once inside the stop radius, the player should stop moving";
        assert Math.abs(afterFirstTickX - startX) <= moveSpeed
                : "Movement should advance by the configured starter-blue move speed, not teleport";
        assert Math.abs(afterFirstTickY - startY) <= moveSpeed
                : "Movement should advance by the configured starter-blue move speed, not teleport";
    }

    private static int readIntField(PlayerRuntimeState playerState, String fieldName) {
        try {
            Field field = PlayerRuntimeState.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(playerState);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Expected PlayerRuntimeState to expose " + fieldName, e);
        }
    }

    private static int readIntConstant(String fieldName) {
        try {
            Field field = GameplayBalance.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Expected GameplayBalance to expose " + fieldName, e);
        }
    }
}
