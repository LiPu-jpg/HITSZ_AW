package edu.hitsz;

import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.basic.BloodSupply;
import edu.hitsz.bullet.BaseBullet;

import java.util.List;

public class FeatureRegressionTest {

    public static void main(String[] args) {
        testHeroAircraftSingleton();
        testEliteEnemyShootDownward();
        testBloodSupplyCapsHeroHpAtMax();
        System.out.println("FeatureRegressionTest passed.");
    }

    private static void testHeroAircraftSingleton() {
        HeroAircraft first = HeroAircraft.getSingleton();
        HeroAircraft second = HeroAircraft.getSingleton();
        assert first == second : "HeroAircraft should be a singleton";
    }

    private static void testEliteEnemyShootDownward() {
        EliteEnemy eliteEnemy = new EliteEnemy(100, 100, 0, 5, 90);
        List<BaseBullet> bullets = eliteEnemy.shoot();
        assert bullets.size() == 1 : "Elite enemy should shoot one bullet by default";

        BaseBullet bullet = bullets.get(0);
        int initialY = bullet.getLocationY();
        bullet.forward();
        assert bullet.getLocationY() > initialY : "Enemy bullet should move downward";
    }

    private static void testBloodSupplyCapsHeroHpAtMax() {
        HeroAircraft heroAircraft = HeroAircraft.getSingleton();
        heroAircraft.decreaseHp(300);
        BloodSupply bloodSupply = new BloodSupply(0, 0, 0, 1);
        bloodSupply.activate(heroAircraft);
        bloodSupply.activate(heroAircraft);
        bloodSupply.activate(heroAircraft);
        bloodSupply.activate(heroAircraft);
        assert heroAircraft.getHp() == 1000 : "Hero hp should not exceed 1000";
    }
}
