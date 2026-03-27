package edu.hitsz.client;

import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

public class LobbyOverlayLayoutTest {

    public static void main(String[] args) throws Exception {
        Game game = new Game();
        game.setLocalSessionId("session-guest");

        WorldSnapshot snapshot = new WorldSnapshot(5L);
        snapshot.setGameStarted(false);
        snapshot.setReadyPlayerCount(1);
        snapshot.setConnectedPlayerCount(2);
        snapshot.setDifficulty("HARD");
        snapshot.setRoomCode("654321");
        snapshot.setHostSessionId("session-host-player-very-long");
        snapshot.setTotalScore(180);
        snapshot.setNextBossScoreThreshold(320);
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-host-player-very-long", "player-host", 200, 300, 1000, 50, true, 2, "BOMB"));
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-guest", "player-guest", 260, 340, 900, 66, false, 3, "FREEZE"));
        game.applyWorldSnapshot(snapshot);

        String[] lines = invokeLines(game, "buildLobbyOverlayLines");
        assert lines.length >= 3 : "Lobby overlay should split state into multiple readable lines";

        BufferedImage canvas = new BufferedImage(512, 768, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 16));
        FontMetrics metrics = graphics.getFontMetrics();

        for (String line : lines) {
            assert metrics.stringWidth(line) <= 330
                    : "Lobby overlay line should fit inside the room card: " + line;
        }
        graphics.dispose();
    }

    private static String[] invokeLines(Game game, String methodName) throws Exception {
        Method method = Game.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return (String[]) method.invoke(game);
    }
}
