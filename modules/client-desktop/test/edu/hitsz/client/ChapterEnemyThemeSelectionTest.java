package edu.hitsz.client;

import edu.hitsz.client.aircraft.BossEnemy;
import edu.hitsz.client.aircraft.MobEnemy;
import edu.hitsz.common.ChapterId;

import java.awt.image.BufferedImage;

public class ChapterEnemyThemeSelectionTest {

    public static void main(String[] args) {
        BufferedImage ch1Mob = ChapterVisualCatalog.enemyImageFor(ChapterId.CH1, MobEnemy.class.getName());
        BufferedImage ch2Mob = ChapterVisualCatalog.enemyImageFor(ChapterId.CH2, MobEnemy.class.getName());
        BufferedImage ch3Mob = ChapterVisualCatalog.enemyImageFor(ChapterId.CH3, MobEnemy.class.getName());

        assert ch1Mob != null : "CH1 should resolve a mob image";
        assert ch2Mob != null : "CH2 should resolve a mob image";
        assert ch3Mob != null : "CH3 should resolve a mob image";
        assert ch1Mob != ch2Mob : "The same enemy archetype should have a chapter-specific image variant";
        assert ch2Mob != ch3Mob : "The same enemy archetype should have a chapter-specific image variant";

        BufferedImage ch1Boss = ChapterVisualCatalog.enemyImageFor(ChapterId.CH1, BossEnemy.class.getName());
        BufferedImage ch3Boss = ChapterVisualCatalog.enemyImageFor(ChapterId.CH3, BossEnemy.class.getName());
        assert ch1Boss != null : "Boss image should resolve for CH1";
        assert ch3Boss != null : "Boss image should resolve for CH3";
        assert ch1Boss != ch3Boss : "Boss visuals should also vary by chapter";
    }
}
