package edu.hitsz.server;

import edu.hitsz.common.AircraftBranch;
import edu.hitsz.common.Difficulty;

/**
 * 服务端权威数值总表。
 *
 * 调数值时优先看这个文件。
 * 这里集中的是“战斗节奏 / 伤害 / 血量 / 冷却 / 敌机生成 / Boss 阈值 / 分支成长”。
 *
 * 不在这里调的内容：
 * 1. 碰撞箱大小：看 {@code EntitySizing}
 * 2. 客户端显示大小：看 {@code EntityRenderSizing}
 * 3. 窗口大小：看 {@code GameConstants}
 *
 * 建议调参顺序：
 * 1. 先调玩家和敌机基础数值
 * 2. 再调刷怪节奏和 Boss 阈值
 * 3. 最后调分支成长和章节弹幕密度
 */
public final class GameplayBalance {

    /** 世界主循环 tick 间隔，单位毫秒。越小越“丝滑”，但服务器更新更频繁。 */
    public static final int WORLD_TICK_INTERVAL_MILLIS = 40;

    // =========================
    // 玩家基础属性
    // =========================
    /** 玩家初始生命值。 */
    public static final int PLAYER_INITIAL_HP = 1000;
    /** 玩家初始单次发射弹数。 */
    public static final int PLAYER_BASE_SHOOT_NUM = 1;
    /** 玩家最大基础弹数上限。 */
    public static final int PLAYER_MAX_SHOOT_NUM = 3;
    /** 玩家基础射击周期。数值越小，射速越高。 */
    public static final int PLAYER_BASE_SHOOT_CYCLE = 20;
    /** 玩家射击周期下限，防止成长后无限提速。 */
    public static final int PLAYER_MIN_SHOOT_CYCLE = 8;
    /** 玩家基础移动速度。当前已经调成原来的 2 倍。 */
    public static final int PLAYER_BASE_MOVE_SPEED = 16;
    /** 接近鼠标目标点后允许停下的半径，避免贴点抖动。 */
    public static final int PLAYER_STOP_RADIUS = 6;
    /** 玩家基础子弹伤害。 */
    public static final int PLAYER_BASE_BULLET_POWER = 30;
    /** 玩家子弹伤害上限。 */
    public static final int PLAYER_MAX_BULLET_POWER = 60;
    /** 每次火力成长增加的基础子弹伤害。 */
    public static final int PLAYER_BULLET_POWER_STEP = 10;
    /** 玩家普通子弹速度。 */
    public static final int PLAYER_BULLET_SPEED = 5;
    /** 射速升级每级减少多少发射周期。 */
    public static final int PLAYER_FIRE_RATE_UPGRADE_CYCLE_REDUCTION = 2;
    /** 子弹伤害升级每级增加的伤害。 */
    public static final int PLAYER_BULLET_POWER_UPGRADE_BONUS = 10;
    /** 散射升级每级增加的额外弹数。 */
    public static final int PLAYER_SPREAD_SHOT_UPGRADE_BONUS = 1;

    // =========================
    // 分支机基础武器与成长
    // =========================
    /** 绿色防御机基础散射弹数量。 */
    public static final int GREEN_DEFENSE_SPREAD_BULLET_COUNT = 3;
    /** 绿色防御机相邻散射子弹的横向速度间隔。 */
    public static final int GREEN_DEFENSE_SPREAD_X_SPEED_STEP = 2;
    /** 红色速度机“移速强化”每级增加的移速。 */
    public static final int RED_SPEED_MOVE_SPEED_UPGRADE_BONUS = 1;
    /** 红色速度机“激光伤害强化”每级增加的伤害。 */
    public static final int RED_SPEED_LASER_DAMAGE_UPGRADE_BONUS = 8;
    /** 红色速度机“激光宽度强化”每级增加的宽度。 */
    public static final int RED_SPEED_LASER_WIDTH_UPGRADE_BONUS = 6;
    /** 红色速度机“激光持续强化”每级增加的持续 tick。 */
    public static final int RED_SPEED_LASER_DURATION_UPGRADE_BONUS = 1;
    /** 绿色防御机“散射数量强化”每级增加的弹数。 */
    public static final int GREEN_DEFENSE_SPREAD_COUNT_UPGRADE_BONUS = 1;
    /** 绿色防御机“散射角度强化”每级增加的横向扩散。 */
    public static final int GREEN_DEFENSE_SPREAD_WIDTH_UPGRADE_BONUS = 1;
    /** 绿色防御机“子弹伤害强化”每级增加的伤害。 */
    public static final int GREEN_DEFENSE_BULLET_DAMAGE_UPGRADE_BONUS = 8;
    /** 通用生命上限强化，每级增加的最大生命值。 */
    public static final int BRANCH_MAX_HP_UPGRADE_BONUS = 120;
    /** 黑色重轰机“空爆伤害强化”每级增加的伤害。 */
    public static final int BLACK_HEAVY_AIRBURST_DAMAGE_UPGRADE_BONUS = 12;
    /** 黑色重轰机“空爆范围强化”每级增加的爆炸半径。 */
    public static final int BLACK_HEAVY_AIRBURST_RADIUS_UPGRADE_BONUS = 20;
    /** 黑色重轰机“空爆射程强化”每级增加的最大射程。 */
    public static final int BLACK_HEAVY_AIRBURST_RANGE_UPGRADE_BONUS = 40;
    /** 轻微追踪升级每级增加的横向修正速度。 */
    public static final int PLAYER_LIGHT_TRACKING_SPEED_STEP = 1;
    /** 轻微追踪最大横向修正速度上限。 */
    public static final int PLAYER_LIGHT_TRACKING_MAX_SPEED = 4;

