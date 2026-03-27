package edu.hitsz.client;

import edu.hitsz.client.basic.AbstractFlyingObject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class RenderSizingSplitTest {

    public static void main(String[] args) throws Exception {
        paintUsesRenderSizeNotCollisionSize();
    }

    private static void paintUsesRenderSizeNotCollisionSize() throws Exception {
        Game game = new Game();
        TestFlyingObject object = new TestFlyingObject(100, 100, 20, 20, 40, 40);

        BufferedImage canvas = new BufferedImage(240, 240, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            Method paintMethod = Game.class.getDeclaredMethod("paintImageWithPositionRevised", java.awt.Graphics.class, List.class);
            paintMethod.setAccessible(true);
            paintMethod.invoke(game, graphics, Collections.singletonList(object));
        } finally {
            graphics.dispose();
        }

        assert canvas.getRGB(115, 100) == Color.RED.getRGB()
                : "Draw should use render size and reach outside collision bounds";
        assert canvas.getRGB(70, 100) != Color.RED.getRGB()
                : "Draw should not be driven by collision size";
    }

    private static final class TestFlyingObject extends AbstractFlyingObject {

        private final BufferedImage image;

        private TestFlyingObject(int x, int y, int collisionWidth, int collisionHeight, int renderWidth, int renderHeight) throws Exception {
            super(x, y, 0, 0);
            this.width = collisionWidth;
            this.height = collisionHeight;
            this.image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            this.image.setRGB(0, 0, Color.RED.getRGB());
            Method setRenderSize = AbstractFlyingObject.class.getMethod("setRenderSize", int.class, int.class);
            setRenderSize.invoke(this, renderWidth, renderHeight);
        }

        @Override
        public BufferedImage getImage() {
            return image;
        }
    }
}
