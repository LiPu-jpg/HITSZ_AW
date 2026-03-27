package edu.hitsz.client;

import edu.hitsz.common.protocol.dto.LaserSnapshot;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

public class LaserSnapshotRenderTest {

    public static void main(String[] args) throws Exception {
        WorldSnapshot snapshot = new WorldSnapshot(7L);
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-local", "player-local", 200, 340, 900, 20, false, 2, null));
        snapshot.addLaserSnapshot(new LaserSnapshot("session-local", 200, 298, -Math.PI / 2.0, 24, 220, 4, 45));

        WorldSnapshotJsonMapper mapper = new WorldSnapshotJsonMapper();
        WorldSnapshot restored = mapper.fromJson(mapper.toJson(snapshot));

        ClientWorldState state = new ClientWorldState();
        new DefaultSnapshotApplier().apply(restored, state, "session-local");
        assert state.getActiveLasers().size() == 1
                : "Snapshot apply should preserve active lasers for rendering";

        Game game = new Game();
        game.setLocalSessionId("session-local");
        game.applyWorldSnapshot(restored);

        BufferedImage canvas = new BufferedImage(512, 768, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            Method paintLaserBeams = Game.class.getDeclaredMethod("paintLaserBeams", java.awt.Graphics.class);
            paintLaserBeams.setAccessible(true);
            paintLaserBeams.invoke(game, graphics);
        } finally {
            graphics.dispose();
        }

        assert canvas.getRGB(200, 220) != 0
                : "Laser rendering should draw a visible beam overlay";
    }
}