    // =========================
    // 红色速度机激光参数
    // =========================
    /** 红色速度机默认朝正前方发射激光。 */
    public static final double RED_SPEED_LASER_ANGLE = -Math.PI / 2.0;
    /** 红色速度机基础激光宽度。 */
    public static final int RED_SPEED_LASER_WIDTH = 24;
    /** 红色速度机基础激光持续时间，单位 tick。 */
    public static final int RED_SPEED_LASER_DURATION_TICKS = 4;

    // =========================
    // Boss 锁定预警激光
    // =========================
    /** Boss 预警激光宽度。 */
    public static final int BOSS_WARNING_LASER_WIDTH = 18;
    /** Boss 预警/发射激光长度。 */
    public static final int BOSS_WARNING_LASER_LENGTH = 620;
    /** Boss 预警阶段持续 tick。 */
    public static final int BOSS_WARNING_LASER_WARNING_TICKS = 38;
    /** Boss 真正发射阶段持续 tick。 */
    public static final int BOSS_WARNING_LASER_FIRING_TICKS = 4;
    /** Boss 激光单次命中的伤害。 */
    public static final int BOSS_WARNING_LASER_DAMAGE = 80;

    // =========================
    // 黑色重轰机空爆弹参数
    // =========================
    /** 黑色重轰机基础爆炸半径。 */
    public static final int BLACK_HEAVY_AIRBURST_RADIUS = 90;
    /** 黑色重轰机基础最大射程。达到后自动空爆。 */
    public static final int BLACK_HEAVY_AIRBURST_MAX_RANGE = 280;
    /** 黑色重轰机空爆弹飞行速度。 */
    public static final int BLACK_HEAVY_AIRBURST_PROJECTILE_SPEED = 28;
    /** 爆炸特效保留时长，单位 tick。 */
    public static final int BLACK_HEAVY_EXPLOSION_DURATION_TICKS = 3;

    // =========================
    // 章节化敌机弹幕参数
    // =========================
    /** 第 2 章精英横向散射间隔。 */
    public static final int CH2_ELITE_SPREAD_X_SPEED_STEP = 2;
    /** 第 2 章 Boss 横向散射间隔。 */
    public static final int CH2_BOSS_SPREAD_X_SPEED_STEP = 2;
    /** 第 3 章精英横向散射间隔。 */
    public static final int CH3_ELITE_SPREAD_X_SPEED_STEP = 3;
    /** 第 3 章 Boss 横向散射间隔。 */
    public static final int CH3_BOSS_SPREAD_X_SPEED_STEP = 2;

    /** 第 4 章精英每次齐射的弹数。 */
    public static final int CH4_ELITE_VOLLEY_COUNT = 5;
    /** 第 4 章精英相邻弹道步长。 */
    public static final int CH4_ELITE_VOLLEY_STEP = 2;
    public static final int CH4_ELITE_PLUS_VOLLEY_COUNT = 7;
    public static final int CH4_ELITE_PLUS_VOLLEY_STEP = 2;
    public static final int CH4_ACE_VOLLEY_COUNT = 9;
    public static final int CH4_ACE_VOLLEY_STEP = 2;
    public static final int CH4_BOSS_VOLLEY_COUNT = 11;
    public static final int CH4_BOSS_VOLLEY_STEP = 2;
    public static final int CH4_BOSS_SWEEP_SHIFT_STEPS = 1;
    public static final int CH5_ELITE_VOLLEY_COUNT = 5;
    public static final int CH5_ELITE_VOLLEY_STEP = 2;
    public static final int CH5_ELITE_PLUS_VOLLEY_COUNT = 7;
    public static final int CH5_ELITE_PLUS_VOLLEY_STEP = 2;
    public static final int CH5_ACE_VOLLEY_COUNT = 9;
    public static final int CH5_ACE_VOLLEY_STEP = 2;
    public static final int CH5_BOSS_VOLLEY_COUNT = 11;
    public static final int CH5_BOSS_VOLLEY_STEP = 2;

