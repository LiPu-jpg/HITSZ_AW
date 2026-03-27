package edu.hitsz.client;

import edu.hitsz.client.aircraft.MobEnemy;
import edu.hitsz.client.basic.BloodSupply;
import edu.hitsz.client.bullet.EnemyBullet;
import edu.hitsz.common.EntitySizing;

import java.awt.image.BufferedImage;

public class EntitySizingRenderTest {

    public static void main(String[] args) {
        renderObjectsUseExplicitSizingInsteadOfImageDimensions();
        bulletsAndItemsUseExplicitSizing();
    }

    private static void renderObjectsUseExplicitSizingInsteadOfImageDimensions() {
        BufferedImage originalMobImage = ImageManager.MOB_ENEMY_IMAGE;
        ImageManager.MOB_ENEMY_IMAGE = new BufferedImage(888, 666, BufferedImage.TYPE_INT_ARGB);
        try {
            MobEnemy mobEnemy = new MobEnemy(100, 100, 0, 10, 30);
            assert mobEnemy.getWidth() == EntitySizing.MOB_ENEMY_WIDTH
                    : "Client mob enemy width should come from EntitySizing";
            assert mobEnemy.getHeight() == EntitySizing.MOB_ENEMY_HEIGHT
                    : "Client mob enemy height should come from EntitySizing";
        } finally {
            ImageManager.MOB_ENEMY_IMAGE = originalMobImage;
        }
    }

    private static void bulletsAndItemsUseExplicitSizing() {
        EnemyBullet enemyBullet = new EnemyBullet(100, 100, 0, 5, 10);
        BloodSupply bloodSupply = new BloodSupply(100, 100, 0, 4);
        assert enemyBullet.getWidth() == EntitySizing.ENEMY_BULLET_WIDTH
                : "Client enemy bullet width should come from EntitySizing";
        assert enemyBullet.getHeight() == EntitySizing.ENEMY_BULLET_HEIGHT
                : "Client enemy bullet height should come from EntitySizing";
        assert bloodSupply.getWidth() == EntitySizing.BLOOD_SUPPLY_WIDTH
                : "Client blood supply width should come from EntitySizing";
        assert bloodSupply.getHeight() == EntitySizing.BLOOD_SUPPLY_HEIGHT
                : "Client blood supply height should come from EntitySizing";
    }
}
