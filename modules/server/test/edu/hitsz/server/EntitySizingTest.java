package edu.hitsz.server;

import edu.hitsz.common.EntitySizing;
import edu.hitsz.common.GameConstants;
import edu.hitsz.server.aircraft.AbstractAircraft;
import edu.hitsz.server.aircraft.MobEnemy;
import edu.hitsz.server.basic.BloodSupply;
import edu.hitsz.server.bullet.EnemyBullet;

import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

public class EntitySizingTest {

    public static void main(String[] args) throws Exception {
        mobEnemyUsesExplicitSizingInsteadOfImageDimensions();
        spawnBoundaryDoesNotDependOnMobImageWidth();
        bulletsAndItemsUseExplicitSizing();
    }

    private static void mobEnemyUsesExplicitSizingInsteadOfImageDimensions() {
        BufferedImage originalMobImage = ServerImageManager.MOB_ENEMY_IMAGE;
        ServerImageManager.MOB_ENEMY_IMAGE = new BufferedImage(999, 777, BufferedImage.TYPE_INT_ARGB);
        try {
            MobEnemy mobEnemy = new MobEnemy(100, 100, 0, 10, 30);
            assert mobEnemy.getWidth() == EntitySizing.MOB_ENEMY_WIDTH
                    : "Mob enemy width should come from EntitySizing";
            assert mobEnemy.getHeight() == EntitySizing.MOB_ENEMY_HEIGHT
                    : "Mob enemy height should come from EntitySizing";
        } finally {
            ServerImageManager.MOB_ENEMY_IMAGE = originalMobImage;
        }
    }

    private static void spawnBoundaryDoesNotDependOnMobImageWidth() throws Exception {
        BufferedImage originalMobImage = ServerImageManager.MOB_ENEMY_IMAGE;
        ServerImageManager.MOB_ENEMY_IMAGE = new BufferedImage(2000, 1200, BufferedImage.TYPE_INT_ARGB);
        try {
            ServerWorldState worldState = new ServerWorldState();
            Method createEnemy = ServerWorldState.class.getDeclaredMethod("createEnemy");
            createEnemy.setAccessible(true);
            AbstractAircraft enemy = (AbstractAircraft) createEnemy.invoke(worldState);
            int enemyWidth = enemy.getWidth();
            assert enemy.getLocationX() >= enemyWidth / 2
                    : "Enemy spawn X should stay inside left bound derived from sizing";
            assert enemy.getLocationX() <= GameConstants.WINDOW_WIDTH - enemyWidth / 2
                    : "Enemy spawn X should stay inside right bound derived from sizing";
        } finally {
            ServerImageManager.MOB_ENEMY_IMAGE = originalMobImage;
        }
    }

    private static void bulletsAndItemsUseExplicitSizing() {
        EnemyBullet enemyBullet = new EnemyBullet(100, 100, 0, 5, 10);
        BloodSupply bloodSupply = new BloodSupply(100, 100, 0, 4);
        assert enemyBullet.getWidth() == EntitySizing.ENEMY_BULLET_WIDTH
                : "Enemy bullet width should come from EntitySizing";
        assert enemyBullet.getHeight() == EntitySizing.ENEMY_BULLET_HEIGHT
                : "Enemy bullet height should come from EntitySizing";
        assert bloodSupply.getWidth() == EntitySizing.BLOOD_SUPPLY_WIDTH
                : "Blood supply width should come from EntitySizing";
        assert bloodSupply.getHeight() == EntitySizing.BLOOD_SUPPLY_HEIGHT
                : "Blood supply height should come from EntitySizing";
    }
}
