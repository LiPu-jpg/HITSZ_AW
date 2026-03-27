package edu.hitsz.client;

import edu.hitsz.client.aircraft.MobEnemy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class ScaledDrawLayoutTest {

    public static void main(String[] args) throws Exception {
        paintShouldRespectConfiguredRenderSize();
    }

    private static void paintShouldRespectConfiguredRenderSize() throws Exception {
        BufferedImage originalMobImage = ImageManager.MOB_ENEMY_IMAGE;
        Map<String, BufferedImage> imageMap = imageMap();
        BufferedImage originalMappedMobImage = imageMap.get(MobEnemy.class.getName());
        ImageManager.MOB_ENEMY_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        ImageManager.MOB_ENEMY_IMAGE.setRGB(0, 0, Color.RED.getRGB());
        imageMap.put(MobEnemy.class.getName(), ImageManager.MOB_ENEMY_IMAGE);

        try {
            Game game = new Game();
            MobEnemy mobEnemy = new MobEnemy(100, 100, 0, 10, 30);

            BufferedImage canvas = new BufferedImage(240, 240, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = canvas.createGraphics();
            try {
                Method method = Game.class.getDeclaredMethod("paintImageWithPositionRevised", java.awt.Graphics.class, java.util.List.class);
                method.setAccessible(true);
                method.invoke(game, graphics, java.util.Collections.singletonList(mobEnemy));
            } finally {
                graphics.dispose();
            }

            int sampleX = 130;
            int sampleY = 100;
            assert canvas.getRGB(sampleX, sampleY) == Color.RED.getRGB()
                    : "Paint should scale using configured render size, not raw image size";
        } finally {
            ImageManager.MOB_ENEMY_IMAGE = originalMobImage;
            imageMap.put(MobEnemy.class.getName(), originalMappedMobImage);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, BufferedImage> imageMap() throws Exception {
        Field field = ImageManager.class.getDeclaredField("CLASSNAME_IMAGE_MAP");
        field.setAccessible(true);
        return (Map<String, BufferedImage>) field.get(null);
    }
}
