package edu.hitsz.client;

import edu.hitsz.client.aircraft.AceEnemy;
import edu.hitsz.client.aircraft.BossEnemy;
import edu.hitsz.client.aircraft.EliteEnemy;
import edu.hitsz.client.aircraft.ElitePlusEnemy;
import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.client.aircraft.MobEnemy;
import edu.hitsz.client.aircraft.OtherPlayer;
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
        BACKGROUND_BY_CHAPTER.put(ChapterId.CH4, ImageManager.BOSS_BACKGROUND_IMAGE);
        BACKGROUND_BY_CHAPTER.put(ChapterId.CH5, ImageManager.LAUNCHER_BACKGROUND_IMAGE);

        ENEMY_IMAGES_BY_CHAPTER.put(ChapterId.CH1, chapterEnemyMap(
                ImageManager.MOB_ENEMY_IMAGE,
                ImageManager.CH1_ELITE_ENEMY_IMAGE,
                tint(ImageManager.CH1_ELITE_ENEMY_IMAGE, new Color(80, 180, 120, 72)),
                tint(ImageManager.COMMON_SOLDIER_IMAGE, new Color(215, 180, 60, 88)),
                ImageManager.CH1_BOSS_ENEMY_IMAGE
        ));
        ENEMY_IMAGES_BY_CHAPTER.put(ChapterId.CH2, chapterEnemyMap(
                tint(ImageManager.MOB_ENEMY_IMAGE, new Color(188, 156, 92, 72)),
                ImageManager.CH2_ELITE_ENEMY_IMAGE,
                tint(ImageManager.CH2_ELITE_ENEMY_IMAGE, new Color(176, 120, 48, 84)),
                tint(ImageManager.COMMON_SOLDIER_IMAGE, new Color(96, 92, 84, 90)),
                ImageManager.CH2_BOSS_ENEMY_IMAGE
        ));
        ENEMY_IMAGES_BY_CHAPTER.put(ChapterId.CH3, chapterEnemyMap(
                tint(ImageManager.MOB_ENEMY_IMAGE, new Color(122, 108, 94, 78)),
                ImageManager.CH3_ELITE_ENEMY_IMAGE,
                tint(ImageManager.CH3_ELITE_ENEMY_IMAGE, new Color(180, 72, 50, 88)),
                tint(ImageManager.COMMON_SOLDIER_IMAGE, new Color(132, 24, 18, 96)),
                ImageManager.CH3_BOSS_ENEMY_IMAGE
        ));
        ENEMY_IMAGES_BY_CHAPTER.put(ChapterId.CH4, chapterEnemyMap(
                tint(ImageManager.MOB_ENEMY_IMAGE, new Color(176, 88, 32, 92)),
                ImageManager.CH4_ELITE_ENEMY_IMAGE,
                tint(ImageManager.CH4_ELITE_ENEMY_IMAGE, new Color(214, 110, 58, 90)),
                tint(ImageManager.COMMON_SOLDIER_IMAGE, new Color(176, 66, 20, 110)),
                ImageManager.CH4_BOSS_ENEMY_IMAGE
        ));
        ENEMY_IMAGES_BY_CHAPTER.put(ChapterId.CH5, chapterEnemyMap(
                tint(ImageManager.MOB_ENEMY_IMAGE, new Color(90, 136, 196, 86)),
                ImageManager.CH5_ELITE_ENEMY_IMAGE,
                tint(ImageManager.CH5_ELITE_ENEMY_IMAGE, new Color(88, 188, 218, 90)),
                tint(ImageManager.COMMON_SOLDIER_IMAGE, new Color(88, 150, 220, 98)),
                ImageManager.CH5_BOSS_ENEMY_IMAGE
        ));
    }

    private ChapterVisualCatalog() {
    }

    public static BufferedImage backgroundFor(ChapterId chapterId, boolean bossActive) {
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

    public static BufferedImage aircraftImageFor(ChapterId chapterId, String className) {
        if (HeroAircraft.class.getName().equals(className)) {
            return ImageManager.aircraftImageFor(HeroAircraft.getSingleton().getAircraftBranch());
        }
        if (OtherPlayer.class.getName().equals(className)) {
            return null;
        }
        return null;
    }

    public static BufferedImage aircraftImageFor(ChapterId chapterId, Object aircraft) {
        if (aircraft instanceof HeroAircraft) {
            return ImageManager.aircraftImageFor(((HeroAircraft) aircraft).getAircraftBranch());
        }
        if (aircraft instanceof OtherPlayer) {
            return ImageManager.aircraftImageFor(((OtherPlayer) aircraft).getAircraftBranch());
        }
        return null;
    }

    private static Map<String, BufferedImage> chapterEnemyMap(BufferedImage mobImage,
                                                              BufferedImage eliteImage,
                                                              BufferedImage elitePlusImage,
                                                              BufferedImage aceImage,
                                                              BufferedImage bossImage) {
        Map<String, BufferedImage> images = new HashMap<>();
        images.put(MobEnemy.class.getName(), mobImage);
        images.put(EliteEnemy.class.getName(), eliteImage);
        images.put(ElitePlusEnemy.class.getName(), elitePlusImage);
        images.put(AceEnemy.class.getName(), aceImage);
        images.put(BossEnemy.class.getName(), bossImage);
        return images;
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
