package edu.hitsz.client;

import edu.hitsz.client.aircraft.BossEnemy;
import edu.hitsz.client.aircraft.EliteEnemy;
import edu.hitsz.client.aircraft.MobEnemy;
import edu.hitsz.common.ChapterId;

import java.awt.image.BufferedImage;

public class ChapterEnemyThemeSelectionTest {

    public static void main(String[] args) {
        BufferedImage ch1Mob = ChapterVisualCatalog.enemyImageFor(ChapterId.CH1, MobEnemy.class.getName());
        BufferedImage ch2Mob = ChapterVisualCatalog.enemyImageFor(ChapterId.CH2, MobEnemy.class.getName());
        BufferedImage ch3Mob = ChapterVisualCatalog.enemyImageFor(ChapterId.CH3, MobEnemy.class.getName());
        BufferedImage ch4Mob = ChapterVisualCatalog.enemyImageFor(ChapterId.CH4, MobEnemy.class.getName());
        BufferedImage ch5Mob = ChapterVisualCatalog.enemyImageFor(ChapterId.CH5, MobEnemy.class.getName());

        assert ch1Mob != null : "CH1 should resolve a mob image";
        assert ch2Mob != null : "CH2 should resolve a mob image";
        assert ch3Mob != null : "CH3 should resolve a mob image";
        assert ch4Mob != null : "CH4 should resolve a mob image";
        assert ch5Mob != null : "CH5 should resolve a mob image";
        assert ch1Mob != ch2Mob : "The same enemy archetype should have a chapter-specific image variant";
        assert ch2Mob != ch3Mob : "The same enemy archetype should have a chapter-specific image variant";
        assert ch3Mob != ch4Mob : "The same enemy archetype should have a chapter-specific image variant";
        assert ch4Mob != ch5Mob : "The same enemy archetype should have a chapter-specific image variant";

        BufferedImage ch1Elite = ChapterVisualCatalog.enemyImageFor(ChapterId.CH1, EliteEnemy.class.getName());
        BufferedImage ch2Elite = ChapterVisualCatalog.enemyImageFor(ChapterId.CH2, EliteEnemy.class.getName());
        BufferedImage ch3Elite = ChapterVisualCatalog.enemyImageFor(ChapterId.CH3, EliteEnemy.class.getName());
        assert ch1Elite == ImageManager.CH1_ELITE_ENEMY_IMAGE : "CH1 elite should use chapter-1 elite art";
        assert ch2Elite == ImageManager.CH2_ELITE_ENEMY_IMAGE : "CH2 elite should use chapter-2 elite art";
        assert ch3Elite == ImageManager.CH3_ELITE_ENEMY_IMAGE : "CH3 elite should use chapter-3 elite art";

        BufferedImage ch1Boss = ChapterVisualCatalog.enemyImageFor(ChapterId.CH1, BossEnemy.class.getName());
        BufferedImage ch2Boss = ChapterVisualCatalog.enemyImageFor(ChapterId.CH2, BossEnemy.class.getName());
        BufferedImage ch3Boss = ChapterVisualCatalog.enemyImageFor(ChapterId.CH3, BossEnemy.class.getName());
        BufferedImage ch4Boss = ChapterVisualCatalog.enemyImageFor(ChapterId.CH4, BossEnemy.class.getName());
        BufferedImage ch5Boss = ChapterVisualCatalog.enemyImageFor(ChapterId.CH5, BossEnemy.class.getName());
        assert ch1Boss != null : "Boss image should resolve for CH1";
        assert ch2Boss != null : "Boss image should resolve for CH2";
        assert ch3Boss != null : "Boss image should resolve for CH3";
        assert ch4Boss != null : "Boss image should resolve for CH4";
        assert ch5Boss != null : "Boss image should resolve for CH5";
        assert ch1Boss == ImageManager.CH1_BOSS_ENEMY_IMAGE : "CH1 boss should use boss1 art";
        assert ch2Boss == ImageManager.CH2_BOSS_ENEMY_IMAGE : "CH2 boss should use boss2 art";
        assert ch3Boss == ImageManager.CH3_BOSS_ENEMY_IMAGE : "CH3 boss should use boss3 art";
        assert ch4Boss == ImageManager.CH4_BOSS_ENEMY_IMAGE : "CH4 boss should use boss4 art";
        assert ch5Boss == ImageManager.CH5_BOSS_ENEMY_IMAGE : "CH5 boss should use boss5 art";
        assert ch1Boss != ch3Boss : "Boss visuals should also vary by chapter";
    }
}
