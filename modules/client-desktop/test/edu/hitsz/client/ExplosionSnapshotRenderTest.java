package edu.hitsz.client;

import edu.hitsz.common.protocol.dto.ExplosionSnapshot;
import edu.hitsz.common.protocol.dto.PlayerSnapshot;
import edu.hitsz.common.protocol.dto.WorldSnapshot;
import edu.hitsz.common.protocol.json.WorldSnapshotJsonMapper;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

public class ExplosionSnapshotRenderTest {

    public static void main(String[] args) throws Exception {
        explosionSnapshotSurvivesSnapshotRoundTripAndRenders();
    }

    private static void explosionSnapshotSurvivesSnapshotRoundTripAndRenders() throws Exception {
        WorldSnapshot snapshot = new WorldSnapshot(8L);
        snapshot.addPlayerSnapshot(new PlayerSnapshot("session-local", "player-local", 200, 340, 900, 20, false, 2, null));
        snapshot.addExplosionSnapshot(new ExplosionSnapshot(220, 300, 48, 3));

        WorldSnapshotJsonMapper mapper = new WorldSnapshotJsonMapper();
        WorldSnapshot restored = mapper.fromJson(mapper.toJson(snapshot));

        ClientWorldState state = new ClientWorldState();
        new DefaultSnapshotApplier().apply(restored, state, "session-local");
        assert state.getExplosionSnapshots().size() == 1
                : "Snapshot apply should preserve explosion snapshots for rendering";

        Game game = new Game();
        game.setLocalSessionId("session-local");
        game.applyWorldSnapshot(restored);

        BufferedImage canvas = new BufferedImage(512, 768, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            Method paintExplosions = Game.class.getDeclaredMethod("paintExplosionBursts", java.awt.Graphics.class);
            paintExplosions.setAccessible(true);
            paintExplosions.invoke(game, graphics);
        } finally {
            graphics.dispose();
        }

        assert canvas.getRGB(220, 300) != 0
                : "Explosion rendering should draw a visible burst overlay";
    }
}
