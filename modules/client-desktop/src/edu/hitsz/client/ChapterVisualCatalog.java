package edu.hitsz.client;

import edu.hitsz.client.aircraft.AceEnemy;
import edu.hitsz.client.aircraft.BossEnemy;
import edu.hitsz.client.aircraft.EliteEnemy;
import edu.hitsz.client.aircraft.ElitePlusEnemy;
import edu.hitsz.client.aircraft.MobEnemy;
import edu.hitsz.common.ChapterId;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class ChapterVisualCatalog {

    private static final Map<ChapterId, BufferedImage> BACKGROUND_BY_CHAPTER = new EnumMap<>(ChapterId.class);
    private static final Map<ChapterId, Map<String, BufferedImage>> ENEMY_IMAGES_BY_CHAPTER = new EnumMap<>(ChapterId.class);

    static {
        BACKGROUND_BY_CHAPTER.put(ChapterId.CH1, ImageManager.EASY_BACKGROUND_IMAGE);
        BACKGROUND_BY_CHAPTER.put(ChapterId.CH2, ImageManager.NORMAL_BACKGROUND_IMAGE);
        BACKGROUND_BY_CHAPTER.put(ChapterId.CH3, ImageManager.HARD_BACKGROUND_IMAGE);

        ENEMY_IMAGES_BY_CHAPTER.put(ChapterId.CH1, baseEnemyMap());
        ENEMY_IMAGES_BY_CHAPTER.put(ChapterId.CH2, tintedEnemyMap(new Color(40, 120, 160, 70)));
        ENEMY_IMAGES_BY_CHAPTER.put(ChapterId.CH3, tintedEnemyMap(new Color(180, 80, 30, 80)));
    }

    private ChapterVisualCatalog() {
    }

    public static BufferedImage backgroundFor(ChapterId chapterId, boolean bossActive) {
        if (bossActive) {
            return ImageManager.BOSS_BACKGROUND_IMAGE;
        }
        return BACKGROUND_BY_CHAPTER.getOrDefault(chapterId, ImageManager.NORMAL_BACKGROUND_IMAGE);
    }

    public static BufferedImage enemyImageFor(ChapterId chapterId, String className) {
        Map<String, BufferedImage> chapterImages = ENEMY_IMAGES_BY_CHAPTER.getOrDefault(
                chapterId,
                ENEMY_IMAGES_BY_CHAPTER.get(ChapterId.CH1)
        );
        BufferedImage image = chapterImages.get(className);
        if (image != null) {
            return image;
        }
        return ImageManager.get(className);
    }

    private static Map<String, BufferedImage> baseEnemyMap() {
        Map<String, BufferedImage> images = new HashMap<>();
        images.put(MobEnemy.class.getName(), ImageManager.MOB_ENEMY_IMAGE);
        images.put(EliteEnemy.class.getName(), ImageManager.ELITE_ENEMY_IMAGE);
        images.put(ElitePlusEnemy.class.getName(), ImageManager.ELITE_PLUS_ENEMY_IMAGE);
        images.put(AceEnemy.class.getName(), ImageManager.ACE_ENEMY_IMAGE);
        images.put(BossEnemy.class.getName(), ImageManager.BOSS_ENEMY_IMAGE);
        return images;
    }

    private static Map<String, BufferedImage> tintedEnemyMap(Color tint) {
        Map<String, BufferedImage> base = baseEnemyMap();
        Map<String, BufferedImage> tinted = new HashMap<>();
        for (Map.Entry<String, BufferedImage> entry : base.entrySet()) {
            tinted.put(entry.getKey(), tint(entry.getValue(), tint));
        }
        return tinted;
    }

    private static BufferedImage tint(BufferedImage source, Color tint) {
        BufferedImage tinted = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = tinted.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.setColor(tint);
        graphics.fillRect(0, 0, source.getWidth(), source.getHeight());
        graphics.dispose();
        return tinted;
    }
}