    // =========================
    // 掉落
    // =========================
    /** 精英敌机坠毁后掉落任意道具的总概率。 */
    public static final double ITEM_DROP_PROBABILITY = 0.6;

    // =========================
    // 敌机基础属性
    // =========================
    public static final int MOB_ENEMY_HP = 30;
    public static final int MOB_ENEMY_SPEED_Y = 10;
    public static final int MOB_ENEMY_SCORE = 10;

    public static final int ELITE_ENEMY_HP = 60;
    public static final int ELITE_ENEMY_SPEED_Y = 8;
    public static final int ELITE_ENEMY_BULLET_POWER = 30;
    public static final int ELITE_ENEMY_SCORE = 20;

    public static final int ELITE_PLUS_ENEMY_HP = 80;
    public static final int ELITE_PLUS_ENEMY_SPEED_Y = 8;
    public static final int ELITE_PLUS_ENEMY_BULLET_POWER = 35;
    public static final int ELITE_PLUS_ENEMY_SCORE = 25;

    public static final int ACE_ENEMY_HP = 100;
    public static final int ACE_ENEMY_SPEED_Y = 7;
    public static final int ACE_ENEMY_BULLET_POWER = 42;
    public static final int ACE_ENEMY_SCORE = 35;

    public static final int BOSS_ENEMY_HP = 240;
    public static final int BOSS_ENEMY_SPEED_X = 4;
    public static final int BOSS_ENEMY_SPEED_Y = 0;
    public static final int BOSS_ENEMY_BULLET_POWER = 50;
    public static final int BOSS_ENEMY_SCORE = 100;

    // =========================
    // 成长与章节推进
    // =========================
    /** 每多少分升 1 级。 */
    public static final int SCORE_PER_LEVEL = 80;
    /** 玩家等级上限。 */
    public static final int MAX_LEVEL = 5;
    /** 下一次 Boss 阈值相对上一次增加多少分。 */
    public static final int BOSS_THRESHOLD_STEP = 200;
    /** Boss 击败后的白光/过场时长。 */
    public static final long CHAPTER_TRANSITION_FLASH_MILLIS = 1200L;

    // =========================
    // 敌机生成节奏
    // =========================
    public static final int EASY_BASE_SPAWN_CYCLE = 26;
    public static final int NORMAL_BASE_SPAWN_CYCLE = 20;
    public static final int HARD_BASE_SPAWN_CYCLE = 16;
    public static final int MIN_SPAWN_CYCLE = 8;
    public static final int SPAWN_CYCLE_SCORE_DIVISOR = 120;
    public static final int SPAWN_CYCLE_BOSS_STAGE_ACCELERATION = 2;

    // =========================
    // 敌机射击节奏
    // =========================
    public static final int EASY_BASE_SHOOT_CYCLE = 24;
    public static final int NORMAL_BASE_SHOOT_CYCLE = 20;
    public static final int HARD_BASE_SHOOT_CYCLE = 16;
    public static final int MIN_SHOOT_CYCLE = 8;
    public static final int SHOOT_CYCLE_SCORE_DIVISOR = 180;
    public static final int SHOOT_CYCLE_BOSS_STAGE_ACCELERATION = 1;

    // =========================
    // 敌机数量上限
    // =========================
    public static final int EASY_BASE_ENEMY_MAX = 4;
    public static final int NORMAL_BASE_ENEMY_MAX = 5;
    public static final int HARD_BASE_ENEMY_MAX = 6;
    public static final int MAX_ENEMY_MAX = 10;
    public static final int ENEMY_MAX_SCORE_DIVISOR = 200;
    public static final int ENEMY_MAX_BOSS_STAGE_BONUS = 1;

    // =========================
    // 精英敌机出现概率
    // =========================
    public static final double EASY_BASE_ELITE_PROBABILITY = 0.20;
    public static final double NORMAL_BASE_ELITE_PROBABILITY = 0.30;
    public static final double HARD_BASE_ELITE_PROBABILITY = 0.40;
    public static final double MAX_ELITE_PROBABILITY = 0.80;
    public static final double ELITE_PROBABILITY_SCORE_DIVISOR = 1000.0;
    public static final double ELITE_PROBABILITY_BOSS_STAGE_BONUS = 0.05;

