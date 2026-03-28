package edu.hitsz.common;

/**
 * 服务端碰撞箱尺寸。
 *
 * 这里只影响命中判定，不影响客户端图片显示大小。
 * 如果你觉得“明明没碰到却算碰到”或者“明明碰到了却没算”，优先调这里。
 */
public final class EntitySizing {

    // 玩家机碰撞箱
    public static final int HERO_WIDTH = 100;
    public static final int HERO_HEIGHT = 83;

    // 敌机碰撞箱
    public static final int MOB_ENEMY_WIDTH = 75;
    public static final int MOB_ENEMY_HEIGHT = 50;

    public static final int ELITE_ENEMY_WIDTH = 105;
    public static final int ELITE_ENEMY_HEIGHT = 68;

    public static final int ELITE_PLUS_ENEMY_WIDTH = 105;
    public static final int ELITE_PLUS_ENEMY_HEIGHT = 68;

    public static final int ACE_ENEMY_WIDTH = 100;
    public static final int ACE_ENEMY_HEIGHT = 83;

    public static final int BOSS_ENEMY_WIDTH = 300;
    public static final int BOSS_ENEMY_HEIGHT = 200;

    // 子弹碰撞箱
    public static final int HERO_BULLET_WIDTH = 10;
    public static final int HERO_BULLET_HEIGHT = 25;

    public static final int ENEMY_BULLET_WIDTH = 10;
    public static final int ENEMY_BULLET_HEIGHT = 18;

    // 道具碰撞箱
    public static final int BLOOD_SUPPLY_WIDTH = 33;
    public static final int BLOOD_SUPPLY_HEIGHT = 30;

    public static final int FIRE_SUPPLY_WIDTH = 35;
    public static final int FIRE_SUPPLY_HEIGHT = 33;

    public static final int FIRE_PLUS_SUPPLY_WIDTH = 34;
    public static final int FIRE_PLUS_SUPPLY_HEIGHT = 34;

    public static final int BOMB_SUPPLY_WIDTH = 34;
    public static final int BOMB_SUPPLY_HEIGHT = 34;

    public static final int FREEZE_SUPPLY_WIDTH = 35;
    public static final int FREEZE_SUPPLY_HEIGHT = 35;

    private EntitySizing() {
    }
}
