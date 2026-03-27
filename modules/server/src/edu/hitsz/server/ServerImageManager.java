package edu.hitsz.server;

import edu.hitsz.common.ImageResourceLoader;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public final class ServerImageManager {

    private static final String SERVER_AIRCRAFT_PACKAGE = "edu.hitsz.server.aircraft.";
    private static final String SERVER_BULLET_PACKAGE = "edu.hitsz.server.bullet.";
    private static final String SERVER_BASIC_PACKAGE = "edu.hitsz.server.basic.";

    private static final Map<String, BufferedImage> CLASSNAME_IMAGE_MAP = new HashMap<>();

    public static BufferedImage HERO_IMAGE;
    public static BufferedImage HERO_BULLET_IMAGE;
    public static BufferedImage ENEMY_BULLET_IMAGE;
    public static BufferedImage MOB_ENEMY_IMAGE;
    public static BufferedImage ELITE_ENEMY_IMAGE;
    public static BufferedImage ELITE_PLUS_ENEMY_IMAGE;
    public static BufferedImage ACE_ENEMY_IMAGE;
    public static BufferedImage BOSS_ENEMY_IMAGE;
    public static BufferedImage BLOOD_SUPPLY_IMAGE;
    public static BufferedImage FIRE_SUPPLY_IMAGE;
    public static BufferedImage FIRE_PLUS_SUPPLY_IMAGE;
    public static BufferedImage BOMB_SUPPLY_IMAGE;
    public static BufferedImage FREEZE_SUPPLY_IMAGE;

    static {
        HERO_IMAGE = ImageResourceLoader.load("hero.png");
        HERO_BULLET_IMAGE = ImageResourceLoader.load("bullet_hero.png");
        ENEMY_BULLET_IMAGE = ImageResourceLoader.load("bullet_enemy.png");
        MOB_ENEMY_IMAGE = ImageResourceLoader.load("mob.png");
        ELITE_ENEMY_IMAGE = ImageResourceLoader.load("elite.png");
        ELITE_PLUS_ENEMY_IMAGE = ImageResourceLoader.load("elitePlus.png");
        ACE_ENEMY_IMAGE = ImageResourceLoader.load("elitePro.png");
        BOSS_ENEMY_IMAGE = ImageResourceLoader.load("boss.png");
        BLOOD_SUPPLY_IMAGE = ImageResourceLoader.load("prop_blood.png");
        FIRE_SUPPLY_IMAGE = ImageResourceLoader.load("prop_bullet.png");
        FIRE_PLUS_SUPPLY_IMAGE = ImageResourceLoader.load("prop_bulletPlus.png");
        BOMB_SUPPLY_IMAGE = ImageResourceLoader.load("prop_bomb.png");
        FREEZE_SUPPLY_IMAGE = ImageResourceLoader.load("prop_freeze.png");

        putMappings(HERO_IMAGE, ServerPlayerAircraft.class.getName());
        putMappings(MOB_ENEMY_IMAGE, SERVER_AIRCRAFT_PACKAGE + "MobEnemy");
        putMappings(ELITE_ENEMY_IMAGE, SERVER_AIRCRAFT_PACKAGE + "EliteEnemy");
        putMappings(ELITE_PLUS_ENEMY_IMAGE, SERVER_AIRCRAFT_PACKAGE + "ElitePlusEnemy");
        putMappings(ACE_ENEMY_IMAGE, SERVER_AIRCRAFT_PACKAGE + "AceEnemy");
        putMappings(BOSS_ENEMY_IMAGE, SERVER_AIRCRAFT_PACKAGE + "BossEnemy");
        putMappings(HERO_BULLET_IMAGE, SERVER_BULLET_PACKAGE + "HeroBullet", ServerHeroBullet.class.getName());
        putMappings(ENEMY_BULLET_IMAGE, SERVER_BULLET_PACKAGE + "EnemyBullet");
        putMappings(BLOOD_SUPPLY_IMAGE, SERVER_BASIC_PACKAGE + "BloodSupply");
        putMappings(FIRE_SUPPLY_IMAGE, SERVER_BASIC_PACKAGE + "FireSupply");
        putMappings(FIRE_PLUS_SUPPLY_IMAGE, SERVER_BASIC_PACKAGE + "FirePlusSupply");
        putMappings(BOMB_SUPPLY_IMAGE, SERVER_BASIC_PACKAGE + "BombSupply");
        putMappings(FREEZE_SUPPLY_IMAGE, SERVER_BASIC_PACKAGE + "FreezeSupply");
    }

    private ServerImageManager() {
    }

    public static BufferedImage get(String className) {
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static BufferedImage get(Object obj) {
        if (obj == null) {
            return null;
        }
        return get(obj.getClass().getName());
    }

    private static void putMappings(BufferedImage image, String... classNames) {
        for (String className : classNames) {
            CLASSNAME_IMAGE_MAP.put(className, image);
        }
    }
}
