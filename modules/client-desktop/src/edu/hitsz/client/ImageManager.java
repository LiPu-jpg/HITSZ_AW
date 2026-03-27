package edu.hitsz.client;

import edu.hitsz.client.aircraft.AceEnemy;
import edu.hitsz.client.aircraft.BossEnemy;
import edu.hitsz.client.aircraft.EliteEnemy;
import edu.hitsz.client.aircraft.ElitePlusEnemy;
import edu.hitsz.client.aircraft.HeroAircraft;
import edu.hitsz.client.aircraft.MobEnemy;
import edu.hitsz.client.aircraft.OtherPlayer;
import edu.hitsz.client.basic.BloodSupply;
import edu.hitsz.client.basic.BombSupply;
import edu.hitsz.client.basic.FirePlusSupply;
import edu.hitsz.client.basic.FireSupply;
import edu.hitsz.client.basic.FreezeSupply;
import edu.hitsz.client.bullet.EnemyBullet;
import edu.hitsz.client.bullet.HeroBullet;
import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.ChapterId;
import edu.hitsz.common.ImageResourceLoader;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * 综合管理图片的加载，访问
 * 提供图片的静态访问方法
 * @author hitsz
 */
public class ImageManager {

    /**
     * 类名-图片 映射，存储各基类的图片 <br>
     * 可使用 CLASSNAME_IMAGE_MAP.get( obj.getClass().getName() ) 获得 obj 所属基类对应的图片
     */
    private static final Map<String, BufferedImage> CLASSNAME_IMAGE_MAP = new HashMap<>();

    public static BufferedImage BACKGROUND_IMAGE;
    public static BufferedImage LAUNCHER_BACKGROUND_IMAGE;
    public static BufferedImage EASY_BACKGROUND_IMAGE;
    public static BufferedImage NORMAL_BACKGROUND_IMAGE;
    public static BufferedImage HARD_BACKGROUND_IMAGE;
    public static BufferedImage BOSS_BACKGROUND_IMAGE;
    public static BufferedImage HERO_IMAGE;
    public static BufferedImage STARTER_BLUE_IMAGE;
    public static BufferedImage RED_SPEED_IMAGE;
    public static BufferedImage GREEN_DEFENSE_IMAGE;
    public static BufferedImage BLACK_HEAVY_IMAGE;
    public static BufferedImage HERO_BULLET_IMAGE;
    public static BufferedImage ENEMY_BULLET_IMAGE;
    public static BufferedImage MOB_ENEMY_IMAGE;
    public static BufferedImage COMMON_SOLDIER_IMAGE;
    public static BufferedImage ELITE_ENEMY_IMAGE;
    public static BufferedImage ELITE_PLUS_ENEMY_IMAGE;
    public static BufferedImage ACE_ENEMY_IMAGE;
    public static BufferedImage BOSS_ENEMY_IMAGE;
    public static BufferedImage CH1_ELITE_ENEMY_IMAGE;
    public static BufferedImage CH2_ELITE_ENEMY_IMAGE;
    public static BufferedImage CH3_ELITE_ENEMY_IMAGE;
    public static BufferedImage CH1_BOSS_ENEMY_IMAGE;
    public static BufferedImage CH2_BOSS_ENEMY_IMAGE;
    public static BufferedImage CH3_BOSS_ENEMY_IMAGE;
    public static BufferedImage BLOOD_SUPPLY_IMAGE;
    public static BufferedImage FIRE_SUPPLY_IMAGE;
    public static BufferedImage FIRE_PLUS_SUPPLY_IMAGE;
    public static BufferedImage BOMB_SUPPLY_IMAGE;
    public static BufferedImage FREEZE_SUPPLY_IMAGE;