    // =========================
    // 初始 Boss 阈值
    // =========================
    public static final int EASY_INITIAL_BOSS_THRESHOLD = 240;
    public static final int NORMAL_INITIAL_BOSS_THRESHOLD = 200;
    public static final int HARD_INITIAL_BOSS_THRESHOLD = 160;

    // =========================
    // 高阶敌机解锁与概率
    // =========================
    public static final int ELITE_PLUS_UNLOCK_SCORE = 150;
    public static final double ELITE_PLUS_BASE_PROBABILITY = 0.10;
    public static final double ELITE_PLUS_MAX_PROBABILITY = 0.22;
    public static final double ELITE_PLUS_SCORE_DIVISOR = 2000.0;
    public static final double ELITE_PLUS_HARD_BONUS = 0.04;
    public static final double ELITE_PLUS_EASY_BONUS = -0.02;

    public static final int ACE_UNLOCK_SCORE = 300;
    public static final double ACE_BASE_PROBABILITY = 0.05;
    public static final double ACE_MAX_PROBABILITY = 0.15;
    public static final double ACE_SCORE_DIVISOR = 3000.0;
    public static final double ACE_HARD_BONUS = 0.03;
    public static final double ACE_EASY_BONUS = -0.01;

    // =========================
    // 技能与道具效果
    // =========================
    /** 冻结技能基础持续时间。 */
    public static final long FREEZE_BASE_DURATION_MILLIS = 1000L;
    /** 冻结技能每级额外持续时间。 */
    public static final long FREEZE_LEVEL_BONUS_DURATION_MILLIS = 500L;
    /** 冻结技能冷却。 */
    public static final long FREEZE_SKILL_COOLDOWN_MILLIS = 6000L;
    /** 爆炸技能基础伤害。 */
    public static final int BOMB_BASE_DAMAGE = 40;
    /** 爆炸技能每级额外伤害。 */
    public static final int BOMB_LEVEL_BONUS_DAMAGE = 20;
    /** 爆炸技能冷却。 */
    public static final long BOMB_SKILL_COOLDOWN_MILLIS = 5000L;
    /** 护盾基础持续时间。 */
    public static final long SHIELD_BASE_DURATION_MILLIS = 800L;
    /** 护盾每级额外持续时间。 */
    public static final long SHIELD_LEVEL_BONUS_DURATION_MILLIS = 400L;
    /** 护盾技能冷却。 */
    public static final long SHIELD_SKILL_COOLDOWN_MILLIS = 7000L;
    /** 普通火力道具增加的火力等级。 */
    public static final int FIRE_SUPPLY_FIREPOWER_BONUS = 1;
    /** 超级火力道具增加的火力等级。 */
    public static final int FIRE_PLUS_SUPPLY_FIREPOWER_BONUS = 2;

    private GameplayBalance() {
    }

    public static int baseSpawnCycle(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return EASY_BASE_SPAWN_CYCLE;
            case HARD:
                return HARD_BASE_SPAWN_CYCLE;
            default:
                return NORMAL_BASE_SPAWN_CYCLE;
        }
    }

    public static int baseShootCycle(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return EASY_BASE_SHOOT_CYCLE;
            case HARD:
                return HARD_BASE_SHOOT_CYCLE;
            default:
                return NORMAL_BASE_SHOOT_CYCLE;
        }
    }

    public static int baseEnemyMax(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return EASY_BASE_ENEMY_MAX;
            case HARD:
                return HARD_BASE_ENEMY_MAX;
            default:
                return NORMAL_BASE_ENEMY_MAX;
        }
    }

    public static double baseEliteProbability(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return EASY_BASE_ELITE_PROBABILITY;
            case HARD:
                return HARD_BASE_ELITE_PROBABILITY;
            default:
                return NORMAL_BASE_ELITE_PROBABILITY;
        }
    }

    public static int initialBossThreshold(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return EASY_INITIAL_BOSS_THRESHOLD;
            case HARD:
                return HARD_INITIAL_BOSS_THRESHOLD;
            default:
                return NORMAL_INITIAL_BOSS_THRESHOLD;
        }
    }

    public static int playerMoveSpeed(AircraftBranch branch) {
        if (branch == null) {
            return PLAYER_BASE_MOVE_SPEED;
        }
        switch (branch) {
            /** 红色速度机比其他机型更快。 */
            case RED_SPEED:
                return PLAYER_BASE_MOVE_SPEED + 2;
            case STARTER_BLUE:
            default:
                return PLAYER_BASE_MOVE_SPEED;
        }
    }
}
