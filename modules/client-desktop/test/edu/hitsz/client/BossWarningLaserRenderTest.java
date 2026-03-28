package edu.hitsz.client;

import edu.hitsz.common.protocol.dto.LaserSnapshot;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

public class BossWarningLaserRenderTest {

    public static void main(String[] args) throws Exception {
        bossWarningLaserRendersTelegraphTint();
        bossFiringLaserRendersAggressiveTint();
    }

    private static void bossWarningLaserRendersTelegraphTint() throws Exception {
        BufferedImage canvas = renderLaser(new LaserSnapshot(
                "boss",
                220,
                120,
                Math.PI / 2.0,
                12,
                500,
                30,
                0,
                "BOSS_WARNING",
                0.5
        ));
        assert canvas.getRGB(220, 280) != 0 : "Boss warning laser should render a visible telegraph line";
    }

    private static void bossFiringLaserRendersAggressiveTint() throws Exception {
        BufferedImage canvas = renderLaser(new LaserSnapshot(
                "boss",
                220,
                120,
                Math.PI / 2.0,
                24,
                500,
                4,
                80,
                "BOSS_FIRING",
                1.0
        ));
        assert canvas.getRGB(220, 280) != 0 : "Boss firing laser should render a visible active beam";
    }

    private static BufferedImage renderLaser(LaserSnapshot laserSnapshot) throws Exception {
        WorldSnapshot snapshot = new WorldSnapshot(17L);
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-local", "player-local", 220, 680, 900, 20, false, 2, null));
        snapshot.addLaserSnapshot(laserSnapshot);

        Game game = new Game();
        game.setLocalSessionId("session-local");
        game.applyWorldSnapshot(snapshot);

        BufferedImage canvas = new BufferedImage(512, 768, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            Method paintLaserBeams = Game.class.getDeclaredMethod("paintLaserBeams", java.awt.Graphics.class);
            paintLaserBeams.setAccessible(true);
            paintLaserBeams.invoke(game, graphics);
        } finally {
            graphics.dispose();
        }
        return canvas;
    }
}