    static {
        LAUNCHER_BACKGROUND_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/背景5-天域.jpg", "bg.jpg");
        EASY_BACKGROUND_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/背景1-大草原.jpg", "bg2.jpg");
        NORMAL_BACKGROUND_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/背景2-砂石之地.jpg", "bg3.jpg");
        HARD_BACKGROUND_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/背景3-河源工厂.jpg", "bg4.jpg");
        BOSS_BACKGROUND_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/背景4-熔岩火山.jpg", "bg5.jpg");
        BACKGROUND_IMAGE = NORMAL_BACKGROUND_IMAGE;

        STARTER_BLUE_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/初始飞机.png", "hero.png");
        HERO_IMAGE = STARTER_BLUE_IMAGE;
        RED_SPEED_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/速度分支.png", "hero.png");
        GREEN_DEFENSE_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/防御分支.png", "hero.png");
        BLACK_HEAVY_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/重轰分支.png", "hero.png");

        MOB_ENEMY_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/通用杂兵.png", "mob.png");
        COMMON_SOLDIER_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/通用普通兵.png", "elite.png");
        CH1_ELITE_ENEMY_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/1号地图-精英兵.png", "elite.png");
        CH2_ELITE_ENEMY_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/2号地图-精英兵.png", "elite.png");
        CH3_ELITE_ENEMY_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/3号地图-精英兵.png", "elite.png");
        CH1_BOSS_ENEMY_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/boss1.png", "boss.png");
        CH2_BOSS_ENEMY_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/boss2.png", "boss.png");
        CH3_BOSS_ENEMY_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/boss3.png", "boss.png");
        ELITE_ENEMY_IMAGE = CH1_ELITE_ENEMY_IMAGE;
        ELITE_PLUS_ENEMY_IMAGE = COMMON_SOLDIER_IMAGE;
        ACE_ENEMY_IMAGE = CH1_ELITE_ENEMY_IMAGE;
        BOSS_ENEMY_IMAGE = CH1_BOSS_ENEMY_IMAGE;

        HERO_BULLET_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/普通子弹-友.png", "bullet_hero.png");
        ENEMY_BULLET_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/普通子弹-敌.png", "bullet_enemy.png");
        BLOOD_SUPPLY_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/回血道具.png", "prop_blood.png");
        FIRE_SUPPLY_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/子弹增强道具.png", "prop_bullet.png");
        FIRE_PLUS_SUPPLY_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/奇怪的标识.png", "prop_bulletPlus.png");
        BOMB_SUPPLY_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/爆炸道具.png", "prop_bomb.png");
        FREEZE_SUPPLY_IMAGE = ImageResourceLoader.loadOrFallback("最终素材/冻结道具.png", "prop_freeze.png");

        putMappings(HERO_IMAGE, HeroAircraft.class.getName(), OtherPlayer.class.getName());
        putMappings(MOB_ENEMY_IMAGE, MobEnemy.class.getName());
        putMappings(ELITE_ENEMY_IMAGE, EliteEnemy.class.getName());
        putMappings(ELITE_PLUS_ENEMY_IMAGE, ElitePlusEnemy.class.getName());
        putMappings(ACE_ENEMY_IMAGE, AceEnemy.class.getName());
        putMappings(BOSS_ENEMY_IMAGE, BossEnemy.class.getName());
        putMappings(HERO_BULLET_IMAGE, HeroBullet.class.getName());
        putMappings(ENEMY_BULLET_IMAGE, EnemyBullet.class.getName());
        putMappings(BLOOD_SUPPLY_IMAGE, BloodSupply.class.getName());
        putMappings(FIRE_SUPPLY_IMAGE, FireSupply.class.getName());
        putMappings(FIRE_PLUS_SUPPLY_IMAGE, FirePlusSupply.class.getName());
        putMappings(BOMB_SUPPLY_IMAGE, BombSupply.class.getName());
        putMappings(FREEZE_SUPPLY_IMAGE, FreezeSupply.class.getName());
    }

    public static BufferedImage get(String className){
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static BufferedImage get(Object obj){
        if (obj == null){
            return null;
        }
        return get(obj.getClass().getName());
    }

    public static BufferedImage get(Object obj, ChapterId chapterId) {
        if (obj == null) {
            return null;
        }
        BufferedImage aircraftImage = ChapterVisualCatalog.aircraftImageFor(chapterId, obj);
        if (aircraftImage != null) {
            return aircraftImage;
        }
        return get(obj.getClass().getName(), chapterId);
    }

    public static BufferedImage get(String className, ChapterId chapterId) {
        BufferedImage aircraftImage = ChapterVisualCatalog.aircraftImageFor(chapterId, className);
        if (aircraftImage != null) {
            return aircraftImage;
        }
        BufferedImage chapterImage = ChapterVisualCatalog.enemyImageFor(chapterId, className);
        return chapterImage == null ? get(className) : chapterImage;
    }

    public static BufferedImage aircraftImageFor(AircraftBranch branch) {
        AircraftBranch resolvedBranch = branch == null ? AircraftBranch.STARTER_BLUE : branch;
        switch (resolvedBranch) {
            case RED_SPEED:
                return RED_SPEED_IMAGE;
            case GREEN_DEFENSE:
                return GREEN_DEFENSE_IMAGE;
            case BLACK_HEAVY:
                return BLACK_HEAVY_IMAGE;
            case STARTER_BLUE:
            default:
                return STARTER_BLUE_IMAGE;
        }
    }

    private static void putMappings(BufferedImage image, String... classNames) {
        for (String className : classNames) {
            CLASSNAME_IMAGE_MAP.put(className, image);
        }
    }

    public static BufferedImage backgroundFor(String difficulty, boolean bossActive) {
        if (bossActive) {
            return BOSS_BACKGROUND_IMAGE;
        }
        if ("EASY".equals(difficulty)) {
            return EASY_BACKGROUND_IMAGE;
        }
        if ("HARD".equals(difficulty)) {
            return HARD_BACKGROUND_IMAGE;
        }
        return NORMAL_BACKGROUND_IMAGE;
    }

    public static BufferedImage backgroundFor(ChapterId chapterId, boolean bossActive) {
        return ChapterVisualCatalog.backgroundFor(chapterId, bossActive);
    }

}
