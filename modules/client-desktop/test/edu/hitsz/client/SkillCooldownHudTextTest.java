package edu.hitsz.client;

import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class SkillCooldownHudTextTest {

    public static void main(String[] args) {
        Game game = new Game();
        game.setLocalSessionId("session-local");

        WorldSnapshot cooldownSnapshot = new WorldSnapshot(1L);
        cooldownSnapshot.addPlayerSnapshot(playerSnapshotWithCooldown(
                "session-local",
                "player-local",
                200,
                300,
                900,
                66,
                false,
                3,
                "FREEZE",
                2500L
        ));
        game.applyWorldSnapshot(roundTrip(cooldownSnapshot));

        assert "技能：冻结（冷却 2.5秒）".equals(skillStatusText(game))
                : "HUD should show remaining skill cooldown";

        WorldSnapshot readySnapshot = new WorldSnapshot(2L);
        readySnapshot.addPlayerSnapshot(playerSnapshotWithCooldown(
                "session-local",
                "player-local",
                200,
                300,
                900,
                66,
                false,
                3,
                "FREEZE",
                0L
        ));
        game.applyWorldSnapshot(roundTrip(readySnapshot));

        assert "技能：冻结（可用）".equals(skillStatusText(game))
                : "HUD should show ready state after cooldown ends";
    }

    private static WorldSnapshot roundTrip(WorldSnapshot snapshot) {
        WorldSnapshotJsonMapper mapper = new WorldSnapshotJsonMapper();
        return mapper.fromJson(mapper.toJson(snapshot));
    }

    private static PlayerSnapshot playerSnapshotWithCooldown(
            String sessionId,
            String playerId,
            int x,
            int y,
            int hp,
            int score,
            boolean ready,
            int level,
            String selectedSkill,
            long cooldownRemainingMillis
    ) {
        try {
            Constructor<PlayerSnapshot> constructor = PlayerSnapshot.class.getConstructor(
                    String.class,
                    String.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class,
                    boolean.class,
                    int.class,
                    String.class,
                    long.class
            );
            return constructor.newInstance(
                    sessionId,
                    playerId,
                    x,
                    y,
                    hp,
                    score,
                    ready,
                    level,
                    selectedSkill,
                    cooldownRemainingMillis
            );
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("PlayerSnapshot should support skill cooldown HUD data", e);
        }
    }

    private static String skillStatusText(Game game) {
        try {
            Method method = Game.class.getDeclaredMethod("buildSkillStatusText");
            method.setAccessible(true);
            return (String) method.invoke(game);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Game should expose skill HUD formatting for cooldown state", e);
        }
    }
}
